package com.example.MindBloom;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JournalManager provides a secure, user-scoped database access layer.
 * All wellness journal entries are persisted under:
 * JournalEntries/{authenticatedUserUid}/{entryId}
 *
 * Enforces authenticated access and surfaces Firebase errors cleanly without faking success.
 */
public class JournalManager {

    private static final String ROOT_JOURNAL_ENTRIES = "JournalEntries";
    private final FirebaseDatabase database;
    private final FirebaseAuth auth;

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface DataCallback {
        void onDataLoaded(List<JournalEntry> entries);
        void onError(String errorMessage);
    }

    public JournalManager() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Helper to get current authenticated user's database reference.
     * Path: JournalEntries/{userUid}
     */
    private DatabaseReference getUserDatabaseReference() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return database.getReference().child(ROOT_JOURNAL_ENTRIES).child(user.getUid());
    }

    public String getCurrentUserUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Save a new journal entry under the current user's UID using push().
     */
    public void saveEntry(JournalEntry entry, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to save entries.");
            }
            return;
        }

        if (entry.getId() == null || entry.getId().trim().isEmpty()) {
            String pushKey = userRef.push().getKey();
            if (pushKey == null) {
                if (callback != null) {
                    callback.onError("Unable to generate unique key from Firebase.");
                }
                return;
            }
            entry.setId(pushKey);
        }

        userRef.child(entry.getId()).setValue(entry, (error, ref) -> {
            if (error != null) {
                if (callback != null) {
                    callback.onError(formatFirebaseError(error));
                }
            } else {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    /**
     * Update an existing journal entry under the current user's UID.
     */
    public void updateEntry(JournalEntry entry, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to update entries.");
            }
            return;
        }

        if (entry == null || entry.getId() == null || entry.getId().trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Cannot update an entry without a valid ID.");
            }
            return;
        }

        userRef.child(entry.getId()).setValue(entry, (error, ref) -> {
            if (error != null) {
                if (callback != null) {
                    callback.onError(formatFirebaseError(error));
                }
            } else {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    /**
     * Delete an entry from the current user's database path.
     */
    public void deleteEntry(String entryId, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to delete entries.");
            }
            return;
        }

        if (entryId == null || entryId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid entry ID for deletion.");
            }
            return;
        }

        userRef.child(entryId).removeValue((error, ref) -> {
            if (error != null) {
                if (callback != null) {
                    callback.onError(formatFirebaseError(error));
                }
            } else {
                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    /**
     * Listen for real-time updates to journal entries belonging strictly to the current user.
     */
    public ValueEventListener listenToEntries(DataCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to view entries.");
            }
            return null;
        }

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<JournalEntry> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    JournalEntry entry = child.getValue(JournalEntry.class);
                    if (entry != null) {
                        list.add(entry);
                    }
                }
                // Sort newest entries first
                Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                if (callback != null) {
                    callback.onDataLoaded(list);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (callback != null) {
                    callback.onError(formatFirebaseError(error));
                }
            }
        };

        userRef.addValueEventListener(listener);
        return listener;
    }

    /**
     * Detach real-time value event listener to prevent leaks.
     */
    public void removeListener(ValueEventListener listener) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef != null && listener != null) {
            userRef.removeEventListener(listener);
        }
    }

    /**
     * Formats Firebase errors, clearly distinguishing permission denials from network or rule issues.
     */
    private String formatFirebaseError(DatabaseError error) {
        if (error == null) {
            return "Unknown database error.";
        }
        if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
            String uid = getCurrentUserUid();
            return "Firebase Permission Denied: Database rules currently restrict access to path /JournalEntries/" + (uid != null ? uid : "{uid}") + ". Please deploy authenticated user rules in the Firebase Console.";
        }
        return error.getMessage();
    }
}
