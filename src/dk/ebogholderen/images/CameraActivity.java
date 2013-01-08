package dk.ebogholderen.images;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener, Callback, Camera.PictureCallback, Camera.ErrorCallback, Camera.ShutterCallback
{
	SharedPreferences sp;
	SurfaceView cameraView;
	SurfaceHolder surfaceHolder;
	Camera camera;
	ImageView imgCamera;
	// -------------------
	public static File pathToSave = null;
	boolean takingPictures = true;
	private Uri imgUri;
	private int userRingerMode;
	private AudioManager audioManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		//
		imgCamera = (ImageView) findViewById(R.id.imgCamera);
		imgCamera.setOnClickListener(this);
		//
		pathToSave = new File(MainActivity.SAVE_PATH);
		if (!pathToSave.exists())
			pathToSave.mkdirs();
		// hack for avoiding these images from being seen in mobile's Gallery
		// app
		// writeImageToSDCard(null, ".nomedia");
		//
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
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
		startTakingPictures(holder);
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
			parameters.setPictureSize(MainActivity.DESIRED_WIDTH, MainActivity.DESIRED_HEIGHT);
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
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	/******************************************************************************************************************/
	public void onPictureTaken(byte[] data, Camera camera) {
		Options bmpOptions = new BitmapFactory.Options();
		// bmpOptions.inSampleSize = 2;
		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, bmpOptions);
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
		//
		stopTakingPictures();
		// store the image
		File file = Utility.writeImageToSDCard(b, MainActivity.SAVE_PATH, MainActivity.APP_NAME + "_" + new Date().getTime() + ".jpg");
		imgUri = Uri.parse(file.toString());
		reviewImage();
	}

	public void onError(int error, Camera camera) {
		Toast.makeText(CameraActivity.this, "Camera error. Restarting camera.", Toast.LENGTH_LONG).show();
		stopTakingPictures();
		startTakingPictures(surfaceHolder);
	}
	
	@Override
	public void onShutter() {
		audioManager.setRingerMode(userRingerMode);
	}

	/******************************************************************************************************************/
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgCamera:
				if (camera != null) {
					
					userRingerMode = audioManager.getRingerMode();
					audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					camera.takePicture(CameraActivity.this, null, null, CameraActivity.this);
				}
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
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case MainActivity.REVIEW_REQUEST:
					setResult(RESULT_OK, data);
					finish();
				break;
			}
		}
	}

	
}
