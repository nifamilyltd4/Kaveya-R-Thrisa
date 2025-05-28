package com.nidoham.kaveya.firebase.google.database.repository.control;

import androidx.annotation.NonNull;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.nidoham.kaveya.firebase.google.database.repository.MemoriesRepository;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class MemoriesRepositoryController {
    
    private final MemoriesRepository repository;
    
    public interface MemoriesCallback {
        void onSuccess();
        void onError(Exception e);
    }
    
    public interface MemoriesListener {
        void onMessagesChanged(String message);
        void onError(DatabaseError error);
    }
    
    // Constructor requires character parameter
    public MemoriesRepositoryController(@NonNull String character) {
        this.repository = new MemoriesRepository(FirebaseDatabase.getInstance(), character);
    }
    
    public void startListeningToMessages(@NonNull MemoriesListener listener) {
        repository.listenToMessages(
            new Function1<String, Unit>() {
                @Override
                public Unit invoke(String message) {
                    listener.onMessagesChanged(message);
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
    
    public void insertMessage(@NonNull String message, @NonNull MemoriesCallback callback) {
        repository.insertMessage(
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
    
    public void removeMessage(@NonNull MemoriesCallback callback) {
        repository.removeMessage(
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
        return repository.getCurrentUtcTime();
    }
    
    public void cleanup() {
        repository.cleanup();
    }
}