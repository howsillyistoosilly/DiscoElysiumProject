package com.discocheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public final class DiscoWidgetProvider extends AppWidgetProvider {

    private static final String TAG         = "DiscoWidget";
    private static final String ACTION_ROLL = "com.discocheck.widget.ROLL";

    private static final long   COOLDOWN_MS       = 10_000L;
    private static final String PREFS_NAME         = "disco_widget_state";
    private static final String KEY_LAST_ROLL_TIME = "last_roll_time";
    private static final String KEY_LAST_D1        = "last_d1";
    private static final String KEY_LAST_D2        = "last_d2";
    private static final String KEY_HAS_ROLLED     = "has_rolled";

    // ── Full skill table (24 skills, mirrors engine/quotes.go) ───────────────

    private static final String[][] SKILLS = {
        // { name, quote, assetPath, accentColor }

        // INTELLECT
        { "LOGIC",
          "\u201CDo it for the picture puzzle. Put it all together. Solve the world.\u201D",
          "assets/skills/Intellect/Logic.jpg", "#C4A35A" },
        { "ENCYCLOPEDIA",
          "\u201CYour mangled brain would like you to know there is a boxer called Contact Mike.\u201D",
          "assets/skills/Intellect/Encyclopedia.jpg", "#C4A35A" },
        { "RHETORIC",
          "\u201CYou are The Last Communist. Now get to work, comrade.\u201D",
          "assets/skills/Intellect/Rhetoric.jpg", "#C4A35A" },
        { "DRAMA",
          "\u201CYou can\u2019t trust artists. They\u2019re nearly as bad as clowns.\u201D",
          "assets/skills/Intellect/Drama.jpg", "#C4A35A" },
        { "CONCEPTUALIZATION",
          "\u201CThe world\u2019s most precious material. You are stealing a touch. It\u2019s not yours to take.\u201D",
          "assets/skills/Intellect/Conceptualization.jpg", "#C4A35A" },
        { "VISUAL CALCULUS",
          "\u201CDeath comes faster than the realization.\u201D",
          "assets/skills/Intellect/Visual_Calculus.jpg", "#C4A35A" },

        // PSYCHE
        { "VOLITION",
          "\u201CThis is somewhere to be. Streets and sodium lights. You\u2019re still alive.\u201D",
          "assets/skills/Psyche/Volition.jpg", "#8170B2" },
        { "INLAND EMPIRE",
          "\u201CA tremendous loneliness comes over you. Everybody in the world is doing something without you.\u201D",
          "assets/skills/Psyche/Inland_Empire.jpg", "#8170B2" },
        { "EMPATHY",
          "\u201CThis is a very, very sad man who has just seen something that\u2019s made him forget his sadness.\u201D",
          "assets/skills/Psyche/Empathy.jpg", "#8170B2" },
        { "AUTHORITY",
          "\u201CAnd you? Is it an honour to work with you? Don\u2019t ask him, ask yourself.\u201D",
          "assets/skills/Psyche/Authority.jpg", "#8170B2" },
        { "SUGGESTION",
          "\u201CThis was always going to be horror. I should not have suggested it.\u201D",
          "assets/skills/Psyche/Suggestion.jpg", "#8170B2" },
        { "ESPRIT DE CORPS",
          "\u201CIf an assault were launched right now \u2014 this man would hurl himself in death\u2019s way to save you.\u201D",
          "assets/skills/Psyche/Espirit_De_Corps.jpg", "#8170B2" },

        // PHYSIQUE
        { "ENDURANCE",
          "\u201CNo time for sentimental bullshit. Just like you.\u201D",
          "assets/skills/Physique/Endurance.jpg", "#A84F63" },
        { "PAIN THRESHOLD",
          "\u201CTenderness curdled into pain. A love aborted and smothered.\u201D",
          "assets/skills/Physique/Pain_Threshold.jpg", "#A84F63" },
        { "PHYSICAL INSTRUMENT",
          "\u201CCold and heavy \u2014 like truth.\u201D",
          "assets/skills/Physique/Physical_Instrument.jpg", "#A84F63" },
        { "ELECTROCHEMISTRY",
          "\u201CThe funk soul brother at the back of his head has gone dark. Forever.\u201D",
          "assets/skills/Physique/Electrochemistry.jpg", "#A84F63" },
        { "SHIVERS",
          "\u201CI am a fragment of the world spirit, the genius loci of Revachol.\u201D",
          "assets/skills/Physique/Shivers.jpg", "#A84F63" },
        { "HALF LIGHT",
          "\u201CHe whispers with such predatory hunger it borders on longing.\u201D",
          "assets/skills/Physique/Half_Light.jpg", "#A84F63" },

        // MOTORICS
        { "HAND/EYE COORDINATION",
          "\u201CA gun is a tool for putting holes into things far away. Keep your fingers steady.\u201D",
          "assets/skills/Motorics/Hand_Eye_Coordination.jpg", "#6C9B9A" },
        { "PERCEPTION",
          "\u201CThe silence here is not empty; it is crowded with things people decided not to say.\u201D",
          "assets/skills/Motorics/Perception.jpg", "#6C9B9A" },
        { "REACTION SPEED",
          "\u201CThe second ticks by like a falling guillotine blade. Move, or be severed.\u201D",
          "assets/skills/Motorics/Reaction_Speed.jpg", "#6C9B9A" },
        { "SAVOIR FAIRE",
          "\u201CStyle is not an accident. It is a calculated declaration of war against the mundane.\u201D",
          "assets/skills/Motorics/Savoir_Faire.jpg", "#6C9B9A" },
        { "INTERFACING",
          "\u201CYou understand machines because they are the only things that tell the simple truth.\u201D",
          "assets/skills/Motorics/Interfacing.jpg", "#6C9B9A" },
        { "COMPOSURE",
          "\u201CStraighten your collar. No one can know that your heart is coming apart at the seams.\u201D",
          "assets/skills/Motorics/Composure.jpg", "#6C9B9A" },
    };

    // ── AppWidgetProvider lifecycle ───────────────────────────────────────────

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            renderIdle(context, manager, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (!ACTION_ROLL.equals(intent.getAction())) return;

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int targetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        if (targetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            handleRoll(context, manager, targetId);
        } else {
            ComponentName comp = new ComponentName(context, DiscoWidgetProvider.class);
            for (int id : manager.getAppWidgetIds(comp)) {
                handleRoll(context, manager, id);
            }
        }
    }

    // ── Idle state renderer ────────────────────────────────────────────────────
    // Shows two centred d6 icons and "TAP TO ROLL". No portrait, no skill text.

    private void renderIdle(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.disco_widget);

        // Show idle panel, hide result panels
        views.setViewVisibility(R.id.widget_idle,    View.VISIBLE);
        views.setViewVisibility(R.id.widget_content, View.GONE);
        views.setViewVisibility(R.id.widget_portrait, View.GONE);
        views.setViewVisibility(R.id.widget_scrim,   View.GONE);

        // Centred dice — show die 1 and 2 as decorative (fixed at d6)
        setAssetImage(context, views, R.id.widget_idle_die_one, "assets/dice/die6.png", false);
        setAssetImage(context, views, R.id.widget_idle_die_two, "assets/dice/die6.png", false);

        wireRollIntent(context, views, id);
        applyUpdate(manager, id, views);
    }

    // ── Roll / Volition handler ────────────────────────────────────────────────

    private void handleRoll(Context context, AppWidgetManager manager, int id) {
        android.content.SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        long lastRollTime = prefs.getLong(KEY_LAST_ROLL_TIME, 0);
        long now          = System.currentTimeMillis();
        long elapsed      = now - lastRollTime;
        boolean hasRolled = prefs.getBoolean(KEY_HAS_ROLLED, false);

        if (hasRolled && elapsed < COOLDOWN_MS) {
            // ── Cooldown path: Volition asks for patience ─────────────────────
            long remainingSec = Math.max(1, (long) Math.ceil((COOLDOWN_MS - elapsed) / 1000.0));
            int d1 = prefs.getInt(KEY_LAST_D1, 1);
            int d2 = prefs.getInt(KEY_LAST_D2, 6);

            renderResult(context, manager, id,
                    d1, d2,
                    "VOLITION",
                    "\u201CHold it together. Take a breath. The dice aren\u2019t going anywhere"
                            + " \u2014 steady your hands and give it " + remainingSec
                            + " more second" + (remainingSec == 1 ? "" : "s")
                            + " before you throw again.\u201D",
                    "assets/skills/Psyche/Volition.jpg",
                    "#8170B2");
        } else {
            // ── Fresh roll path ───────────────────────────────────────────────
            Random rng = new Random();
            int d1 = rng.nextInt(6) + 1;
            int d2 = rng.nextInt(6) + 1;

            prefs.edit()
                 .putLong(KEY_LAST_ROLL_TIME, now)
                 .putInt(KEY_LAST_D1, d1)
                 .putInt(KEY_LAST_D2, d2)
                 .putBoolean(KEY_HAS_ROLLED, true)
                 .apply();

            String title, quote, portrait, accent;

            if (d1 == 1 && d2 == 1) {
                title   = "CRITICAL FAILURE";
                quote   = "\u201CTwo ones stare back at you like empty eye sockets."
                        + " The universe simply refuses to cooperate.\u201D";
                portrait = "assets/skills/Physique/Half_Light.jpg";
                accent  = "#D71921";
            } else if (d1 == 6 && d2 == 6) {
                title   = "CRITICAL SUCCESS";
                quote   = "\u201CDouble sixes. Pure, unadulterated transcendence."
                        + " You could split an atom with your bare grin.\u201D";
                portrait = "assets/skills/Psyche/Volition.jpg";
                accent  = "#7D6BB3";
            } else {
                String[] skill = SKILLS[rng.nextInt(SKILLS.length)];
                title   = skill[0];
                quote   = skill[1];
                portrait = skill[2];
                accent  = skill[3];
            }

            renderResult(context, manager, id, d1, d2, title, quote, portrait, accent);
        }
    }

    // ── Result state renderer ──────────────────────────────────────────────────
    // Shows skill portrait, gradient scrim, title, dice, and quote at the bottom.

    private void renderResult(Context context, AppWidgetManager manager, int id,
                              int d1, int d2,
                              String title, String quote,
                              String portrait, String accent) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.disco_widget);

        // Hide idle, show result panels
        views.setViewVisibility(R.id.widget_idle,    View.GONE);
        views.setViewVisibility(R.id.widget_portrait, View.VISIBLE);
        views.setViewVisibility(R.id.widget_scrim,   View.VISIBLE);
        views.setViewVisibility(R.id.widget_content, View.VISIBLE);

        // Skill portrait
        setAssetImage(context, views, R.id.widget_portrait, portrait, true);

        // Title + accent colour
        views.setTextViewText(R.id.widget_title, title);
        views.setTextColor(R.id.widget_title, accentColor(accent));

        // Quote
        views.setTextViewText(R.id.widget_quote, quote);

        // Dice (result-state, small, bottom-right)
        setAssetImage(context, views, R.id.widget_die_one, "assets/dice/die" + d1 + ".png", false);
        setAssetImage(context, views, R.id.widget_die_two, "assets/dice/die" + d2 + ".png", false);

        wireRollIntent(context, views, id);
        applyUpdate(manager, id, views);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Attaches the ROLL broadcast PendingIntent to every tappable view in the layout. */
    private void wireRollIntent(Context context, RemoteViews views, int id) {
        Intent rollIntent = new Intent(context, DiscoWidgetProvider.class)
                .setAction(ACTION_ROLL)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                // Unique URI ensures distinct PendingIntents per widget instance
                .setData(android.net.Uri.parse("disco://widget/roll/" + id));

        PendingIntent pending = PendingIntent.getBroadcast(
                context, id, rollIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Wire every view so tapping anywhere on the card works
        views.setOnClickPendingIntent(R.id.widget_root,         pending);
        views.setOnClickPendingIntent(R.id.widget_idle,         pending);
        views.setOnClickPendingIntent(R.id.widget_idle_die_one, pending);
        views.setOnClickPendingIntent(R.id.widget_idle_die_two, pending);
        views.setOnClickPendingIntent(R.id.widget_idle_hint,    pending);
        views.setOnClickPendingIntent(R.id.widget_portrait,     pending);
        views.setOnClickPendingIntent(R.id.widget_scrim,        pending);
        views.setOnClickPendingIntent(R.id.widget_content,      pending);
        views.setOnClickPendingIntent(R.id.widget_title,        pending);
        views.setOnClickPendingIntent(R.id.widget_quote,        pending);
        views.setOnClickPendingIntent(R.id.widget_die_one,      pending);
        views.setOnClickPendingIntent(R.id.widget_die_two,      pending);
    }

    private void applyUpdate(AppWidgetManager manager, int id, RemoteViews views) {
        try {
            manager.updateAppWidget(id, views);
        } catch (Throwable e) {
            Log.e(TAG, "Failed to update widget " + id, e);
        }
    }

    private static int accentColor(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException ignored) {
            return 0xFFD0D0D0;
        }
    }

    // ── Asset loader ──────────────────────────────────────────────────────────

    private static InputStream openAssetStream(Context context, String path) throws IOException {
        try {
            return context.getAssets().open(path);
        } catch (IOException e) {
            // Try both with and without the leading "assets/" prefix
            if (path.startsWith("assets/")) {
                return context.getAssets().open(path.substring("assets/".length()));
            } else {
                return context.getAssets().open("assets/" + path);
            }
        }
    }

    private static void setAssetImage(
            Context context, RemoteViews views, int viewId,
            String assetPath, boolean isPortrait) {
        try {
            int maxDim = isPortrait ? 300 : 96;

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = openAssetStream(context, assetPath)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }

            int sample = 1;
            while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, sample);
            if (isPortrait) {
                opts.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565;
            }

            try (InputStream in = openAssetStream(context, assetPath)) {
                android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
                if (bitmap != null) {
                    views.setImageViewBitmap(viewId, bitmap);
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Unable to load asset: " + assetPath, e);
        }
    }
}
