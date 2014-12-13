package by.aleks.ghcwidget.api;

import by.aleks.ghcwidget.Widget;
import by.aleks.ghcwidget.data.CommitsBase;
import by.aleks.ghcwidget.data.Day;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.AsyncTask;
import android.util.Log;
import org.xmlpull.v1.XmlPullParserFactory;


public class GitHubAPITask extends AsyncTask<String, Integer, CommitsBase> // Username to the input, Progress, Output
{

    private static final String debugTag = "GHCWiget";
    private Widget widget;

    public GitHubAPITask(Widget widget){
        this.widget = widget;
    }


    // Call the downloading method in background and load data
    @Override
    protected CommitsBase doInBackground(String... params) {
        String result = null;
        try {
            Log.d(debugTag, "Background:" + Thread.currentThread().getName());
            result = GitHubHelper.downloadFromServer(params[0]);
        } catch (GitHubHelper.ApiException e) {
            Log.d(debugTag, "Loading failed");
            e.getMessage();
            widget.setStatus(Widget.STATUS_OFFLINE);
            return null;
        }
        if(result.equals("invalid_response")){
            widget.setStatus(Widget.STATUS_NOTFOUND);
            return null;
        }

        CommitsBase base = parseResult(result);
        return base;
    }


    private CommitsBase parseResult(String result) {

        CommitsBase base = new CommitsBase();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();

            boolean firstTagSkipped = false;
            SimpleDateFormat textFormat = new SimpleDateFormat("yyyy-MM-dd");

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: {
                        break;
                    }
                    case XmlPullParser.START_TAG: {
                        if (xpp.getName().equals("g")) {
                            if (!firstTagSkipped) {
                                firstTagSkipped = true;
                                eventType = xpp.next();
                                break;
                            } else {
                                base.newWeek();
                                eventType = xpp.next();
                                break;
                            }

                        }
                        if (xpp.getName().equals("rect")) {
                            Date date = textFormat.parse(xpp.getAttributeValue(null, "data-date"));
                            int commits = Integer.valueOf(xpp.getAttributeValue(null, "data-count"));
                            String color = xpp.getAttributeValue(null, "fill");
                            Day day = new Day(date, commits, color);
                            base.addDay(day);
                            eventType = xpp.next();
                            break;
                        }
                    }
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            Log.d(debugTag, "Error in parsing");
            e.printStackTrace();
        }
        return base;
    }
}