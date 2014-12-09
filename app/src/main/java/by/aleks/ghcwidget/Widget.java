package by.aleks.ghcwidget;

        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.graphics.*;
        import android.view.Display;
        import android.view.WindowManager;
        import android.widget.RemoteViews;
        import by.aleks.ghcwidget.api.GitHubAPITask;
        import by.aleks.ghcwidget.data.ColorTheme;
        import by.aleks.ghcwidget.data.CommitsBase;
        import by.aleks.ghcwidget.data.Day;

        import java.util.ArrayList;

public class Widget extends AppWidgetProvider {

    private RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
        //remoteViews.setTextViewText(R.id.myTextView, loadData());
        remoteViews.setImageViewBitmap(R.id.commitsView, processImage(context));
        setClickIntent(context);
    }


    private void setClickIntent(Context context) {

        // When we click the widget, we want to open our main activity.
        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

        remoteViews.setOnClickPendingIntent(R.id.commitsView, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }


    private Bitmap processImage(Context context){
        CommitsBase base = loadData();
        Point size = getScreenSize(context);
        return createBitmap(base, 21, size, ColorTheme.ThemeName.MODERN);
    }


    private CommitsBase loadData(){
        GitHubAPITask task = new GitHubAPITask();

        try {
            return task.execute("xRoker").get();
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


    private Bitmap createBitmap(CommitsBase base, int weeksNumber, Point size, ColorTheme.ThemeName theme){
        float SPACE_RATIO = 0.1f;
        int TEXT_GRAPH_SPACE = 7;

        float side = size.x/weeksNumber * (1-SPACE_RATIO);
        float space = size.x/weeksNumber - side;
        float textSize = side*0.8f;

        int height = (int)(7*(side+space)+textSize+TEXT_GRAPH_SPACE);

        Bitmap bitmap = Bitmap.createBitmap(size.x, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
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
                    paint.setColor(ColorTheme.getColor(theme, day.getLevel()));
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