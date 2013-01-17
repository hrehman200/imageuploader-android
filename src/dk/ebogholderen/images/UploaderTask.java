package dk.ebogholderen.images;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import dk.ebogholderen.images.CustomMultipartEntity.ProgressListener;

class UploaderTask extends AsyncTask<Void, Integer, Void>
{
	ProgressDialog pd;
	long totalSize;
	UploaderService service;
	Context ctx;
	GridItem gridItem;
	String exception;
	byte ready = 1, running = 2, finished = 3;
	byte state = ready;
	Prefs prefs;

	public UploaderTask(UploaderService service, GridItem gridItem) {
		this.service = service;
		this.gridItem = gridItem;
		this.ctx = service.getApplicationContext();
		prefs = new Prefs(ctx);
	}

	@Override
	protected void onPreExecute() {
		/* pd = new ProgressDialog(ctx); pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); pd.setMessage("Uploading Picture..."); pd.setCancelable(false); */
	}

	@Override
	protected Void doInBackground(Void... params) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 120000);
		HttpConnectionParams.setSoTimeout(httpParams, 120000);
		final HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpContext httpContext = new BasicHttpContext();
		String url = String.format(RESTServiceClient.IMG_UPLOAD_URL, prefs.DEVICE_ID, URLEncoder.encode(gridItem.category));
		Log.v("url", url);
		final HttpPost httpPost = new HttpPost(url);
		try {
			CustomMultipartEntity multipartContent = new CustomMultipartEntity(new ProgressListener() {
				@Override
				public void transferred(long num) {
					if (isCancelled()) {
						httpPost.abort();
						return;
					}
					publishProgress((int) ((num / (float) totalSize) * 100));
				}
			});
			File image = Utility.uriToFile(ctx, gridItem.imgUri);
			// We use FileBody to transfer an image
			multipartContent.addPart("newImage", new FileBody(image));
			// multipartContent.addPart("imageFile", new FileBody(new File(this.imgPath)));
			totalSize = multipartContent.getContentLength();
			// Send it
			httpPost.setEntity(multipartContent);
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			String serverResponse = EntityUtils.toString(response.getEntity());
			Log.v("upload response", serverResponse);
			// ResponseFactory rp = new HttpResp(serverResponse);
			// return (TypeImage) rp.getData();
		}
		catch(SocketTimeoutException e) {
			exception = e.getMessage();
			e.printStackTrace();
		}
		catch (UnknownHostException e) {
			exception = e.getMessage();
			e.printStackTrace();
			httpPost.abort();
		}
		catch (ClientProtocolException e) {
			exception = e.getMessage();
			e.printStackTrace();
		}
		catch (IOException e) {
			exception = e.getMessage();
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			//exception = e.getMessage();
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		state = running;
		if(progress[0] >= 100) {
			//gridItem.isUploaded = 1;
			state = finished;
		}
		service.sendProgressMessage(gridItem.tag, progress[0]);
		Log.v("---", progress[0] + "...");
	}

	@Override
	protected void onPostExecute(Void v) {
		if (exception != null && !exception.equalsIgnoreCase("")) {
			service.sendOtherMessage(exception);
		}
		service.startDownload();
	}
}