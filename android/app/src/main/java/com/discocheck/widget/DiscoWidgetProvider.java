package com.discocheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public final class DiscoWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_ROLL = "com.discocheck.widget.ROLL";
    private static final String[] TITLES = {
            "VOLITION", "RHETORIC", "LOGIC", "EMPATHY", "SHIVERS", "PERCEPTION"
    };
    private static final String[] QUOTES = {
            "You are still alive.",
            "Yes! Total, irreversible defeat on all fronts!",
            "Solve the world. One conversation at a time.",
            "This is a very, very sad man.",
            "I am a fragment of the world spirit.",
            "The silence is crowded with things left unsaid."
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_ROLL.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, DiscoWidgetProvider.class);
            for (int id : manager.getAppWidgetIds(component)) update(context, manager, id);
        }
    }

    private void update(Context context, AppWidgetManager manager, int id) {
        String[] titles = TITLES;
        String[] quotes = QUOTES;
        String saved = context.getSharedPreferences("disco-check", Context.MODE_PRIVATE)
                .getString("lines", "");
        try {
            JSONArray lines = new JSONArray(saved);
            if (lines.length() > 0) {
                titles = new String[lines.length()];
                quotes = new String[lines.length()];
                for (int i = 0; i < lines.length(); i++) {
                    JSONObject line = lines.getJSONObject(i);
                    titles[i] = line.optString("title", "DISCO CHECK");
                    quotes[i] = line.optString("quote", "Tap to roll.");
                }
            }
        } catch (org.json.JSONException error) {
            Log.w("DiscoWidgetProvider", "Saved dashboard lines are invalid; using defaults", error);
        }
        int index = new Random().nextInt(titles.length);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.disco_widget);
        views.setTextViewText(R.id.widget_title, titles[index]);
        views.setTextViewText(R.id.widget_quote, "“" + quotes[index] + "”");
        Intent intent = new Intent(context, DiscoWidgetProvider.class).setAction(ACTION_ROLL);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        manager.updateAppWidget(id, views);
    }
}
