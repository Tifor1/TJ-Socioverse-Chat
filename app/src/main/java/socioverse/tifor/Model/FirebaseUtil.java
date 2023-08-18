package socioverse.tifor.Model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {

    public static DocumentReference currentUserDetails() {
        try {
            String currentUserId = currentUserDetails().getId();
            if (currentUserId != null) {
                return FirebaseFirestore.getInstance().collection("users").document(currentUserId);
            }
        } catch (Exception e) {

        }
        return null;
    }
}