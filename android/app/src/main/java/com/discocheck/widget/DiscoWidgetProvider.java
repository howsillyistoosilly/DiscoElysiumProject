package com.discocheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Home-screen widget that rolls 2d6 on tap.
 *
 * States
 * ------
 *  IDLE    — placed fresh / after reboot. Title = "DISCO CHECK", quote = "Tap to roll."
 *  RESULT  — after a successful roll. Shows skill portrait, title, dice, quote.
 *  VOLITION— tap within 10 s of last roll. Volition portrait + patience message.
 *
 * Implementation notes
 * --------------------
 *  • Single-state layout: no View.VISIBLE / View.GONE toggling (avoids RemoteViews
 *    complexity and potential Binder parcel-size issues).
 *  • PendingIntent targets this class explicitly, so it works on all launchers.
 *  • Unique URI per widget id makes each PendingIntent truly distinct.
 */
public final class DiscoWidgetProvider extends AppWidgetProvider {

    private static final String TAG         = "DiscoWidget";
    private static final String ACTION_ROLL = "com.discocheck.widget.ROLL";

    private static final long   COOLDOWN_MS       = 10_000L;
    private static final String PREFS              = "disco_widget_prefs";
    private static final String KEY_LAST_ROLL_MS  = "last_roll_ms";
    private static final String KEY_LAST_D1       = "last_d1";
    private static final String KEY_LAST_D2       = "last_d2";
    private static final String KEY_HAS_ROLLED    = "has_rolled";

    // ── 24-skill dataset ──────────────────────────────────────────────────────

    private static final String[][] SKILLS = {
        // { display name, quote, asset path, accent hex }

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
        for (int id : ids) showIdle(ctx, mgr, id);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (!ACTION_ROLL.equals(intent.getAction())) return;

        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int targetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        if (targetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            onRollTapped(ctx, mgr, targetId);
        } else {
            for (int id : mgr.getAppWidgetIds(new ComponentName(ctx, DiscoWidgetProvider.class))) {
                onRollTapped(ctx, mgr, id);
            }
        }
    }

    // ── State: IDLE ───────────────────────────────────────────────────────────

    private void showIdle(Context ctx, AppWidgetManager mgr, int id) {
        RemoteViews rv = buildBase(ctx, id);
        rv.setTextViewText(R.id.widget_title, "DISCO CHECK");
        rv.setTextColor(R.id.widget_title, 0xFFB7A0E5);   // Psyche purple hint
        rv.setTextViewText(R.id.widget_quote, "\u201CTap to roll the dice.\u201D");
        // Decorative dice in idle (both show d6 face)
        loadDie(ctx, rv, R.id.widget_die_one, 6);
        loadDie(ctx, rv, R.id.widget_die_two, 6);
        push(mgr, id, rv);
    }

    // ── State: ROLL / VOLITION ────────────────────────────────────────────────

    private void onRollTapped(Context ctx, AppWidgetManager mgr, int id) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long lastMs    = prefs.getLong(KEY_LAST_ROLL_MS, 0);
        boolean rolled = prefs.getBoolean(KEY_HAS_ROLLED, false);
        long elapsed   = System.currentTimeMillis() - lastMs;

        if (rolled && elapsed < COOLDOWN_MS) {
            showVolition(ctx, mgr, id, prefs, elapsed);
        } else {
            doRoll(ctx, mgr, id, prefs);
        }
    }

    private void showVolition(Context ctx, AppWidgetManager mgr, int id,
                              SharedPreferences prefs, long elapsed) {
        long secLeft = Math.max(1, (long) Math.ceil((COOLDOWN_MS - elapsed) / 1000.0));
        int d1 = prefs.getInt(KEY_LAST_D1, 3);
        int d2 = prefs.getInt(KEY_LAST_D2, 4);

        RemoteViews rv = buildBase(ctx, id);
        rv.setTextViewText(R.id.widget_title, "VOLITION");
        rv.setTextColor(R.id.widget_title, 0xFF8170B2);
        rv.setTextViewText(R.id.widget_quote,
                "\u201CHold it together. Take a breath. The dice aren\u2019t going anywhere"
                + " \u2014 give it " + secLeft + " more second"
                + (secLeft == 1 ? "" : "s") + " before you throw again.\u201D");
        loadPortrait(ctx, rv, "skills/Psyche/Volition.jpg");
        loadDie(ctx, rv, R.id.widget_die_one, d1);
        loadDie(ctx, rv, R.id.widget_die_two, d2);
        push(mgr, id, rv);
    }

    private void doRoll(Context ctx, AppWidgetManager mgr, int id, SharedPreferences prefs) {
        Random rng = new Random();
        int d1 = rng.nextInt(6) + 1;
        int d2 = rng.nextInt(6) + 1;

        prefs.edit()
             .putLong(KEY_LAST_ROLL_MS, System.currentTimeMillis())
             .putInt(KEY_LAST_D1, d1)
             .putInt(KEY_LAST_D2, d2)
             .putBoolean(KEY_HAS_ROLLED, true)
             .apply();

        String title, quote, asset, accent;

        if (d1 == 1 && d2 == 1) {
            title  = "CRITICAL FAILURE";
            quote  = "\u201CTwo ones stare back at you like empty eye sockets. "
                   + "The universe simply refuses to cooperate.\u201D";
            asset  = "skills/Physique/Half_Light.jpg";
            accent = "#D71921";
        } else if (d1 == 6 && d2 == 6) {
            title  = "CRITICAL SUCCESS";
            quote  = "\u201CDouble sixes. Pure, unadulterated transcendence. "
                   + "You could split an atom with your bare grin.\u201D";
            asset  = "skills/Psyche/Volition.jpg";
            accent = "#7D6BB3";
        } else {
            String[] sk = SKILLS[rng.nextInt(SKILLS.length)];
            title = sk[0]; quote = sk[1]; asset = sk[2]; accent = sk[3];
        }

        RemoteViews rv = buildBase(ctx, id);
        rv.setTextViewText(R.id.widget_title, title);
        rv.setTextColor(R.id.widget_title, parseColor(accent));
        rv.setTextViewText(R.id.widget_quote, quote);
        loadPortrait(ctx, rv, asset);
        loadDie(ctx, rv, R.id.widget_die_one, d1);
        loadDie(ctx, rv, R.id.widget_die_two, d2);
        push(mgr, id, rv);
    }

    // ── RemoteViews helpers ───────────────────────────────────────────────────

    /** Build base RemoteViews with the roll PendingIntent wired to every view. */
    private RemoteViews buildBase(Context ctx, int id) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.disco_widget);

        Intent intent = new Intent(ctx, DiscoWidgetProvider.class)
                .setAction(ACTION_ROLL)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .setData(android.net.Uri.parse("disco://roll/" + id));

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        rv.setOnClickPendingIntent(R.id.widget_root,    pi);
        rv.setOnClickPendingIntent(R.id.widget_portrait, pi);
        rv.setOnClickPendingIntent(R.id.widget_scrim,   pi);
        rv.setOnClickPendingIntent(R.id.widget_content, pi);
        rv.setOnClickPendingIntent(R.id.widget_title,   pi);
        rv.setOnClickPendingIntent(R.id.widget_quote,   pi);
        rv.setOnClickPendingIntent(R.id.widget_die_one, pi);
        rv.setOnClickPendingIntent(R.id.widget_die_two, pi);
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
        try   { return Color.parseColor(hex); }
        catch (Exception e) { return 0xFFD0D0D0; }
    }

    // ── Asset loading ─────────────────────────────────────────────────────────

    /**
     * Load a skill portrait from assets into the portrait ImageView.
     * Path is relative to the assets/ root, e.g. "skills/Physique/Half_Light.jpg".
     * We try both with and without a leading "assets/" prefix to handle staging variants.
     */
    private static void loadPortrait(Context ctx, RemoteViews rv, String path) {
        // Downscale to ~256 px wide — keeps Binder parcel well under 400 KB (RGB_565)
        loadBitmap(ctx, rv, R.id.widget_portrait, path, 256, true);
    }

    private static void loadDie(Context ctx, RemoteViews rv, int viewId, int face) {
        loadBitmap(ctx, rv, viewId, "dice/die" + face + ".png", 64, false);
    }

    private static void loadBitmap(Context ctx, RemoteViews rv, int viewId,
                                   String path, int maxPx, boolean rgb565) {
        try {
            // Determine sample size from bounds
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream s = open(ctx, path)) { BitmapFactory.decodeStream(s, null, bounds); }

            int sample = 1;
            while ((bounds.outWidth  / sample) > maxPx
                || (bounds.outHeight / sample) > maxPx) sample *= 2;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize    = Math.max(1, sample);
            opts.inPreferredConfig = rgb565
                    ? android.graphics.Bitmap.Config.RGB_565
                    : android.graphics.Bitmap.Config.ARGB_8888;

            try (InputStream s = open(ctx, path)) {
                android.graphics.Bitmap bmp = BitmapFactory.decodeStream(s, null, opts);
                if (bmp != null) rv.setImageViewBitmap(viewId, bmp);
            }
        } catch (Throwable e) {
            Log.w(TAG, "Could not load: " + path + " — " + e.getMessage());
        }
    }

    /** Try opening an asset with or without a leading "assets/" prefix. */
    private static InputStream open(Context ctx, String path) throws IOException {
        // The stageImageAssets Gradle task copies files to android/app/src/main/assets/assets/
        // so at runtime the path inside getAssets() is "assets/skills/..." NOT "skills/..."
        // We try "assets/" prefix first, then bare path as fallback.
        String[] candidates = {
            "assets/" + path,
            path
        };
        IOException last = null;
        for (String c : candidates) {
            try { return ctx.getAssets().open(c); }
            catch (IOException e) { last = e; }
        }
        throw last != null ? last : new IOException("Asset not found: " + path);
    }
}
