package com.nidoham.kaveya.firebase.google.database.repository.control;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.nidoham.kaveya.firebase.google.database.model.Messages;
import com.nidoham.kaveya.firebase.google.database.repository.ChatRepository;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class ChatRepositoryController {
    private final ChatRepository chatRepository;

    public interface MessageCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface MessagesListener {
        void onMessagesChanged(List<Messages> messages);
        void onError(DatabaseError error);
    }

    // No userId parameter needed anymore
    public ChatRepositoryController() {
        this.chatRepository = new ChatRepository(FirebaseDatabase.getInstance());
    }

    public void startListeningToMessages(@NonNull MessagesListener listener) {
        chatRepository.listenToMessages(
            new Function1<List<Messages>, Unit>() {
                @Override
                public Unit invoke(List<Messages> messages) {
                    listener.onMessagesChanged(messages);
                    return Unit.INSTANCE;
                }
            },
            new Function1<DatabaseError, Unit>() {
                @Override
                public Unit invoke(DatabaseError error) {
                    listener.onError(error);
                    return Unit.INSTANCE;
                }
            }
        );
    }

    public void insertMessage(@NonNull Messages message, @NonNull MessageCallback callback) {
        chatRepository.insertMessage(
            message,
            new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    callback.onSuccess();
                    return Unit.INSTANCE;
                }
            },
            new Function1<Exception, Unit>() {
                @Override
                public Unit invoke(Exception e) {
                    callback.onError(e);
                    return Unit.INSTANCE;
                }
            }
        );
    }

    public void removeMessage(@NonNull String messageId, @NonNull MessageCallback callback) {
        chatRepository.removeMessage(
            messageId,
            new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    callback.onSuccess();
                    return Unit.INSTANCE;
                }
            },
            new Function1<Exception, Unit>() {
                @Override
                public Unit invoke(Exception e) {
                    callback.onError(e);
                    return Unit.INSTANCE;
                }
            }
        );
    }

    public String getCurrentUtcTime() {
        return chatRepository.getCurrentUtcTime();
    }

    public void cleanup() {
        chatRepository.cleanup();
    }
}