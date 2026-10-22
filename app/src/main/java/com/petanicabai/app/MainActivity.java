package com.petanicabai.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private WebView webView;
    private FrameLayout root;
    private LinearLayout errorLayout;
    private ProgressBar progressBar;
    private TextView errorMessage;

    private static final String APP_URL =
            "https://dinsuye-png.github.io/Petani-cabai/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        // WebView menjadi tampilan utama.
        webView = new WebView(this);

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        root.addView(webView, webParams);

        // Indikator pemuatan halaman.
        progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        FrameLayout.LayoutParams progressParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        8,
                        Gravity.TOP
                );

        root.addView(progressBar, progressParams);

        // Tampilan jika halaman utama gagal dimuat.
        errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setPadding(40, 40, 40, 40);
        errorLayout.setBackgroundColor(Color.WHITE);
        errorLayout.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("🌶️ Petani-Cabai");
        title.setTextColor(Color.rgb(40, 130, 55));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        errorMessage = new TextView(this);
        errorMessage.setText(
                "Halaman belum dapat dimuat.\n" +
                "Periksa koneksi internet dan coba lagi."
        );
        errorMessage.setTextColor(Color.DKGRAY);
        errorMessage.setTextSize(16);
        errorMessage.setGravity(Gravity.CENTER);
        errorMessage.setPadding(0, 24, 0, 24);

        Button retry = new Button(this);
        retry.setText("Coba Lagi");
        retry.setOnClickListener(v -> loadApp());

        errorLayout.addView(title);
        errorLayout.addView(errorMessage);
        errorLayout.addView(retry);

        FrameLayout.LayoutParams errorParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        root.addView(errorLayout, errorParams);

        setContentView(root);

        // Konfigurasi WebView.
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        webView.setWebChromeClient(new WebChromeClient() {

    @Override
    public boolean onConsoleMessage(
            android.webkit.ConsoleMessage consoleMessage
    ) {
        android.util.Log.e(
                "PetaniCabai",
                "Web error: " + consoleMessage.message()
                + " | Baris: " + consoleMessage.lineNumber()
                + " | Sumber: " + consoleMessage.sourceId()
        );

        return true;
    }

    @Override
    public void onProgressChanged(
            WebView view,
            int newProgress
    ) {
        super.onProgressChanged(view, newProgress);

        android.util.Log.d(
                "PetaniCabai",
                "Progress pemuatan: " + newProgress + "%"
        );
    }
});

        // Pulihkan halaman jika memungkinkan.
        // Jika pemulihan gagal, muat URL utama.
        boolean restored = false;

        if (savedInstanceState != null) {
            restored = webView.restoreState(savedInstanceState) != null;
        }

        if (!restored) {
            loadApp();
        }
    }

    private void loadApp() {
        errorLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        webView.loadUrl(APP_URL);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        errorMessage.setText(message);
        errorLayout.setVisibility(View.VISIBLE);
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
        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
