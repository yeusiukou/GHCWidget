package by.aleks.ghcwidget.api;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


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
    protected static synchronized String downloadFromServer(String username)
            throws ApiException {
        String retval = null;
        String url = "https://github.com/users/" + username + "/contributions";

        Log.d(logTag, "Fetching " + url);

        // create an http client and a request object.
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

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
}
