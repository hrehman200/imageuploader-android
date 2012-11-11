package dk.ebogholderen.images;

import java.io.File;
import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnClickListener {
	Button btnGallery, btnEmail;
	ImageButton btnCamera;
	GridView gridView;
	Uri imgUri;
	ImageAdapter imgAdapter;
	final static String APP_NAME = "ImageUploader";
	final static int CAMERA_RESULT = 0;
	final static int GALLERY_RESULT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//
		btnCamera = (ImageButton) findViewById(R.id.imgCamera);
		btnCamera.setOnClickListener(this);
		//
		btnGallery = (Button) findViewById(R.id.btnGallery);
		btnGallery.setOnClickListener(this);
		//
		btnEmail = (Button) findViewById(R.id.btnEmail);
		btnEmail.setOnClickListener(this);
		//
		gridView = (GridView) findViewById(R.id.gridImages);
		imgAdapter = new ImageAdapter(this);
		gridView.setAdapter(imgAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgCamera:
				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				imgUri = getCameraOutputImageUri();
				i.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
				startActivityForResult(i, CAMERA_RESULT);
			break;
			case R.id.btnGallery:
				Intent j = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
				j.setType("image/*");
				startActivityForResult(j, GALLERY_RESULT);
			break;
			case R.id.btnEmail:
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case CAMERA_RESULT:
					addImageToGrid();
				break;
				case GALLERY_RESULT:
					imgUri = intent.getData();
					addImageToGrid();
				break;
			}
		}
	}

	/**
	 * 
	 */
	private void addImageToGrid() {
		if(!ImageAdapter.arrImages.contains(imgUri)) {
			ImageAdapter.arrImages.add(imgUri);
			imgAdapter.notifyDataSetChanged();
			UploaderTask task = new UploaderTask(this, imgUri);
			task.execute();
		}
	}

	private Uri getCameraOutputImageUri() {
		File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_NAME);
		if(!imagesFolder.exists())
			imagesFolder.mkdirs();
		File image = new File(imagesFolder, String.format("%s_%s.jpg", APP_NAME, new Date().getTime()));
		Uri uriSavedImage = Uri.fromFile(image);
		return uriSavedImage;
	}
}
