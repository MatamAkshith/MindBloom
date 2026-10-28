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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GoalManager provides a secure, user-scoped database access layer.
 * All goals are persisted under:
 * Goals/{authenticatedUserUid}/{goalId}
 *
 * Enforces authenticated access and surfaces Firebase errors cleanly without faking success.
 */
public class GoalManager {

    private static final String ROOT_GOALS = "Goals";
    private final FirebaseDatabase database;
    private final FirebaseAuth auth;

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface DataCallback {
        void onDataLoaded(List<Goal> goals);
        void onError(String errorMessage);
    }

    public GoalManager() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Helper to get current authenticated user's database reference.
     * Path: Goals/{userUid}
     */
    private DatabaseReference getUserDatabaseReference() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return database.getReference().child(ROOT_GOALS).child(user.getUid());
    }

    public String getCurrentUserUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Save a new goal under the current user's UID using push().
     */
    public void saveGoal(Goal goal, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to save goals.");
            }
            return;
        }

        if (goal.getId() == null || goal.getId().trim().isEmpty()) {
            String pushKey = userRef.push().getKey();
            if (pushKey == null) {
                if (callback != null) {
                    callback.onError("Unable to generate unique key from Firebase.");
                }
                return;
            }
            goal.setId(pushKey);
        }

        userRef.child(goal.getId()).setValue(goal, (error, ref) -> {
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
     * Update an existing goal under the current user's UID.
     */
    public void updateGoal(Goal goal, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to update goals.");
            }
            return;
        }

        if (goal == null || goal.getId() == null || goal.getId().trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Cannot update a goal without a valid ID.");
            }
            return;
        }

        userRef.child(goal.getId()).setValue(goal, (error, ref) -> {
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
     * Quick helper to update progress and updatedAt timestamp.
     */
    public void updateGoalProgress(String goalId, int newProgress, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to update goals.");
            }
            return;
        }

        if (goalId == null || goalId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid goal ID.");
            }
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", newProgress);
        updates.put("updatedAt", System.currentTimeMillis());

        userRef.child(goalId).updateChildren(updates, (error, ref) -> {
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
     * Delete a goal from the current user's database path.
     */
    public void deleteGoal(String goalId, OperationCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to delete goals.");
            }
            return;
        }

        if (goalId == null || goalId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid goal ID for deletion.");
            }
            return;
        }

        userRef.child(goalId).removeValue((error, ref) -> {
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
     * Listen for real-time updates to goals belonging strictly to the current user.
     * Sorts goals by target date ascending (soonest deadline first).
     */
    public ValueEventListener listenToGoals(DataCallback callback) {
        DatabaseReference userRef = getUserDatabaseReference();
        if (userRef == null) {
            if (callback != null) {
                callback.onError("User is not signed in. Please log in to view goals.");
            }
            return null;
        }

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Goal> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Goal goal = child.getValue(Goal.class);
                    if (goal != null) {
                        list.add(goal);
                    }
                }
                // Sort goals by target date ascending (soonest deadline first)
                Collections.sort(list, (a, b) -> {
                    int dateComp = Long.compare(a.getTargetDate(), b.getTargetDate());
                    if (dateComp != 0) return dateComp;
                    return Long.compare(b.getUpdatedAt(), a.getUpdatedAt());
                });

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
     * Formats Firebase errors, clearly distinguishing permission denials from network issues.
     */
    private String formatFirebaseError(DatabaseError error) {
        if (error == null) {
            return "Unknown database error.";
        }
        if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
            String uid = getCurrentUserUid();
            return "Firebase Permission Denied: Database rules currently restrict access to path /Goals/" + (uid != null ? uid : "{uid}") + ". Please deploy authenticated user rules in the Firebase Console.";
        }
        return error.getMessage();
    }
}
