package com.discocheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

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
        int index = new Random().nextInt(TITLES.length);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.disco_widget);
        views.setTextViewText(R.id.widget_title, TITLES[index]);
        views.setTextViewText(R.id.widget_quote, "“" + QUOTES[index] + "”");
        Intent intent = new Intent(context, DiscoWidgetProvider.class).setAction(ACTION_ROLL);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        manager.updateAppWidget(id, views);
    }
}
