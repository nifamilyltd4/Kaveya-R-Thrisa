package com.nidoham.kaveya.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.nidoham.kaveya.R;
import com.nidoham.kaveya.databinding.*;
import com.nidoham.kaveya.features.ReactionBottomSheetDialog;
import com.nidoham.kaveya.firebase.google.database.model.Messages;
import com.nidoham.kaveya.liberies.SketchwareUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced ChatAdapter with improved features
 * - Support for multiple media types (text, image, voice)
 * - Message reactions
 * - Improved performance with paging
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SENT_MEDIA = 3;
    private static final int VIEW_TYPE_RECEIVED_MEDIA = 4;
    private static final int VIEW_TYPE_SENT_VOICE = 5;
    private static final int VIEW_TYPE_RECEIVED_VOICE = 6;
    private static final int SMOOTH_SCROLL_THRESHOLD = 5; // Messages threshold for smooth scroll
    private static final int PREFETCH_DISTANCE = 10; // Number of items to prefetch

    private List<Messages> messageList;
    private final String currentUserId;
    private final Context context;
    private final SimpleDateFormat timeFormat;
    private OnMessageClickListener clickListener;
    private final RequestOptions imageRequestOptions;
    private ExecutorService backgroundExecutor;
    private RecyclerView recyclerView;
    private boolean isScrolling = false;
    private boolean pendingScroll = false;
    private boolean suppressScrolling = false;
    private TextToSpeech textToSpeech;
    private final Map<String, Translator> translators = new HashMap<>();
    private final Map<String, List<String>> messageReactions = new HashMap<>();

    private PopupWindow popupWindow;

    public interface OnMessageClickListener {
        void onMessageClick(Messages message, int position);
        void onMessageLongClick(Messages message, int position, View view);
        void onMessageDelete(Messages message, int position);
        void onReactionAdded(Messages message, String reaction);
        void onTranslationRequested(Messages message);
        void onVoicePlaybackRequested(Messages message);
    }

    public ChatAdapter(List<Messages> messageList, String currentUserId, Context context) {
        this.messageList = (messageList != null) ? new ArrayList<>(messageList) : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.context = context;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.imageRequestOptions = new RequestOptions()
                .placeholder(R.drawable.app_icon)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        this.backgroundExecutor = Executors.newFixedThreadPool(2);
        
        // Initialize TextToSpeech
        initTextToSpeech();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English if default language is not supported
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;

        // Add scroll listener to detect when user is manually scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isScrolling = false;
                    if (pendingScroll) {
                        scrollToBottom(true);
                        pendingScroll = false;
                    }
                }
            }
            
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Prefetch images when scrolling to improve performance
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    int lastVisible = layoutManager.findLastVisibleItemPosition();
                    
                    // Prefetch images for items that will soon be visible
                    int start = Math.max(0, firstVisible - PREFETCH_DISTANCE);
                    int end = Math.min(messageList.size() - 1, lastVisible + PREFETCH_DISTANCE);
                    
                    for (int i = start; i <= end; i++) {
                        Messages message = messageList.get(i);
                        if (message.getMediaType() == Messages.MediaType.IMAGE && message.getMediaUrl() != null) {
                            Glide.with(context).load(message.getMediaUrl()).preload();
                        }
                    }
                }
            }
        });

        // Set fixed size for better performance if content size doesn't change
        recyclerView.setHasFixedSize(true);

        // Improve animation stability
        recyclerView.getItemAnimator().setChangeDuration(150);
        recyclerView.getItemAnimator().setAddDuration(200);
        recyclerView.getItemAnimator().setRemoveDuration(150);
    }

    public void scrollToBottom(boolean smooth) {
        if (recyclerView == null || messageList.isEmpty() || suppressScrolling) return;

        if (isScrolling) {
            pendingScroll = true;
            // Fallback to force scroll after a delay if still pending
            recyclerView.postDelayed(() -> {
                if (pendingScroll && !isScrolling) {
                    scrollToBottom(smooth);
                    pendingScroll = false;
                }
            }, 300); // Reduced delay for better responsiveness
            return;
        }

        int lastPosition = messageList.size() - 1;
        if (smooth) {
            recyclerView.smoothScrollToPosition(lastPosition);
        } else {
            recyclerView.scrollToPosition(lastPosition);
        }
    }

    private boolean isNearBottom() {
        if (recyclerView == null) return false;

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) return false;

        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int lastPosition = messageList.size() - 1;

        return lastPosition - lastVisibleItemPosition <= SMOOTH_SCROLL_THRESHOLD;
    }

    private void showMessageOptions(Messages message, int position, View anchorView) {
        dismissPopup();

        try {
            // Inflate the custom layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.layout_message_options, null);
            
            int widthInPx = (int) (160 * context.getResources().getDisplayMetrics().density);
            
            // Create the popup window
            popupWindow = new PopupWindow(
                    popupView,
                    widthInPx,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );
            
            // Set up click listeners for each option
            View btnCopy = popupView.findViewById(R.id.btnCopy);
            View btnSpeak = popupView.findViewById(R.id.btnSpeak);
            View btnTranslate = popupView.findViewById(R.id.btnTranslate);
            View btnShare = popupView.findViewById(R.id.btnShare);
            View btnDelete = popupView.findViewById(R.id.btnDelete);
            
            btnCopy.setOnClickListener(v -> {
                copyToClipboard(message.getText());
                dismissPopup();
            });
            
            btnSpeak.setOnClickListener(v -> {
                speakText(message.getText());
                if (clickListener != null) {
                    clickListener.onVoicePlaybackRequested(message);
                }
                dismissPopup();
            });
            
            btnTranslate.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTranslationRequested(message);
                    translateMessage(message, position);
                }
                dismissPopup();
            });
            
            btnShare.setOnClickListener(v -> {
                shareMessage(message.getText());
                dismissPopup();
            });
            
            btnDelete.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageDelete(message, position);
                }
                dismissPopup();
            });
            
            // Show the popup window
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_background));
            popupWindow.setElevation(10f);
            popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
            
        } catch (Exception e) {
            // Fallback to simple toast if popup fails
            Toast.makeText(context, "Message options: Copy, Share, Delete", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showReactionBottomSheet(Messages message) {
        ReactionBottomSheetDialog bottomSheet = new ReactionBottomSheetDialog(context);
        bottomSheet.setCallback(new ReactionBottomSheetDialog.ReactionCallback() {
            @Override
            public void onReactionSelected(String emojiName, String emoji) {
                // Handle the reaction here
                addReaction(message, emoji); // Default reaction
            }
        });
        bottomSheet.show();
    }
    
    
    private void dismissPopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
    
    private void addReaction(Messages message, String reaction) {
        String messageId = message.getId();
        if (!messageReactions.containsKey(messageId)) {
            messageReactions.put(messageId, new ArrayList<>());
        }
        
        List<String> reactions = messageReactions.get(messageId);
        if (reactions != null && !reactions.contains(reaction)) {
            reactions.add(reaction);
            notifyDataSetChanged(); // Update to show the reaction
            
            if (clickListener != null) {
                clickListener.onReactionAdded(message, reaction);
            }
        }
    }
    
    private void translateMessage(Messages message, int position) {
        // Get user's preferred language from settings or device
        String targetLanguage = Locale.getDefault().getLanguage();
        if (targetLanguage.equals("en")) {
            // If device is in English, translate to Bengali as fallback
            targetLanguage = "bn";
        }
        
        // Get or create translator
        Translator translator = getTranslator("en", targetLanguage);
        
        // Show translation in progress
        Toast.makeText(context, "অনুবাদ করা হচ্ছে...", Toast.LENGTH_SHORT).show();
        
        // Perform translation in background
        backgroundExecutor.execute(() -> {
            translator.translate(message.getText())
                    .addOnSuccessListener(translatedText -> {
                        // Update UI on main thread
                        recyclerView.post(() -> {
                            // Create a temporary message with translated text
                            message.setText(translatedText+ " (Translated)");
                            message.setTranslated(true);
                            overrideMessage(message, position);
                                
                            // Scroll to show the translation
                            scrollToBottom(true);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Show error on main thread
                        recyclerView.post(() -> {
                            Toast.makeText(context, "Translation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    });
        });
    }
    
    private Translator getTranslator(String sourceLanguage, String targetLanguage) {
        String key = sourceLanguage + "_" + targetLanguage;
        if (!translators.containsKey(key)) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLanguage)
                    .setTargetLanguage(targetLanguage)
                    .build();
            Translator translator = Translation.getClient(options);
            
            // Download translation model if needed
            translator.downloadModelIfNeeded()
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to download translation model", Toast.LENGTH_SHORT).show();
                    });
            
            translators.put(key, translator);
        }
        
        return translators.get(key);
    }
    
    private void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "message_tts");
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Message", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareMessage(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(shareIntent, "Share message via"));
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messageList.get(position);
        
        // Bind based on view holder type
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, position);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message, position);
        } else if (holder instanceof SentMediaViewHolder) {
            ((SentMediaViewHolder) holder).bind(message, position);
        } else if (holder instanceof ReceivedMediaViewHolder) {
            ((ReceivedMediaViewHolder) holder).bind(message, position);
        } else if (holder instanceof SentVoiceViewHolder) {
            ((SentVoiceViewHolder) holder).bind(message, position);
        } else if (holder instanceof ReceivedVoiceViewHolder) {
            ((ReceivedVoiceViewHolder) holder).bind(message, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messageList.get(position);
        
        if (message.isAI()) {
            // Received messages (AI)
            if (message.getMediaType() == Messages.MediaType.IMAGE) {
                return VIEW_TYPE_RECEIVED_MEDIA;
            } else if (message.getMediaType() == Messages.MediaType.VOICE) {
                return VIEW_TYPE_RECEIVED_VOICE;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        } else {
            // Sent messages (User)
            if (message.getMediaType() == Messages.MediaType.IMAGE) {
                return VIEW_TYPE_SENT_MEDIA;
            } else if (message.getMediaType() == Messages.MediaType.VOICE) {
                return VIEW_TYPE_SENT_VOICE;
            } else {
                return VIEW_TYPE_SENT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case VIEW_TYPE_SENT:
                ItemSentMessageBinding sentBinding = ItemSentMessageBinding.inflate(inflater, parent, false);
                return new SentMessageViewHolder(sentBinding);
                
            case VIEW_TYPE_RECEIVED:
                ItemReceivedMessageBinding receivedBinding = ItemReceivedMessageBinding.inflate(inflater, parent, false);
                return new ReceivedMessageViewHolder(receivedBinding);
                
            case VIEW_TYPE_SENT_MEDIA:
                ItemSentMediaMessageBinding sentMediaBinding = ItemSentMediaMessageBinding.inflate(inflater, parent, false);
                return new SentMediaViewHolder(sentMediaBinding);
                
            case VIEW_TYPE_RECEIVED_MEDIA:
                ItemReceivedMediaMessageBinding receivedMediaBinding = ItemReceivedMediaMessageBinding.inflate(inflater, parent, false);
                return new ReceivedMediaViewHolder(receivedMediaBinding);
                
            case VIEW_TYPE_SENT_VOICE:
                ItemSentVoiceMessageBinding sentVoiceBinding = ItemSentVoiceMessageBinding.inflate(inflater, parent, false);
                return new SentVoiceViewHolder(sentVoiceBinding);
                
            case VIEW_TYPE_RECEIVED_VOICE:
                ItemReceivedVoiceMessageBinding receivedVoiceBinding = ItemReceivedVoiceMessageBinding.inflate(inflater, parent, false);
                return new ReceivedVoiceViewHolder(receivedVoiceBinding);
                
            default:
                ItemSentMessageBinding defaultBinding = ItemSentMessageBinding.inflate(inflater, parent, false);
                return new SentMessageViewHolder(defaultBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }

        Messages message = messageList.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).updateText(message.getText());
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).updateText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private static class MessageDiffCallback extends DiffUtil.Callback {
        private final List<Messages> oldList;
        private final List<Messages> newList;

        MessageDiffCallback(List<Messages> oldList, List<Messages> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();
            return oldId != null && oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Messages oldMessage = oldList.get(oldItemPosition);
            Messages newMessage = newList.get(newItemPosition);

            return Objects.equals(oldMessage.getText(), newMessage.getText()) &&
                   Objects.equals(oldMessage.getMediaUrl(), newMessage.getMediaUrl()) &&
                   Objects.equals(oldMessage.getMediaType(), newMessage.getMediaType()) &&
                   oldMessage.getTimestamp() == newMessage.getTimestamp() &&
                   oldMessage.isAI() == newMessage.isAI();
        }

        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return new Object();
        }
    }

    public void updateMessages(List<Messages> newMessages) {
        if (newMessages == null) return;

        boolean wasAtBottom = isNearBottom();
        List<Messages> oldList = new ArrayList<>(messageList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(oldList, newMessages));

        suppressScrolling = true;
        messageList.clear();
        messageList.addAll(newMessages);
        diffResult.dispatchUpdatesTo(this);
        suppressScrolling = false;

        if (wasAtBottom && recyclerView != null) {
            recyclerView.post(() -> scrollToBottom(true));
        }
    }
    
    /**
     * Override message content at specific position
     */
    public void overrideMessage(Messages message, int position) {
        if (message == null || position < 0 || position >= messageList.size()) {
            return;
        }
        messageList.set(position, message);
        notifyItemChanged(position);
    }
    
    public void addMessage(Messages message) {
        if (message == null) return;

        boolean shouldScroll = isNearBottom();

        int existingIndex = -1;
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getId() != null && messageList.get(i).getId().equals(message.getId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            messageList.set(existingIndex, message);
            notifyItemChanged(existingIndex);
        } else {
            messageList.add(message);
            notifyItemInserted(messageList.size() - 1);
        }

        if (shouldScroll && recyclerView != null) {
            recyclerView.post(() -> scrollToBottom(true));
        }
    }

    public void removeMessage(int position) {
   
        // Validate position bounds
        if (position < 0 || position >= messageList.size()) {
            return;
        }

        // Remove message at the specified position
        messageList.remove(position);
        notifyItemRemoved(position);
        // Notify about changes in subsequent items
        notifyItemRangeChanged(position, messageList.size());
    }
    
    public void removeMessageById(String messageId) {
    
        if (messageId == null || messageList == null) {
            return;
        }

        for (int i = 0; i < messageList.size(); i++) {
            Messages message = messageList.get(i);
            if (message != null && messageId.equals(message.getId())) {
                messageList.remove(i);
                notifyItemRemoved(i);
                // Notify about changes in subsequent items
                notifyItemRangeChanged(i, messageList.size());
                break; // Exit loop after removing the message
            }
        }
    }
    
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemSentMessageBinding binding;

        SentMessageViewHolder(ItemSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            binding.messageText.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.messageCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.messageCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.messageCard);
                return true;
            });

            binding.messageCard.setTransitionName("message_" + message.getId());
        }

        void updateText(String text) {
            binding.messageText.setText(text);
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            binding.messageText.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.messageCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    showReactionBottomSheet(message);
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.messageCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.messageCard);
                return true;
            });

            binding.messageCard.setTransitionName("message_" + message.getId());
        }

        void updateText(String text) {
            binding.messageText.setText(text);
        }
    }
    
    class SentMediaViewHolder extends RecyclerView.ViewHolder {
        private final ItemSentMediaMessageBinding binding;

        SentMediaViewHolder(ItemSentMediaMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            // Load image with Glide
            if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                Glide.with(context)
                    .load(message.getMediaUrl())
                    .apply(imageRequestOptions)
                    .error(R.drawable.default_background)
                    .into(binding.mediaImage);
            }
            
            binding.messageCaption.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.mediaCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.mediaCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.mediaCard);
                return true;
            });

            binding.mediaCard.setTransitionName("message_" + message.getId());
        }
    }
    
    class ReceivedMediaViewHolder extends RecyclerView.ViewHolder {
        private final ItemReceivedMediaMessageBinding binding;

        ReceivedMediaViewHolder(ItemReceivedMediaMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            // Load image with Glide
            if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                Glide.with(context)
                    .load(message.getMediaUrl())
                    .apply(imageRequestOptions)
                    .error(R.drawable.default_background)
                    .into(binding.mediaImage);
            }
            
            binding.messageCaption.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.mediaCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.mediaCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.mediaCard);
                return true;
            });

            binding.mediaCard.setTransitionName("message_" + message.getId());
        }
    }
    
    class SentVoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemSentVoiceMessageBinding binding;

        SentVoiceViewHolder(ItemSentVoiceMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            binding.messageText.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Set up play button
            binding.btnPlay.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onVoicePlaybackRequested(message);
                    
                    // Toggle play/pause UI
                    boolean isPlaying = binding.btnPlay.getTag() != null && 
                                       (boolean) binding.btnPlay.getTag();
                    binding.btnPlay.setTag(!isPlaying);
                    binding.btnPlay.setImageResource(isPlaying ? 
                            R.drawable.ic_play : R.drawable.ic_pause);
                }
            });
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.voiceCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.voiceCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.voiceCard);
                return true;
            });

            binding.voiceCard.setTransitionName("message_" + message.getId());
        }
    }
    
    class ReceivedVoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemReceivedVoiceMessageBinding binding;

        ReceivedVoiceViewHolder(ItemReceivedVoiceMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final Messages message, final int position) {
            binding.messageText.setText(message.getText());
            binding.messageTimestamp.setText(timeFormat.format(message.getTimestamp()));
            
            // Set up play button
            binding.btnPlay.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onVoicePlaybackRequested(message);
                    
                    // Toggle play/pause UI
                    boolean isPlaying = binding.btnPlay.getTag() != null && 
                                       (boolean) binding.btnPlay.getTag();
                    binding.btnPlay.setTag(!isPlaying);
                    binding.btnPlay.setImageResource(isPlaying ? 
                            R.drawable.ic_play : R.drawable.ic_pause);
                }
            });
            
            // Show reactions if any
            if (messageReactions.containsKey(message.getId()) && !messageReactions.get(message.getId()).isEmpty()) {
                binding.reactionContainer.setVisibility(View.VISIBLE);
                binding.reactionText.setText(String.join(" ", messageReactions.get(message.getId())));
            } else {
                binding.reactionContainer.setVisibility(View.GONE);
            }

            binding.voiceCard.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMessageClick(message, position);
                }
            });

            binding.voiceCard.setOnLongClickListener(v -> {
                showMessageOptions(message, position, binding.voiceCard);
                return true;
            });

            binding.voiceCard.setTransitionName("message_" + message.getId());
        }
    }

    public void cleanup() {
        dismissPopup();

        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdownNow();
            backgroundExecutor = null;
        }

        if (recyclerView != null) {
            recyclerView.clearOnScrollListeners();
            recyclerView = null;
        }
        
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        // Clean up translators
        for (Translator translator : translators.values()) {
            translator.close();
        }
        translators.clear();
    }
}
