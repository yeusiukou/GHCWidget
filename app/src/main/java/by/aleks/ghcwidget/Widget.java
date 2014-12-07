package by.aleks.ghcwidget;

        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        setClickIntent(context);
    }


    private void setClickIntent(Context context) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
        // When we click the widget, we want to open our main activity.
        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);
        remoteViews.setOnClickPendingIntent(R.id.widgetTextView, pendingIntent);;

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }

}