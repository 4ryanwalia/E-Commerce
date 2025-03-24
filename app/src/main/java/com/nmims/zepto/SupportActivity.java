package com.nmims.zepto;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class SupportActivity extends AppCompatActivity {

    private static final String SUPPORT_PHONE_NUMBER = "8130058680";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        Log.d("CustomerSupport", "CustomerSupportActivity launched");

        TextView supportNumberTextView = findViewById(R.id.supportNumberTextView);
        Button callButton = findViewById(R.id.callButton);

        // Display the customer support number
        supportNumberTextView.setText("Call on this number for instant support: " + SUPPORT_PHONE_NUMBER);

        callButton.setOnClickListener(v -> {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));
                startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(SupportActivity.this, "Error opening dialer", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
