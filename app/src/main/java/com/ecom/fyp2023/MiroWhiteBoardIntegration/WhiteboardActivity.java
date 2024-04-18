package com.ecom.fyp2023.MiroWhiteBoardIntegration;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.ecom.fyp2023.R;

public class WhiteboardActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whiteboard);

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String whiteboardUrl = getIntent().getStringExtra("whiteboardUrl");
        assert whiteboardUrl != null;
        webView.loadUrl(whiteboardUrl);
    }
}