package dk.ebogholderen.images;

import java.io.File;
import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
	Button btnGallery, btnEmail;
	ImageButton btnCamera;
	GridView gridView;
	Uri imgUri;
	int selectedPosition;
	ImageAdapter imgAdapter;
	FrameLayout rlFullImage;
	ImageView imgFullImage, imgCloseFullImage;
	final static String APP_NAME = "ImageUploader";
	final static int CAMERA_RESULT = 0;
	final static int GALLERY_RESULT = 1;
	final static int REVIEW_REQUEST = 2;
	final static int REVIEW_RESULT = 3;

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
		btnEmail.setEnabled(false);
		//
		gridView = (GridView) findViewById(R.id.gridImages);
		imgAdapter = new ImageAdapter(this);
		gridView.setAdapter(imgAdapter);
		gridView.setOnItemClickListener(this);
		//
		rlFullImage = (FrameLayout) findViewById(R.id.flFullImage);
		imgFullImage = (ImageView) findViewById(R.id.imgFullImage);
		imgCloseFullImage = (ImageView) findViewById(R.id.imgCloseFullImage);
		imgCloseFullImage.setOnClickListener(this);
	}

	/********************************************************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/********************************************************************************************************/
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgCamera:
				Intent i = new Intent(MainActivity.this, CameraActivity.class);
				startActivityForResult(i, REVIEW_REQUEST);
			break;
			case R.id.btnGallery:
				Intent j = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
				j.setType("image/*");
				startActivityForResult(j, GALLERY_RESULT);
			break;
			case R.id.btnEmail:
				GridItem gi = ImageAdapter.arrImages.get(selectedPosition);
				shareViaEmail(gi.imgUri);
			break;
			case R.id.imgCloseFullImage:
				hideFullImage();
				break;
		}
	}

	/********************************************************************************************************/
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case GALLERY_RESULT:
					imgUri = intent.getData();
					reviewImage();
				// addImageToGrid();
				break;
				case REVIEW_REQUEST:
					imgUri = Uri.parse((String) intent.getExtras().get("img"));
					addImageToGrid(true);
				break;
			}
		}
	}

	/********************************************************************************************************/
	private void addImageToGrid(boolean fromCameraActivity) {
		GridItem gridItem = new GridItem();
		gridItem.imgUri = imgUri;
		if (!ImageAdapter.arrImages.contains(gridItem)) {
			ImageAdapter.arrImages.add(gridItem);
			imgAdapter.notifyDataSetChanged();
			// UploaderTask task = new UploaderTask(MainActivity.this, imgUri);
			Log.v("---", imgUri.toString());
			UploaderTask task = new UploaderTask(MainActivity.this, gridItem);
			task.execute();
		}
	}

	/********************************************************************************************************/
	private Uri getCameraOutputImageUri() {
		File imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_NAME);
		if (!imagesFolder.exists())
			imagesFolder.mkdirs();
		File image = new File(imagesFolder, String.format("%s_%s.jpg", APP_NAME, new Date().getTime()));
		Uri uriSavedImage = Uri.fromFile(image);
		return uriSavedImage;
	}

	/********************************************************************************************************/
	private void reviewImage() {
		Intent i = new Intent(MainActivity.this, ImageReviewActivity.class);
		i.putExtra("img", imgUri.toString());
		startActivityForResult(i, REVIEW_REQUEST);
	}

	/********************************************************************************************************/
	private void shareViaEmail(Uri picUri) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// emailIntent.setType("vnd.android.cursor.item/email");
		emailIntent.setType("text/html");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "A picture I want to share.");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<b>Title: </b><br/>Nice picture I took from my Android device.<br/><br/>" + "<b>Description: </b><br/>Here is a nice picture I took with my Android device via ImageUploader App."));
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		emailIntent.setType("image/jpeg");
		emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, picUri);
		startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
	}

	/********************************************************************************************************/
	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
		selectedPosition = position;
		GridItem gi = ImageAdapter.arrImages.get(position);
		showFullImage(gi.imgUri);
	}
	
	/********************************************************************************************************/
	public void handleImgSelect(View v) {
		selectedPosition = (Integer) v.getTag();
		deselectAllImagesInGrid();
		View parentView = (View) v.getParent();
		if(parentView.getBackground() == null) {
			parentView.setBackgroundResource(R.drawable.griditem_border);
			btnEmail.setEnabled(true);
		}
		else {
			parentView.setBackgroundDrawable(null);
			btnEmail.setEnabled(false);
		}
	}
	
	/********************************************************************************************************/
	public void handleImgDelete(View v) {
		Log.v("---", v.getTag().toString());
	}
	
	/********************************************************************************************************/
	private void deselectAllImagesInGrid() {
		for(int i=0; i<gridView.getAdapter().getCount(); i++) {
			GridItem gi = (GridItem) gridView.getItemAtPosition(i);
			View parentView = (View) gi.pb.getParent();
			parentView.setBackgroundDrawable(null);
		}
	}
	
	/********************************************************************************************************/
	private void showFullImage(Uri uri) {
		gridView.setVisibility(View.INVISIBLE);
		rlFullImage.setVisibility(View.VISIBLE);
		imgFullImage.setImageURI(uri);
		btnEmail.setEnabled(true);
	}
	
	/********************************************************************************************************/
	private void hideFullImage() {
		gridView.setVisibility(View.VISIBLE);
		rlFullImage.setVisibility(View.INVISIBLE);
	}
}
