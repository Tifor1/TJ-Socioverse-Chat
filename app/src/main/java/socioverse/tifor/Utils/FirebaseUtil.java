package socioverse.tifor.Utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId() {
        try {
            return FirebaseAuth.getInstance().getUid();
        } catch (Exception e) {

            return null;
        }
    }

    public static boolean isLoggedIn() {
        try {
            if (currentUserId() != null) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static DocumentReference currentUserDetails() {
        try {
            String currentUserId = currentUserId();
            if (currentUserId != null) {
                return FirebaseFirestore.getInstance().collection("users").document(currentUserId);
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static CollectionReference allUserCollectionReference() {
        try {
            return FirebaseFirestore.getInstance().collection("users");
        } catch (Exception e) {
            return null;
        }
    }

    public static DocumentReference getChatroomReference(String chatroomId) {
        try {
            return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
        } catch (Exception e) {
            return null;
        }
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId) {
        try {
            return getChatroomReference(chatroomId).collection("chats");
        } catch (Exception e) {
            return null;
        }
    }

    public static String getChatroomId(String userId1, String userId2) {
        try {
            if (userId1.hashCode() < userId2.hashCode()) {
                return userId1 + "_" + userId2;
            } else {
                return userId2 + "_" + userId1;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static CollectionReference allChatroomCollectionReference() {
        try {
            return FirebaseFirestore.getInstance().collection("chatrooms");
        } catch (Exception e) {
            return null;
        }
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        try {
            if (userIds.get(0).equals(FirebaseUtil.currentUserId())) {
                return allUserCollectionReference().document(userIds.get(1));
            } else {
                return allUserCollectionReference().document(userIds.get(0));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String timestampTo12HourString(Timestamp timestamp) {
        try {
            return new SimpleDateFormat("hh:mm a").format(timestamp.toDate());
        } catch (Exception e) {
            return null;
        }
    }

    public static void logout() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {

        }
    }

    public static StorageReference getCurrentProfilePicStorageRef() {
        try {
            return FirebaseStorage.getInstance().getReference().child("profile_pic").child(FirebaseUtil.currentUserId());
        } catch (Exception e) {
            return null;
        }
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId) {
        try {
            return FirebaseStorage.getInstance().getReference().child("profile_pic").child(otherUserId);
        } catch (Exception e) {
            return null;
        }
    }
}