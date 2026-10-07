package com.discocheck.widget;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.util.Random;

public final class MainActivity extends Activity {

    private final Random rng = new Random();

    // ── Full skill table (mirrors engine/quotes.go) ───────────────────────────
    // Each entry: { header, quote, assetPath (relative to skills/), accentColor }

    private static final String[][] SKILLS = {
        { "LOGIC",
          "Do it for the picture puzzle. Put it all together. Solve the world. One conversation at a time.",
          "Intellect/Logic.jpg", "#C4A35A" },
        { "ENCYCLOPEDIA",
          "Your mangled brain would like you to know there is a boxer called Contact Mike.",
          "Intellect/Encyclopedia.jpg", "#C4A35A" },
        { "RHETORIC",
          "Yes! Abject failure. Total, irreversible defeat on all fronts! Absolutely vanquished, beaten, curb-stomped and pissed on \u2014 until *you* came along! You are The Last Communist. Now get to work, comrade.",
          "Intellect/Rhetoric.jpg", "#C4A35A" },
        { "DRAMA",
          "Everyone knows you can\u2019t trust artists. They\u2019re nearly as bad as clowns.",
          "Intellect/Drama.jpg", "#C4A35A" },
        { "CONCEPTUALIZATION",
          "The world\u2019s most precious material, reserved for those she lets close enough to feel it. You are stealing a touch. It\u2019s not yours to take.",
          "Intellect/Conceptualization.jpg", "#C4A35A" },
        { "VISUAL CALCULUS",
          "The man does not know the bullet has entered his brain. He never will. Death comes faster than the realization.",
          "Intellect/Visual_Calculus.jpg", "#C4A35A" },
        { "VOLITION",
          "No. This is somewhere to be. This is all you have, but it\u2019s still something. Streets and sodium lights. The sky, the world. You\u2019re still alive.",
          "Psyche/Volition.jpg", "#8170B2" },
        { "INLAND EMPIRE",
          "A tremendous loneliness comes over you. Everybody in the world is doing something without you.",
          "Psyche/Inland_Empire.jpg", "#8170B2" },
        { "EMPATHY",
          "This is a very, very sad man who has just seen something that\u2019s made him forget his sadness.",
          "Psyche/Empathy.jpg", "#8170B2" },
        { "AUTHORITY",
          "And you? Is it an honour to work with you? Don\u2019t ask him, ask yourself.",
          "Psyche/Authority.jpg", "#8170B2" },
        { "SUGGESTION",
          "This was not about failure or success. This was always going to be horror. I should not have suggested it, and you should not have listened to me.",
          "Psyche/Suggestion.jpg", "#8170B2" },
        { "ESPRIT DE CORPS",
          "If an assault were launched on this building right now \u2014 this man would hurl himself in death\u2019s way to save you.",
          "Psyche/Espirit_De_Corps.jpg", "#8170B2" },
        { "ENDURANCE",
          "Think about the seagull\u2019s story. It\u2019s one of endurance and adaptation. No time for sentimental bullshit. Just like you.",
          "Physique/Endurance.jpg", "#A84F63" },
        { "PAIN THRESHOLD",
          "There\u2019s tenderness in the carabineer\u2019s look. Tenderness that\u2019s curdled into pain or something darker.",
          "Physique/Pain_Threshold.jpg", "#A84F63" },
        { "PHYSICAL INSTRUMENT",
          "Cold and heavy \u2014 like truth.",
          "Physique/Physical_Instrument.jpg", "#A84F63" },
        { "ELECTROCHEMISTRY",
          "The funk soul brother at the back of his head has gone dark. Forever.",
          "Physique/Electrochemistry.jpg", "#A84F63" },
        { "SHIVERS",
          "I am a fragment of the world spirit, the genius loci of Revachol. I\u2019ve seen you with her \u2014 and I\u2019ve seen you without her.",
          "Physique/Shivers.jpg", "#A84F63" },
        { "HALF LIGHT",
          "He whispers with such predatory hunger it borders on longing.",
          "Physique/Half_Light.jpg", "#A84F63" },
        { "HAND/EYE COORDINATION",
          "A gun is a tool for putting holes into things far away. Sometimes those things are human heads. Keep your fingers steady.",
          "Motorics/Hand_Eye_Coordination.jpg", "#6C9B9A" },
        { "PERCEPTION",
          "Listen closely. The silence here is not empty; it is crowded with things people decided not to say.",
          "Motorics/Perception.jpg", "#6C9B9A" },
        { "REACTION SPEED",
          "The second ticks by like a falling guillotine blade. Move, or be severed.",
          "Motorics/Reaction_Speed.jpg", "#6C9B9A" },
        { "SAVOIR FAIRE",
          "Style is not an accident. It is a calculated declaration of war against the mundane.",
          "Motorics/Savoir_Faire.jpg", "#6C9B9A" },
        { "INTERFACING",
          "The needle drops into the groove. You understand machines because they are the only things that tell the simple truth.",
          "Motorics/Interfacing.jpg", "#6C9B9A" },
        { "COMPOSURE",
          "Straighten your collar. Swallow the bile. No one can know that your heart is coming apart at the seams.",
          "Motorics/Composure.jpg", "#6C9B9A" },
    };

    // ─────────────────────────────────────────────────────────────────────────

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

    // ── JS bridge ─────────────────────────────────────────────────────────────

    private final class RollBridge {
        @JavascriptInterface
        public String roll() {
            int d1 = rng.nextInt(6) + 1;
            int d2 = rng.nextInt(6) + 1;
            boolean isCritical = (d1 == d2) && (d1 == 1 || d1 == 6);

            String header, quote, assetPath, accent;

            if (d1 == 1 && d2 == 1) {
                header    = "CRITICAL FAILURE";
                quote     = "Two ones stare back at you like empty eye sockets. The universe simply refuses to cooperate.";
                assetPath = "Physique/Half_Light.jpg";
                accent    = "#D71921";
            } else if (d1 == 6 && d2 == 6) {
                header    = "CRITICAL SUCCESS";
                quote     = "Double sixes. Pure, unadulterated transcendence. You could split an atom with your bare grin.";
                assetPath = "Psyche/Volition.jpg";
                accent    = "#7D6BB3";
            } else {
                String[] skill = SKILLS[rng.nextInt(SKILLS.length)];
                header    = skill[0];
                quote     = skill[1];
                assetPath = skill[2];
                accent    = skill[3];
            }

            JSONObject result = new JSONObject();
            try {
                result.put("die1",         d1);
                result.put("die2",         d2);
                result.put("total",        d1 + d2);
                result.put("header",       header);
                result.put("quote",        quote);
                result.put("asset_path",   assetPath);
                result.put("is_critical",  isCritical);
                result.put("die1_asset",   "dice/die" + d1 + ".png");
                result.put("die2_asset",   "dice/die" + d2 + ".png");
                result.put("accent_color", accent);
            } catch (Exception ignored) {
                return "{}";
            }
            return result.toString();
        }
    }
}
