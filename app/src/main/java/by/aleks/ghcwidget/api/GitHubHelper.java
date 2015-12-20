package by.aleks.ghcwidget.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.CookieStore;
import java.util.HashSet;
import java.util.Set;

import android.webkit.CookieManager;
import by.aleks.ghcwidget.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;


public class GitHubHelper {

    private static final int HTTP_STATUS_OK = 200;
    private static byte[] buff = new byte[1024];
    private static final String logTag = "GHCWidget";

    public static class ApiException extends Exception {
        private static final long serialVersionUID = 1L;

        public ApiException(String msg) {
            super(msg);
        }

        public ApiException(String msg, Throwable thr) {
            super(msg, thr);
        }
    }

    /**
     * download user contribution data.
     *
     * @param username GitHub username
     * @return Array of html strings returned by the API.
     * @throws ApiException
     */
    protected static synchronized String downloadFromServer(String username, Context context)
            throws ApiException {
        String retval = null;
        String url = "https://github.com/users/" + username + "/contributions";

        Log.d(logTag, "Fetching " + url);

        // create an http client and a request object.
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // load and attach cookies
        String cookies = CookieManager.getInstance().getCookie(context.getString(R.string.login_url));
        if(cookies != null){
            BasicCookieStore lCS = getCookieStore(cookies, context.getString(R.string.domain));

            HttpContext localContext = new BasicHttpContext();
            client.setCookieStore(lCS);
            localContext.setAttribute(ClientContext.COOKIE_STORE, lCS);

        }


        try {

            // execute the request
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                // handle error here
                return "invalid_response";
            }

            // process the content.
            HttpEntity entity = response.getEntity();
            InputStream ist = entity.getContent();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            int readCount = 0;
            while ((readCount = ist.read(buff)) != -1) {
                content.write(buff, 0, readCount);
            }
            retval = new String(content.toByteArray());

        } catch (Exception e) {
            throw new ApiException("Problem connecting to the server " +
                    e.getMessage(), e);
        }

        return retval;
    }

    // parse cookie string
    private static BasicCookieStore getCookieStore(String cookies, String domain) {
        String[] cookieValues = cookies.split(";");
        BasicCookieStore cs = new BasicCookieStore();

        BasicClientCookie cookie;
        for (int i = 0; i < cookieValues.length; i++) {
            String[] split = cookieValues[i].split("=");
            if (split.length == 2)
                cookie = new BasicClientCookie(split[0], split[1]);
            else
                cookie = new BasicClientCookie(split[0], null);

            cookie.setDomain(domain);
            cs.addCookie(cookie);
        }
        return cs;

    }
}
