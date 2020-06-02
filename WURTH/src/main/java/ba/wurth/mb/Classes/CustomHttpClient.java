package ba.wurth.mb.Classes;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
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
        	String temp = e.getMessage();
		}
        return "";
    }

    public static String executeHttpsPost(String url, ArrayList<NameValuePair> postParameters) {
        String s = "";
        try {

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            DefaultHttpClient httpClient = new DefaultHttpClient(ccm, params);

            BufferedReader in;

            if (httpClient == null) return "";

            HttpPost request = new HttpPost(url);
            request.addHeader("Accept-Encoding", "gzip");

            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters, "UTF-8");

            request.setEntity(formEntity);
            HttpResponse response = httpClient.execute(request);

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
            String temp = e.getMessage();
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
            e.getMessage();
		}        
        return instream;
    }
}