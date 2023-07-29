package socioverse.tifor;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import socioverse.tifor.Adapter.SearchUserRecyclerAdapter;
import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.FirebaseUtil;

public class SearchUserActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.seach_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);

        searchInput.requestFocus();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    onBackPressed();
                } catch (Exception e) {

                }

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String searchTerm = searchInput.getText().toString();
                    if (searchTerm.isEmpty()) {
                        searchInput.setError("Enter Name");
                        return;
                    }
                    setupSearchRecyclerView(searchTerm);
                } catch (Exception e) {
                }
            }
        });
    }

    void setupSearchRecyclerView(String searchTerm) {
        try {
            String searchTermUpper = searchTerm.toUpperCase();
            String searchTermLower = searchTerm.toLowerCase();

            Query query = FirebaseUtil.allUserCollectionReference().whereGreaterThanOrEqualTo("username", searchTermUpper).whereLessThanOrEqualTo("username", searchTermLower + "\uf8ff");

            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query, UserModel.class).build();

            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (adapter != null) {
                adapter.startListening();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (adapter != null) {
                adapter.stopListening();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (adapter != null) {
                adapter.startListening();
            }
        } catch (Exception e) {
        }
    }
}