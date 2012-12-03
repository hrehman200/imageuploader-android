package dk.ebogholderen.images;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import dk.ebogholderen.images.CustomMultipartEntity.ProgressListener;

class UploaderTask extends AsyncTask<Void, Integer, Void> {
	ProgressDialog pd;
	long totalSize;
	Context ctx;
	GridItem gridItem;
	boolean exceptionOccured;

	public UploaderTask(Context ctx, GridItem gridItem) {
		this.ctx = ctx;
		this.gridItem = gridItem;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(ctx);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Uploading Picture...");
		pd.setCancelable(false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		final HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		final HttpPost httpPost = new HttpPost(String.format(MainActivity.SERVER_URL, MainActivity.DEVICE_ID, URLEncoder.encode(gridItem.category)));
		try {
			CustomMultipartEntity multipartContent = new CustomMultipartEntity(new ProgressListener() {
				@Override
				public void transferred(long num) {
					if (isCancelled() || gridItem.isUploaded) {
						httpPost.abort();
						return;
					}
					publishProgress((int) ((num / (float) totalSize) * 100));
				}
			});
			File image = Utility.uriToFile(ctx, gridItem.imgUri);
			// We use FileBody to transfer an image
			multipartContent.addPart("imageFile", new FileBody(image));
			// multipartContent.addPart("imageFile", new FileBody(new File(this.imgPath)));
			totalSize = multipartContent.getContentLength();
			// Send it
			httpPost.setEntity(multipartContent);
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			String serverResponse = EntityUtils.toString(response.getEntity());
			// ResponseFactory rp = new HttpResp(serverResponse);
			// return (TypeImage) rp.getData();
		}
		catch (UnknownHostException uhe) {
			exceptionOccured = true;
			uhe.printStackTrace();
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		ProgressBar pb = gridItem.pb;
		if (pb != null) {
			pb.setProgress((int) (progress[0]));
			if (progress[0] >= 100)
				gridItem.isUploaded = true;
		}
		Log.v("---", progress[0] + "...");
	}

	@Override
	protected void onPostExecute(Void v) {
		if (exceptionOccured)
			Toast.makeText(ctx, R.string.alert_body_networkerror, Toast.LENGTH_LONG).show();
	}
}