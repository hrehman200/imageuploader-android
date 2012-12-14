package dk.ebogholderen.images;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class RESTServiceClient {
	static final String SERVER_URL = "http://images.ebogholderen.dk/PicUploader/ImageUploader.php?devicetoken=%s&imagetype=%s";
	static final String EMAIL_SAVE_URL = "http://images.ebogholderen.dk/PicUploader/Email.php?devicetoken=%s&email=%s&contactperson=%s&phone=%s&VATnumber=%s";
	//
	public static Context ctx;
	public static ProgressDialog myprogress;
	Prefs prefs;
	Handler pHandler;

	public RESTServiceClient(Context c, Handler pHandler) {
		// this.ctx = c;
		//
		prefs = new Prefs(c);
		this.pHandler = pHandler;
		myprogress = new ProgressDialog(c);
	}

	/**************************************************************************************************/
	public void makeRequest(String scriptUrl, List<NameValuePair> nameValuePairs) {
		if (nameValuePairs != null)
			Log.v("scriptName", nameValuePairs.toString());
		//
		Thread t = new Thread(new DoMakeRequest(scriptUrl, nameValuePairs, this.pHandler));
		t.start();
	}

	/**************************************************************************************************/
	public void showProgress(String title, String message) {
		myprogress.setTitle(title);
		myprogress.setMessage(message);
		myprogress.show();
	}
	
	/**************************************************************************************************/
	public void hideProgress() {
		myprogress.hide();
	}

	/**************************************************************************************************/
	/**
	 * Call the server with given parameters to send email
	 * 
	 * @param String
	 *            pinNumber The pin number of the employee
	 * 
	 */
	public void sendEmail(String devicetoken, String email, String contactPerson, String phone, String VATnumber) {
		showProgress(ctx.getResources().getString(R.string.plzWait), ctx.getResources().getString(R.string.sendingEmail));
		String url = String.format(EMAIL_SAVE_URL, URLEncoder.encode(devicetoken), URLEncoder.encode(email), URLEncoder.encode(contactPerson), URLEncoder.encode(phone), URLEncoder.encode(VATnumber));
		this.makeRequest(url, null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	class DoMakeRequest implements Runnable {
		String scriptUrl;
		List<NameValuePair> nameValuePairs;
		Handler pHandler;

		/**
		 * Calls the remote method in a separate thread and passes the response to xmlHandler
		 * 
		 * @param methodName
		 *            the remote method to call on server
		 * @param nameValuePairs
		 *            a list of parameters that the remote method accept
		 */
		public DoMakeRequest(String scriptUrl, List<NameValuePair> nameValuePairs, Handler pHandler) {
			this.scriptUrl = scriptUrl;
			this.nameValuePairs = nameValuePairs;
			this.pHandler = pHandler;
		}

		@Override
		public void run() {
			// use timeout of 1 minute
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
			HttpConnectionParams.setSoTimeout(httpParams, 60000);
			//
			DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
			Log.v("FULL --- URL", scriptUrl);
			HttpResponse response = null;
			HttpGet httpGet = new HttpGet(scriptUrl);
			try {
				response = httpclient.execute(httpGet);
				Log.v("Status Code", response.getStatusLine().getStatusCode() + ">>>");
				if (response.getStatusLine().getStatusCode() == 401) {
					this.pHandler.sendMessage(Utility.createMessage(2, response.getStatusLine().getReasonPhrase()));
					return;
				}
				if (response.getStatusLine().getStatusCode() == 404) {
					this.pHandler.sendMessage(Utility.createMessage(2, response.getStatusLine().getReasonPhrase()));
					return;
				}
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inStream = entity.getContent();
					BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inStream));
					StringBuilder stringbuilder = new StringBuilder();
					String currentline = null;
					try {
						while ((currentline = bufferedreader.readLine()) != null) {
							stringbuilder.append(currentline);
						}
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					String result = stringbuilder.toString();
					Log.v("RESPONSE", "-"+result);
					this.pHandler.sendMessage(Utility.createMessage(1, result));
				}
			}
			catch (UnknownHostException e) {
				if (this.pHandler != null)
					this.pHandler.sendMessage(Utility.createMessage(2, e.getMessage()));
				e.printStackTrace();
			}
			catch (Exception e) {
				if (this.pHandler != null)
					this.pHandler.sendMessage(Utility.createMessage(2, e.getMessage()));
				e.printStackTrace();
			}
		}
	}
}
