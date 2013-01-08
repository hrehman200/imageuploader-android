package dk.ebogholderen.images;

import java.io.File;
import java.io.IOException;
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
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import dk.ebogholderen.images.CustomMultipartEntity.ProgressListener;

class UploaderTask extends AsyncTask<Void, Integer, Void>
{
	ProgressDialog pd;
	long totalSize;
	Context ctx;
	GridItem gridItem;
	boolean exceptionOccured;
	byte ready = 1, running = 2, finished = 3;
	byte state = ready;
	Prefs prefs;

	public UploaderTask(Context ctx, GridItem gridItem) {
		this.ctx = ctx;
		this.gridItem = gridItem;
		prefs = new Prefs(ctx);
	}

	@Override
	protected void onPreExecute() {
		/* pd = new ProgressDialog(ctx); pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); pd.setMessage("Uploading Picture..."); pd.setCancelable(false); */
	}

	@Override
	protected Void doInBackground(Void... params) {
		final HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		String url = String.format(RESTServiceClient.SERVER_URL, prefs.DEVICE_ID, URLEncoder.encode(gridItem.category));
		Log.v("url", url);
		final HttpPost httpPost = new HttpPost(url);
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
			multipartContent.addPart("newImage", new FileBody(image));
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
			exceptionOccured = true;
			e.printStackTrace();
		}
		catch (IOException e) {
			exceptionOccured = true;
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			exceptionOccured = true;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		state = running;
		ProgressBar pb = gridItem.pb;
		if (pb != null) {
			if (progress[0] >= 100) {
				gridItem.isUploaded = true;
				Rect bounds = pb.getProgressDrawable().getBounds();
				pb.setProgressDrawable(ctx.getResources().getDrawable(R.drawable.greenprogress));
				pb.getProgressDrawable().setBounds(bounds);
			}
			pb.setProgress((int) (progress[0]));
		}
		Log.v("---", progress[0] + "...");
	}

	@Override
	protected void onPostExecute(Void v) {
		state = finished;
		if (exceptionOccured)
			Toast.makeText(ctx, R.string.alert_body_networkerror, Toast.LENGTH_LONG).show();
	}
}