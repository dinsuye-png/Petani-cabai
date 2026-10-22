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
import android.webkit.ConsoleMessage;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private static final String TAG = "PetaniCabai";

    private static final String APP_URL =
            "https://dinsuye-png.github.io/Petani-cabai/";

    private WebView webView;
    private FrameLayout root;
    private LinearLayout errorLayout;
    private ProgressBar progressBar;
    private TextView errorMessage;

    private boolean mainPageFailed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(this);

        root.addView(
                webView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        progressBar.setMax(100);
        progressBar.setProgress(0);

        FrameLayout.LayoutParams progressParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        8,
                        Gravity.TOP
                );

        root.addView(progressBar, progressParams);

        errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setGravity(Gravity.CENTER);
        errorLayout.setPadding(40, 40, 40, 40);
        errorLayout.setBackgroundColor(Color.WHITE);
        errorLayout.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("Petani-Cabai");
        title.setTextColor(Color.rgb(40, 130, 55));
        title.setTextSize(27);
        title.setGravity(Gravity.CENTER);

        errorMessage = new TextView(this);
        errorMessage.setTextColor(Color.DKGRAY);
        errorMessage.setTextSize(16);
        errorMessage.setGravity(Gravity.CENTER);
        errorMessage.setPadding(0, 24, 0, 24);
        errorMessage.setText(
                "Halaman belum dapat dimuat.\n" +
                "Periksa koneksi internet dan coba lagi."
        );

        Button retry = new Button(this);
        retry.setText("Coba Lagi");
        retry.setOnClickListener(v -> loadApp());

        errorLayout.addView(title);
        errorLayout.addView(errorMessage);
        errorLayout.addView(retry);

        root.addView(
                errorLayout,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        // Catat error JavaScript dan progres pemuatan.
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(
                    ConsoleMessage message
            ) {
                Log.e(
                        TAG,
                        "JavaScript: " + message.message()
                        + " | Baris: " + message.lineNumber()
                        + " | Sumber: " + message.sourceId()
                );

                return true;
            }

            @Override
            public void onProgressChanged(
                    WebView view,
                    int newProgress
            ) {
                progressBar.setProgress(newProgress);

                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // PENTING: pasang WebViewClient.
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    android.graphics.Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);

                Log.d(TAG, "Mulai memuat: " + url);

                mainPageFailed = false;

                errorLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                Log.d(TAG, "Selesai memuat: " + url);

                progressBar.setVisibility(View.GONE);

                if (!mainPageFailed) {
                    webView.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
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
                    Log.e(
                            TAG,
                            "Gagal memuat halaman: "
                            + error.getDescription()
                            + " | Kode: "
                            + error.getErrorCode()
                    );

                    showError(
                            "Halaman gagal dimuat.\n\n"
                            + error.getDescription()
                            + "\n\nPeriksa koneksi internet."
                    );
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse response
            ) {
                super.onReceivedHttpError(
                        view, request, response
                );

                if (request.isForMainFrame()) {
                    int status = response.getStatusCode();

                    Log.e(
                            TAG,
                            "HTTP error: " + status
                            + " | URL: " + request.getUrl()
                    );

                    if (status >= 400) {
                        showError(
                                "Server gagal menampilkan halaman.\n\n"
                                + "Kode HTTP: " + status
                                + "\nSilakan coba lagi."
                        );
                    }
                }
            }
        });

        // Pulihkan halaman hanya jika benar-benar berhasil.
        boolean restored = false;

        if (savedInstanceState != null) {
            restored =
                    webView.restoreState(savedInstanceState) != null;
        }

        if (!restored) {
            loadApp();
        }
    }

    private void loadApp() {
        mainPageFailed = false;

        errorLayout.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        Log.d(TAG, "Memuat URL aplikasi: " + APP_URL);

        webView.loadUrl(APP_URL);
    }

    private void showError(String message) {
        mainPageFailed = true;

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
