package com.petanicabai.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends Activity {

private WebView webView;
private LinearLayout errorLayout;

private static final String APP_URL =
        "https://dinsuye-png.github.io/Petani-cabai/";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(Color.WHITE);

    webView = new WebView(this);

    errorLayout = new LinearLayout(this);
    errorLayout.setOrientation(LinearLayout.VERTICAL);
    errorLayout.setGravity(Gravity.CENTER);
    errorLayout.setPadding(32, 32, 32, 32);
    errorLayout.setVisibility(android.view.View.GONE);

    TextView message = new TextView(this);
    message.setText(
            "Petani-Cabai belum dapat dimuat.\n\n" +
            "Periksa koneksi internet, lalu coba lagi."
    );
    message.setTextColor(Color.DKGRAY);
    message.setTextSize(16);
    message.setGravity(Gravity.CENTER);

    Button retry = new Button(this);
    retry.setText("Coba Lagi");
    retry.setOnClickListener(v -> loadApp());

    errorLayout.addView(message);
    errorLayout.addView(retry);

    root.addView(webView, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1
    ));

    root.addView(errorLayout, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
    ));

    setContentView(root);

    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setLoadWithOverviewMode(true);
    settings.setUseWideViewPort(true);
    settings.setSupportZoom(false);

    webView.setWebChromeClient(new WebChromeClient());

    webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (url != null && url.startsWith("https://")) {
                errorLayout.setVisibility(android.view.View.GONE);
                webView.setVisibility(android.view.View.VISIBLE);
            }
        }

        @Override
        public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
        ) {
            super.onReceivedError(view, request, error);

            if (request.isForMainFrame()) {
                webView.setVisibility(android.view.View.GONE);
                errorLayout.setVisibility(android.view.View.VISIBLE);
            }
        }
    });

    if (savedInstanceState != null) {
        webView.restoreState(savedInstanceState);
    } else {
        loadApp();
    }
}

private void loadApp() {
    errorLayout.setVisibility(android.view.View.GONE);
    webView.setVisibility(android.view.View.VISIBLE);
    webView.loadUrl(APP_URL);
}

@Override
public void onBackPressed() {
    if (webView != null && webView.canGoBack()) {
        webView.goBack();
    } else {
        super.onBackPressed();
    }
}

@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (webView != null) {
        webView.saveState(outState);
    }
}

@Override
protected void onDestroy() {
    if (webView != null) {
        webView.destroy();
        webView = null;
    }

    super.onDestroy();
}

}
