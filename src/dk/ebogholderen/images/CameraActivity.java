package dk.ebogholderen.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener, Callback, Camera.PictureCallback, Camera.ErrorCallback {
	SharedPreferences sp;
	SurfaceView cameraView;
	SurfaceHolder surfaceHolder;
	Camera camera;
	// -------------------
	public static File pathToSave = null;
	boolean takingPictures = true;
	public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/dk.ebogholderen.images/files/";
	private Uri imgUri;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		//
		pathToSave = new File(SAVE_PATH);
		if (!pathToSave.exists())
			pathToSave.mkdirs();
		// hack for avoiding these images from being seen in mobile's Gallery
		// app
		// writeImageToSDCard(null, ".nomedia");
		//
	}

	/******************************************************************************************************************/
	@Override
	public void onResume() {
		super.onResume();
		//
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		//
		cameraView = (SurfaceView) this.findViewById(R.id.cameraView);
		cameraView.setOnClickListener(this);
		surfaceHolder = cameraView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);
	}

	/******************************************************************************************************************/
	@Override
	protected void onPause() {
		super.onPause();
		stopTakingPictures();
	}

	/******************************************************************************************************************/
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	public void surfaceCreated(final SurfaceHolder holder) {
		Thread camThread = new Thread(new Runnable() {
			@Override
			public void run() {
				startTakingPictures(holder);
			}
		});
		camThread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		stopTakingPictures();
	}

	/******************************************************************************************************************/
	private void startTakingPictures(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				// This is an undocumented although widely known feature
				parameters.set("orientation", "portrait");
				// For Android 2.2 and above
				camera.setDisplayOrientation(90);
				// Uncomment for Android 2.0 and above
				// parameters.setRotation(90);
			}
			else {
				// This is an undocumented although widely known feature
				parameters.set("orientation", "landscape");
				// For Android 2.2 and above
				camera.setDisplayOrientation(0);
				// Uncomment for Android 2.0 and above
				// parameters.setRotation(0);
			}
			parameters.setPictureFormat(PixelFormat.JPEG);
			/*
			 * Picture Resolution Usually larger sizes will be at 0 index
			 */
			List<Size> listSizes = parameters.getSupportedPictureSizes();
			int w = 1024, h = 768;
			for (Size s : listSizes) {
				Log.v("---", s.width + "x" + s.height);
				w = s.width;
				h = s.height;
			}
			parameters.setPictureSize(w, h);
			camera.setParameters(parameters);
			camera.setErrorCallback(this);
			camera.setPreviewDisplay(holder);
		}
		catch (IOException exception) {
			camera.release();
		}
		takingPictures = true;
		//
		camera.startPreview();
	}

	private void stopTakingPictures() {
		takingPictures = false;
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	/******************************************************************************************************************/
	public void onPictureTaken(byte[] data, Camera camera) {
		Options bmpOptions = new BitmapFactory.Options();
		bmpOptions.inSampleSize = 2;
		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, bmpOptions);
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
		// store the image
		writeImageToSDCard(b, MainActivity.APP_NAME+"_" + new Date().getTime() + ".jpg");
		//
		//String url = MediaStore.Images.Media.insertImage(getContentResolver(), b, "ImageUploader_" + new Date().getTime(), "ImageUploader App Image");
		// get ready for taking next picture only if stopTakingPictures is not
		// called
		//camera.startPreview();
	}

	public void onError(int error, Camera camera) {
		Toast.makeText(CameraActivity.this, "Camera error. Restarting camera.", Toast.LENGTH_LONG).show();
		stopTakingPictures();
		startTakingPictures(surfaceHolder);
	}

	/******************************************************************************************************************/
	private void writeImageToSDCard(Bitmap b, String fileName) {
		OutputStream outStream = null;
		File file = new File(pathToSave, fileName);
		try {
			outStream = new FileOutputStream(file);
			if (b != null)
				b.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
			imgUri = Uri.parse(file.toString());
			reviewImage();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/******************************************************************************************************************/
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cameraView:
				camera.takePicture(null, null, null, CameraActivity.this);
			break;
		}
	}
	
	/******************************************************************************************************************/
	private void reviewImage() {
		Intent i = new Intent(CameraActivity.this, ImageReviewActivity.class);
		i.putExtra("img", imgUri.toString());
		startActivityForResult(i, MainActivity.REVIEW_REQUEST);
	}
	
	/******************************************************************************************************************/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
				case MainActivity.REVIEW_REQUEST:
					setResult(RESULT_OK, data);
					finish();
					break;
			}
		}
	}
}
