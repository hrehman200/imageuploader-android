package dk.ebogholderen.images;

import java.io.File;
import java.net.URI;
import org.apache.http.HttpResponse;
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
import dk.ebogholderen.images.CustomMultipartEntity.ProgressListener;

class UploaderTask extends AsyncTask<Void, Integer, Void> {
	ProgressDialog pd;
	long totalSize;
	Context ctx;
	Uri uriImage;

	public UploaderTask(Context ctx, Uri uriImage) {
		this.ctx = ctx;
		this.uriImage = uriImage;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(ctx);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Uploading Picture...");
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext httpContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost("http://www.kohatcci.com/upload.php");
		try {
			CustomMultipartEntity multipartContent = new CustomMultipartEntity(new ProgressListener() {
				@Override
				public void transferred(long num) {
					publishProgress((int) ((num / (float) totalSize) * 100));
				}
			});
			// We use FileBody to transfer an image
			multipartContent.addPart("imageFile", new FileBody(new File(new URI(uriImage.toString()))));
			totalSize = multipartContent.getContentLength();
			// Send it
			httpPost.setEntity(multipartContent);
			HttpResponse response = httpClient.execute(httpPost, httpContext);
			String serverResponse = EntityUtils.toString(response.getEntity());
			// ResponseFactory rp = new HttpResp(serverResponse);
			// return (TypeImage) rp.getData();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		pd.setProgress((int) (progress[0]));
	}

	@Override
	protected void onPostExecute(Void v) {
		pd.dismiss();
	}
}