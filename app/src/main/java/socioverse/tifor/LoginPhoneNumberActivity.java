package socioverse.tifor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;

import socioverse.tifor.Model.UserModel;
import socioverse.tifor.Utils.AndroidUtil;
import socioverse.tifor.Utils.FirebaseUtil;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    CountryCodePicker countryCodePicker;
    EditText phoneInput;
    Button sendOtpBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);

        try {
            initializeViews();
            setClickListeners();
        } catch (Exception e) {
            // Handle any exceptions that may occur during initialization.
            // You can display a toast message or log the error for debugging.
            Toast.makeText(this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        countryCodePicker = findViewById(R.id.login_countrycode);
        phoneInput = findViewById(R.id.login_mobile_number);
        sendOtpBtn = findViewById(R.id.send_otp_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        progressBar.setVisibility(View.GONE);

        // Register the phone input EditText with the countryCodePicker
        countryCodePicker.registerCarrierNumberEditText(phoneInput);
    }

    private void setClickListeners() {
        sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    if (!isValidSignInDetails()) {

                        if (!countryCodePicker.isValidFullNumber()) {
                            phoneInput.setError("Phone number not valid");
                            return;
                        }

                    }

                    // Proceed to the next activity and send the phone number along with the country code
                    Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
                    intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus());
                    startActivity(intent);
                } catch (Exception e) {
                    // Handle any exceptions that may occur during the click event.
                    // You can display a toast message or log the error for debugging.
                    Toast.makeText(LoginPhoneNumberActivity.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private boolean isValidSignInDetails() {

        String number = phoneInput.getText().toString().trim();
        String phoneNumberPattern = "^[0-9]{10}.$";

        if (number.isEmpty()) {
            phoneInput.setError("Enter a Phone Number");
            return false;
        } else if (!number.matches(phoneNumberPattern)) {
            phoneInput.setError("Invalid Phone Number");
            return false;
        } else if (!Patterns.PHONE.matcher(number).matches()) {
            phoneInput.setError("Enter Valid Number");
            return false;
        } else {
            // If all validations pass, return true to indicate that the sign-in details are valid.
            return true;
        }
    }
}