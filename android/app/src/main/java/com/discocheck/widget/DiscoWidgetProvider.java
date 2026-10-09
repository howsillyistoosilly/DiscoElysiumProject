package com.discocheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Home-screen widget for Disco Elysium checks.
 *
 * Visual States:
 *   1. IDLE     — Centered 2d6 dice + "DISCO CHECK" + "TAP TO ROLL".
 *   2. RESULT   — Skill portrait + roll results + quote.
 *   3. VOLITION — Rapid-tap cooldown (10s) reminder with live seconds remaining.
 */
public final class DiscoWidgetProvider extends AppWidgetProvider {

    private static final String TAG         = "DiscoWidget";
    public static final String ACTION_ROLL  = "com.discocheck.widget.ROLL";

    private static final long   COOLDOWN_MS       = 10_000L;
    private static final String PREFS              = "disco_widget_prefs";
    private static final String KEY_HAS_ROLLED    = "has_rolled";
    private static final String KEY_LAST_ROLL_MS  = "last_roll_ms";
    private static final String KEY_LAST_D1       = "last_d1";
    private static final String KEY_LAST_D2       = "last_d2";
    private static final String KEY_LAST_TITLE    = "last_title";
    private static final String KEY_LAST_QUOTE    = "last_quote";
    private static final String KEY_LAST_ASSET    = "last_asset";
    private static final String KEY_LAST_ACCENT   = "last_accent";

    private static final int[] DIE_DRAWABLES = {
        R.drawable.die1,
        R.drawable.die2,
        R.drawable.die3,
        R.drawable.die4,
        R.drawable.die5,
        R.drawable.die6
    };

    // ── 24-skill dataset (aligned with backend & engine) ──────────────────────
    private static final String[][] SKILLS = {
        // INTELLECT
        { "LOGIC",
          "\u201CDo it for the picture puzzle. Put it all together. Solve the world.\u201D",
          "skills/Intellect/Logic.jpg", "#C4A35A" },
        { "ENCYCLOPEDIA",
          "\u201CYour mangled brain would like you to know there is a boxer called Contact Mike.\u201D",
          "skills/Intellect/Encyclopedia.jpg", "#C4A35A" },
        { "RHETORIC",
          "\u201CYou are The Last Communist. Now get to work, comrade.\u201D",
          "skills/Intellect/Rhetoric.jpg", "#C4A35A" },
        { "DRAMA",
          "\u201CYou can\u2019t trust artists. They\u2019re nearly as bad as clowns.\u201D",
          "skills/Intellect/Drama.jpg", "#C4A35A" },
        { "CONCEPTUALIZATION",
          "\u201CThe world\u2019s most precious material. You are stealing a touch. It\u2019s not yours to take.\u201D",
          "skills/Intellect/Conceptualization.jpg", "#C4A35A" },
        { "VISUAL CALCULUS",
          "\u201CDeath comes faster than the realization.\u201D",
          "skills/Intellect/Visual_Calculus.jpg", "#C4A35A" },

        // PSYCHE
        { "VOLITION",
          "\u201CThis is somewhere to be. Streets and sodium lights. You\u2019re still alive.\u201D",
          "skills/Psyche/Volition.jpg", "#8170B2" },
        { "INLAND EMPIRE",
          "\u201CA tremendous loneliness comes over you. Everybody in the world is doing something without you.\u201D",
          "skills/Psyche/Inland_Empire.jpg", "#8170B2" },
        { "EMPATHY",
          "\u201CThis is a very, very sad man who has just seen something that\u2019s made him forget his sadness.\u201D",
          "skills/Psyche/Empathy.jpg", "#8170B2" },
        { "AUTHORITY",
          "\u201CAnd you? Is it an honour to work with you? Don\u2019t ask him, ask yourself.\u201D",
          "skills/Psyche/Authority.jpg", "#8170B2" },
        { "SUGGESTION",
          "\u201CThis was always going to be horror. I should not have suggested it.\u201D",
          "skills/Psyche/Suggestion.jpg", "#8170B2" },
        { "ESPRIT DE CORPS",
          "\u201CIf an assault were launched right now \u2014 this man would hurl himself in death\u2019s way to save you.\u201D",
          "skills/Psyche/Espirit_De_Corps.jpg", "#8170B2" },

        // PHYSIQUE
        { "ENDURANCE",
          "\u201CNo time for sentimental bullshit. Just like you.\u201D",
          "skills/Physique/Endurance.jpg", "#A84F63" },
        { "PAIN THRESHOLD",
          "\u201CTenderness curdled into pain. A love aborted and smothered.\u201D",
          "skills/Physique/Pain_Threshold.jpg", "#A84F63" },
        { "PHYSICAL INSTRUMENT",
          "\u201CCold and heavy \u2014 like truth.\u201D",
          "skills/Physique/Physical_Instrument.jpg", "#A84F63" },
        { "ELECTROCHEMISTRY",
          "\u201CThe funk soul brother at the back of his head has gone dark. Forever.\u201D",
          "skills/Physique/Electrochemistry.jpg", "#A84F63" },
        { "SHIVERS",
          "\u201CI am a fragment of the world spirit, the genius loci of Revachol.\u201D",
          "skills/Physique/Shivers.jpg", "#A84F63" },
        { "HALF LIGHT",
          "\u201CHe whispers with such predatory hunger it borders on longing.\u201D",
          "skills/Physique/Half_Light.jpg", "#A84F63" },

        // MOTORICS
        { "HAND/EYE COORDINATION",
          "\u201CA gun is a tool for putting holes into things far away. Keep your fingers steady.\u201D",
          "skills/Motorics/Hand_Eye_Coordination.jpg", "#6C9B9A" },
        { "PERCEPTION",
          "\u201CThe silence here is not empty; it is crowded with things people decided not to say.\u201D",
          "skills/Motorics/Perception.jpg", "#6C9B9A" },
        { "REACTION SPEED",
          "\u201CThe second ticks by like a falling guillotine blade. Move, or be severed.\u201D",
          "skills/Motorics/Reaction_Speed.jpg", "#6C9B9A" },
        { "SAVOIR FAIRE",
          "\u201CStyle is not an accident. It is a calculated declaration of war against the mundane.\u201D",
          "skills/Motorics/Savoir_Faire.jpg", "#6C9B9A" },
        { "INTERFACING",
          "\u201CYou understand machines because they are the only things that tell the simple truth.\u201D",
          "skills/Motorics/Interfacing.jpg", "#6C9B9A" },
        { "COMPOSURE",
          "\u201CStraighten your collar. No one can know that your heart is coming apart at the seams.\u201D",
          "skills/Motorics/Composure.jpg", "#6C9B9A" },
    };

    // ── AppWidgetProvider lifecycle ───────────────────────────────────────────

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) {
            restoreOrShowIdle(ctx, mgr, id);
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        String action = intent.getAction();
        if (ACTION_ROLL.equals(action)) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
            int targetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (targetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                onRollTapped(ctx, mgr, targetId);
            } else {
                ComponentName cn = new ComponentName(ctx, DiscoWidgetProvider.class);
                int[] ids = mgr.getAppWidgetIds(cn);
                if (ids != null) {
                    for (int id : ids) {
                        onRollTapped(ctx, mgr, id);
                    }
                }
            }
        }
    }

    // ── State handling ────────────────────────────────────────────────────────

    private void restoreOrShowIdle(Context ctx, AppWidgetManager mgr, int id) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean hasRolled = prefs.getBoolean(KEY_HAS_ROLLED, false);
        if (!hasRolled) {
            showIdle(ctx, mgr, id);
        } else {
            String title = prefs.getString(KEY_LAST_TITLE, "DISCO CHECK");
            String quote = prefs.getString(KEY_LAST_QUOTE, "\u201CTap to roll.\u201D");
            String asset = prefs.getString(KEY_LAST_ASSET, "skills/Psyche/Volition.jpg");
            String accent = prefs.getString(KEY_LAST_ACCENT, "#8170B2");
            int d1 = prefs.getInt(KEY_LAST_D1, 6);
            int d2 = prefs.getInt(KEY_LAST_D2, 6);
            renderResultState(ctx, mgr, id, title, quote, asset, accent, d1, d2);
        }
    }

    private void showIdle(Context ctx, AppWidgetManager mgr, int id) {
        RemoteViews rv = buildBase(ctx, id);
        rv.setViewVisibility(R.id.widget_idle_layout, View.VISIBLE);
        rv.setViewVisibility(R.id.widget_result_layout, View.GONE);
        push(mgr, id, rv);
    }

    private void onRollTapped(Context ctx, AppWidgetManager mgr, int id) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long lastMs    = prefs.getLong(KEY_LAST_ROLL_MS, 0);
        boolean rolled = prefs.getBoolean(KEY_HAS_ROLLED, false);
        long elapsed   = System.currentTimeMillis() - lastMs;

        if (rolled && elapsed < COOLDOWN_MS) {
            showVolitionCooldown(ctx, mgr, id, prefs, elapsed);
        } else {
            doRoll(ctx, mgr, id, prefs);
        }
    }

    private void showVolitionCooldown(Context ctx, AppWidgetManager mgr, int id,
                                      SharedPreferences prefs, long elapsed) {
        long secLeft = Math.max(1, (long) Math.ceil((COOLDOWN_MS - elapsed) / 1000.0));
        int d1 = prefs.getInt(KEY_LAST_D1, 3);
        int d2 = prefs.getInt(KEY_LAST_D2, 4);

        String title = "VOLITION";
        String quote = "\u201CHold it together. Take a breath. The dice aren\u2019t going anywhere"
                + " \u2014 give it " + secLeft + " more second"
                + (secLeft == 1 ? "" : "s") + " before you throw again.\u201D";
        String asset = "skills/Psyche/Volition.jpg";
        String accent = "#8170B2";

        renderResultState(ctx, mgr, id, title, quote, asset, accent, d1, d2);
    }

    private void doRoll(Context ctx, AppWidgetManager mgr, int id, SharedPreferences prefs) {
        Random rng = new Random();
        int d1 = rng.nextInt(6) + 1;
        int d2 = rng.nextInt(6) + 1;

        String title, quote, asset, accent;

        if (d1 == 1 && d2 == 1) {
            title  = "CRITICAL FAILURE";
            quote  = "\u201CTwo ones stare back at you like empty eye sockets. The universe simply refuses to cooperate.\u201D";
            asset  = "skills/Physique/Half_Light.jpg";
            accent = "#D71921";
        } else if (d1 == 6 && d2 == 6) {
            title  = "CRITICAL SUCCESS";
            quote  = "\u201CDouble sixes. Pure, unadulterated transcendence. You could split an atom with your bare grin.\u201D";
            asset  = "skills/Psyche/Volition.jpg";
            accent = "#7D6BB3";
        } else {
            String[] sk = SKILLS[rng.nextInt(SKILLS.length)];
            title  = sk[0];
            quote  = sk[1];
            asset  = sk[2];
            accent = sk[3];
        }

        prefs.edit()
             .putBoolean(KEY_HAS_ROLLED, true)
             .putLong(KEY_LAST_ROLL_MS, System.currentTimeMillis())
             .putInt(KEY_LAST_D1, d1)
             .putInt(KEY_LAST_D2, d2)
             .putString(KEY_LAST_TITLE, title)
             .putString(KEY_LAST_QUOTE, quote)
             .putString(KEY_LAST_ASSET, asset)
             .putString(KEY_LAST_ACCENT, accent)
             .apply();

        renderResultState(ctx, mgr, id, title, quote, asset, accent, d1, d2);
    }

    private void renderResultState(Context ctx, AppWidgetManager mgr, int id,
                                   String title, String quote, String asset,
                                   String accent, int d1, int d2) {
        RemoteViews rv = buildBase(ctx, id);
        rv.setViewVisibility(R.id.widget_idle_layout, View.GONE);
        rv.setViewVisibility(R.id.widget_result_layout, View.VISIBLE);

        rv.setTextViewText(R.id.widget_title, title);
        rv.setTextColor(R.id.widget_title, parseColor(accent));
        rv.setTextViewText(R.id.widget_quote, quote);

        // Safe integer resource assignment — zero Binder memory overhead
        int d1Res = DIE_DRAWABLES[Math.max(0, Math.min(5, d1 - 1))];
        int d2Res = DIE_DRAWABLES[Math.max(0, Math.min(5, d2 - 1))];
        rv.setImageViewResource(R.id.widget_die_one, d1Res);
        rv.setImageViewResource(R.id.widget_die_two, d2Res);

        // Downscaled portrait bitmap
        loadPortrait(ctx, rv, asset);

        push(mgr, id, rv);
    }

    // ── Base RemoteViews & Click Intent ───────────────────────────────────────

    private RemoteViews buildBase(Context ctx, int id) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.disco_widget);

        // Explicit broadcast intent targeting DiscoWidgetProvider directly
        Intent intent = new Intent(ctx, DiscoWidgetProvider.class);
        intent.setAction(ACTION_ROLL);
        intent.setPackage(ctx.getPackageName());
        intent.setComponent(new ComponentName(ctx, DiscoWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Attach to root and major layout containers for 100% reliable tap coverage
        rv.setOnClickPendingIntent(R.id.widget_root, pi);
        rv.setOnClickPendingIntent(R.id.widget_idle_layout, pi);
        rv.setOnClickPendingIntent(R.id.widget_result_layout, pi);
        rv.setOnClickPendingIntent(R.id.widget_content, pi);
        rv.setOnClickPendingIntent(R.id.widget_portrait, pi);
        rv.setOnClickPendingIntent(R.id.widget_scrim, pi);

        return rv;
    }

    private void push(AppWidgetManager mgr, int id, RemoteViews rv) {
        try {
            mgr.updateAppWidget(id, rv);
        } catch (Throwable e) {
            Log.e(TAG, "updateAppWidget failed for id=" + id, e);
        }
    }

    private static int parseColor(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return 0xFFB7A0E5;
        }
    }

    // ── Asset loading ─────────────────────────────────────────────────────────

    private static void loadPortrait(Context ctx, RemoteViews rv, String path) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream s = openAsset(ctx, path)) {
                BitmapFactory.decodeStream(s, null, bounds);
            }

            int maxDim = 240;
            int sample = 1;
            while ((bounds.outWidth / sample) > maxDim || (bounds.outHeight / sample) > maxDim) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, sample);
            opts.inPreferredConfig = Bitmap.Config.RGB_565;

            try (InputStream s = openAsset(ctx, path)) {
                Bitmap bmp = BitmapFactory.decodeStream(s, null, opts);
                if (bmp != null) {
                    rv.setImageViewBitmap(R.id.widget_portrait, bmp);
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Could not load portrait asset: " + path + " (" + e.getMessage() + ")");
        }
    }

    private static InputStream openAsset(Context ctx, String path) throws IOException {
        String clean = path.startsWith("/") ? path.substring(1) : path;
        String[] candidates = {
            clean,
            "assets/" + clean,
            clean.startsWith("assets/") ? clean.substring(7) : clean,
            "assets/assets/" + clean
        };

        IOException last = null;
        for (String c : candidates) {
            try {
                return ctx.getAssets().open(c);
            } catch (IOException e) {
                last = e;
            }
        }
        throw (last != null) ? last : new IOException("Asset not found: " + path);
    }
}
