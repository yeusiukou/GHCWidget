package by.aleks.ghcwidget;

        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.graphics.*;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.Display;
        import android.view.WindowManager;
        import android.widget.RemoteViews;
        import by.aleks.ghcwidget.api.GitHubAPITask;
        import by.aleks.ghcwidget.data.ColorTheme;
        import by.aleks.ghcwidget.data.CommitsBase;
        import by.aleks.ghcwidget.data.Day;

        import java.util.ArrayList;

public class Widget extends AppWidgetProvider {

    private static final String debugTag = "GHCWidget";
    private RemoteViews remoteViews;

    //Parameters
    private String username;
    private int months;
    private String theme;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        setPreferences(context);
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

        Bitmap bitmap = processImage(context);
        if(bitmap!=null){
            remoteViews.setTextViewText(R.id.loadingText, "");
            remoteViews.setImageViewBitmap(R.id.commitsView, bitmap);
        }
        else remoteViews.setTextViewText(R.id.loadingText, context.getResources().getString(R.string.loading_error));


        for (int appWidgetId : appWidgetIds){
            setClickIntent(context, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                    action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {

                AppWidgetManager appWM = AppWidgetManager.getInstance(context);

                //Update the Widget.
                if (appWM != null) {
                    onUpdate(context, appWM, appWM.getAppWidgetIds(intent.getComponent()));
                }
            }

            super.onReceive(context, intent);
        }
    }

    private void setPreferences(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        username = prefs.getString("username", "xRoker");
        months = Integer.parseInt(prefs.getString("months", "5"));
        theme = prefs.getString("color_theme", ColorTheme.GITHUB);
        Log.d(debugTag, "Preferences updated: "+username+" "+months+" "+theme);

    }

    //On click open the preferences activity
    private void setClickIntent(Context context, int appWidgetId) {

        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        launchActivity.setAction("android.appwidget.action.APPWIDGET_CONFIGURE");
        launchActivity.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);

    }

    private void updateInfoBar(CommitsBase base){
        remoteViews.setTextViewText(R.id.sumContrView, base.commitsNumber()+" total");
        int streak = base.currentStreak();
        if(streak == 1){
            remoteViews.setTextViewText(R.id.streakView, streak+" day");
        } else remoteViews.setTextViewText(R.id.streakView, streak+" days");
    }


    private Bitmap processImage(Context context){
        CommitsBase base = loadData(username);
        updateInfoBar(base);
        Point size = getScreenSize(context);
        int weeks = 4*months+1;
        return createBitmap(base, weeks, size, theme);
    }


    private CommitsBase loadData(String username){
        GitHubAPITask task = new GitHubAPITask();

        try {
            return task.execute(username).get();
        }
        catch (Exception e)
        {
            task.cancel(true);
            return null;
        }
    }

    private Point getScreenSize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    private Bitmap createBitmap(CommitsBase base, int weeksNumber, Point size, String theme){
        float SPACE_RATIO = 0.1f;
        int TEXT_GRAPH_SPACE = 7;

        float side = size.x/weeksNumber * (1-SPACE_RATIO);
        float space = size.x/weeksNumber - side;
        float textSize = side*0.87f;

        int height = (int)(7*(side+space)+textSize+TEXT_GRAPH_SPACE);

        ColorTheme colorTheme = new ColorTheme();

        Bitmap bitmap = Bitmap.createBitmap(size.x, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paintText.setTextSize(textSize);
        paintText.setColor(Color.GRAY);

        if(base!=null){
            float x=0, y=textSize+TEXT_GRAPH_SPACE;
            ArrayList<ArrayList<Day>> weeks = base.getWeeks();
            for(int i = weeks.size() - weeksNumber; i<weeks.size(); i++){
                if(i%4 == 0 && i!=weeks.size()-1){
                    canvas.drawText(weeks.get(i).get(1).getMonth(), x, textSize, paintText);
                }
                for (Day day : weeks.get(i)){
                    paint.setColor(colorTheme.getColor(theme, day.getLevel()));
                    canvas.drawRect(x, y, x+side, y+side, paint);
                    y = y + side + space;
                }
                y = textSize+TEXT_GRAPH_SPACE;
                x = x + side + space;
            }
        }

        return bitmap;
    }

}