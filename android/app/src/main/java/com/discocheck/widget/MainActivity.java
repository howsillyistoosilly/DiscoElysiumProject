package com.discocheck.widget;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Random;

public final class MainActivity extends Activity {
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        WebView webView = new WebView(this);
        webView.setBackgroundColor(0xFF090909);
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new RollBridge(), "Android");
        webView.loadUrl("file:///android_asset/dashboard.html");
        setContentView(webView);
    }

    private final class RollBridge {
        @JavascriptInterface
        public String roll() {
            int die1 = random.nextInt(6) + 1;
            int die2 = random.nextInt(6) + 1;
            JSONObject result = new JSONObject();
            try {
                result.put("die1", die1);
                result.put("die2", die2);
                result.put("total", die1 + die2);
                result.put("header", "DISCO CHECK");
                result.put("quote", "The check is alive. Roll again.");
                result.put("asset_path", "Psyche/Volition.jpg");
                result.put("is_critical", die1 == die2 && (die1 == 1 || die1 == 6));
                result.put("die1_asset", "dice/die" + die1 + ".png");
                result.put("die2_asset", "dice/die" + die2 + ".png");
                result.put("accent_color", "#8170B2");
            } catch (Exception ignored) {
                return "{}";
            }
            return result.toString();
        }

        @JavascriptInterface
        public void saveLines(String json) {
            getSharedPreferences("disco-check", MODE_PRIVATE)
                    .edit()
                    .putString("lines", json)
                    .apply();
        }

        @JavascriptInterface
        public String loadLines() {
            return getSharedPreferences("disco-check", MODE_PRIVATE)
                    .getString("lines", "[]");
        }
    }
}
