package com.mv.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;

public class ActivityWebView extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private String path, title;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initUI();

        if (getIntent().getExtras() != null) {
            path = getIntent().getExtras().getString(Constants.URL);
            title = getIntent().getExtras().getString(Constants.TITLE);
        }
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        setActionbar(title);

        webView.setWebViewClient(new myWebClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.loadUrl(path);
    }

    private void setActionbar(String title) {
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(title);

        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
        }
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (progress != null && !progress.isShowing()) {
                progress.show();
            }
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            try {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    /**
     * method to init WEBVIEW
     */
    private void initUI() {
        webView = (WebView) findViewById(R.id.web_view_container);
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.progress_please_wait));
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        progress.show();
    }
}