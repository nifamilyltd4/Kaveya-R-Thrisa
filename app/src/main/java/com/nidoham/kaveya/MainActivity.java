package com.nidoham.kaveya;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.nidoham.kaveya.adapter.ChatAdapter;
import com.nidoham.kaveya.console.google.gemini.engine.ChatsEngine;
import com.nidoham.kaveya.databinding.ActivityMainBinding;
import com.nidoham.kaveya.databinding.NavHeaderBinding;
import com.nidoham.kaveya.firebase.google.database.model.Messages;
import com.nidoham.kaveya.firebase.google.database.repository.control.ChatRepositoryController;
import com.nidoham.kaveya.liberies.SketchwareUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String AI_ID = "system";
    private static final String USER_ID = "nifamilyltd4";

    private ActivityMainBinding binding;
    private NavHeaderBinding drawerBinding;
    private FirebaseAuth auth;
    private ChatAdapter chatAdapter;
    private ChatsEngine chatsEngine;
    private ChatRepositoryController chatController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeComponents();
        setupUI();
    }

    private void initializeComponents() {
        auth = FirebaseAuth.getInstance();
        chatsEngine = new ChatsEngine();
        chatsEngine.addMemory("Always respond to me in Bangla, My name is NI Doha Mondol");
        chatAdapter = new ChatAdapter(new ArrayList<>(), USER_ID, this);
        chatController = new ChatRepositoryController();
        setupChatAdapter();
        startListeningToMessages();
    }

    private void setupUI() {
        setSupportActionBar(binding.chatToolbar);
        setupNavigationDrawer();
        setupChatInterface();
        updateUserInfo();
    }

    private void startListeningToMessages() {
        chatController.startListeningToMessages(new ChatRepositoryController.MessagesListener() {
            @Override
            public void onMessagesChanged(List<Messages> messages) {
                chatAdapter.updateMessages(messages); // submitList এর পরিবর্তে updateMessages
                if (!messages.isEmpty()) {
                    binding.messagesRecyclerView.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e(TAG, "Error loading messages: " + error.getMessage(), error.toException());
                Toast.makeText(MainActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAIRequest(String messageText) {
        if (chatsEngine == null) {
            Log.e(TAG, "Chat engine not initialized");
            Toast.makeText(this, "Chat engine not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        chatsEngine.sendMessage(messageText, new ChatsEngine.ChatCallback() {
            @Override
            public void onResponse(@NonNull String response) {
                Messages aiResponse = createMessage(AI_ID, USER_ID, response, true);
                saveMessageToFirebase(aiResponse);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Log.e(TAG, "AI response error: " + error.getMessage(), error);
                Toast.makeText(MainActivity.this, "Failed to get AI response", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChatInterface() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.messagesRecyclerView.setLayoutManager(layoutManager);
        binding.messagesRecyclerView.setAdapter(chatAdapter);
        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupChatAdapter() {
        chatAdapter.setOnMessageClickListener(new ChatAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(Messages message, int position) {
                // ইমপ্লিমেন্ট করা যায়
            }

            @Override
            public void onMessageLongClick(Messages message, int position, android.view.View view) {
                // ইমপ্লিমেন্ট করা যায়
            }

            @Override
            public void onMessageDelete(Messages message, int position) {
                if (message == null || message.getId() == null) return;
                chatController.removeMessage(message.getId(), new ChatRepositoryController.MessageCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Message deleted successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error deleting message: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Error deleting message", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReactionAdded(Messages message, String reaction) {
                if (reaction != null) sendAIRequest(reaction);
            }

            @Override
            public void onTranslationRequested(Messages message) {
                Toast.makeText(MainActivity.this, "Translation feature coming soon", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVoicePlaybackRequested(Messages message) {
                Toast.makeText(MainActivity.this, "Voice playback feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = binding.messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        Messages newMessage = createMessage(USER_ID, AI_ID, messageText, false);
        saveMessageToFirebase(newMessage);
        binding.messageInput.setText("");
        sendAIRequest(messageText);
    }

    private Messages createMessage(String senderId, String receiverId, String content, boolean isAI) {
        Messages message = new Messages(senderId, receiverId, content, isAI);
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    private void saveMessageToFirebase(Messages message) {
        chatController.insertMessage(message, new ChatRepositoryController.MessageCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Message saved successfully");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error saving message: " + e.getMessage(), e);
                Toast.makeText(MainActivity.this, "Error saving message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationDrawer() {
        drawerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));
        binding.drawerIcon.setOnClickListener(v -> toggleDrawer());
        binding.btnCallOptions.setOnClickListener(v -> startActivity(new Intent(this, AssistantActivity.class)));
        binding.navView.setNavigationItemSelectedListener(item -> {
            handleNavigationItem(item.getItemId());
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        binding.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, GravityCompat.END);
            popup.getMenuInflater().inflate(R.menu.more_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this::handleMenuItemClick);
            popup.show();
        });
    }

    private void toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void handleNavigationItem(int itemId) {
        String message = itemId == R.id.nav_games ? "Games" : itemId == R.id.nav_identifier ? "Identifier" : "";
        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean handleMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_update) {
            Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.action_whats_new) {
            Toast.makeText(this, "What's new", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.action_about) {
            Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else {
            return false;
        }
        return true;
    }

    private void updateUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            drawerBinding.textUsername.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            drawerBinding.indicator.setText(user.getEmail() != null ? user.getEmail() : "Not signed in");
            if (user.getPhotoUrl() != null) {
                loadUserImage(user.getPhotoUrl().toString());
            } else {
                setDefaultAvatar();
            }
        } else {
            setDefaultUserProfile();
        }
    }

    private void setDefaultUserProfile() {
        drawerBinding.textUsername.setText("Guest");
        drawerBinding.indicator.setText("Not signed in");
        setDefaultAvatar();
    }

    private void setDefaultAvatar() {
        drawerBinding.imageView.setImageResource(R.drawable.default_avatar);
        binding.avatarImage.setImageResource(R.drawable.default_avatar);
    }

    private void loadUserImage(String photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(drawerBinding.imageView);
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(binding.avatarImage);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatAdapter.cleanup();
        chatController.cleanup();
        chatsEngine = null;
        drawerBinding = null;
        binding = null;
    }
}