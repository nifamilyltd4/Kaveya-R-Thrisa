package com.nidoham.kaveya.features;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.nidoham.kaveya.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;


public class ReactionBottomSheetDialog {
    private final Context context;
    private final BottomSheetDialog dialog;
    private ReactionCallback callback;
    
    public interface ReactionCallback {
        void onReactionSelected(String emojiName, String emoji);
    }
    
    public ReactionBottomSheetDialog(Context context) {
        this.context = context;
        this.dialog = new BottomSheetDialog(context);
        setupDialog();
    }
    
    private void setupDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_reaction_selector, null);
        initializeViews(view);
        dialog.setContentView(view);
    }
    
    private void initializeViews(View view) {
        setupReactionButton(view, R.id.btnLike, "Like", "👍");
        setupReactionButton(view, R.id.btnLove, "Love", "❤️");
        setupReactionButton(view, R.id.btnLaugh, "Laugh", "😂");
        setupReactionButton(view, R.id.btnWow, "Wow", "😮");
        setupReactionButton(view, R.id.btnSad, "Sad", "😢");
        setupReactionButton(view, R.id.btnAngry, "Angry", "😡");
        setupReactionButton(view, R.id.btnClap, "Clap", "👏");
        setupReactionButton(view, R.id.btnFire, "Fire", "🔥");
        setupReactionButton(view, R.id.btnParty, "Party", "🎉");
    }
    
    private void setupReactionButton(View view, int buttonId, String emojiName, String emoji) {
        MaterialCardView button = view.findViewById(buttonId);
        button.setOnClickListener(v -> handleReaction(emojiName, emoji));
    }
    
    private void handleReaction(String emojiName, String emoji) {
        if (callback != null) {
            callback.onReactionSelected(emojiName, emoji);
        }
        dialog.dismiss();
    }
    
    public void setCallback(ReactionCallback callback) {
        this.callback = callback;
    }
    
    public void show() {
        dialog.show();
    }
}