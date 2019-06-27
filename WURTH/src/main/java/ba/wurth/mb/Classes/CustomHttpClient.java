package ba.wurth.mb.Classes;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class CustomHttpClient {
	/** The time it takes for our client to timeout */
    public static final int HTTP_TIMEOUT = 5 * 60 * 1000; // milliseconds

    public static String executeHttpPost(String url, ArrayList<NameValuePair> postParameters) {
        String s = "";
        try {

            BufferedReader in;
            HttpClient client = new DefaultHttpClient();
            ClientConnectionManager mgr = client.getConnectionManager();
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);

        	if (client == null) return "";
        	
            HttpPost request = new HttpPost(url);
            request.addHeader("Accept-Encoding", "gzip");
            
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters, "UTF-8");
            
            request.setEntity(formEntity);
            HttpResponse response = client.execute(request);
            
            InputStream instream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                instream = new GZIPInputStream(instream);
            }            
                        
            in = new BufferedReader(new InputStreamReader(instream));

            StringBuffer sb = new StringBuffer("");
            String line;
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            
            in.close();

            return sb.toString();
        } 
        catch (Exception e) {
        	
		}
        return "";
    }

    public static InputStream executeHttpPostStream(String url, ArrayList<NameValuePair> postParameters) {

        InputStream instream = null;

        try {

            HttpClient client = new DefaultHttpClient();
            ClientConnectionManager mgr = client.getConnectionManager();
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);

            if (client == null) return null;
        	
            HttpPost request = new HttpPost(url);
            request.addHeader("Accept-Encoding", "gzip");
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters, "UTF-8");
            request.setEntity(formEntity);
            HttpResponse response = client.execute(request);

            instream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                instream = new GZIPInputStream(instream);
            }

            //client.getConnectionManager().shutdown();
        }
        catch (Exception e) {
            //e.printStackTrace();
		}        
        return instream;
    }
}