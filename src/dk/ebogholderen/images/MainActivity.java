package dk.ebogholderen.images;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.telephony.TelephonyManager;
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
import android.widget.ProgressBar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener
{
	Button btnGallery, btnEmail;
	ImageButton btnCamera;
	GridView gridView;
	int selectedPosition;
	ImageAdapter imgAdapter;
	FrameLayout rlFullImage;
	ImageView imgFullImage, imgCloseFullImage;
	ArrayList<UploaderTask> arrUploadTasks;
	boolean isResuming = true;
	//
	static final String APP_NAME = "ImageUploader";
	static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/dk.ebogholderen.images/files/";
	static final int CAMERA_RESULT = 0;
	static final int GALLERY_RESULT = 1;
	static final int REVIEW_REQUEST = 2;
	static final int REVIEW_RESULT = 3;
	static final String UPLOAD_LIST_FILE = "uploadlist.txt";

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
		//
		arrUploadTasks = new ArrayList<UploaderTask>();
	}

	/********************************************************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
					Uri imgUri = intent.getData();
					reviewImage(imgUri);
				// addImageToGrid();
				break;
				case REVIEW_REQUEST:
					Uri imgUri2 = Uri.parse((String) intent.getExtras().get("img"));
					String category = (String) intent.getExtras().getString("category");
					addImageToGrid(imgUri2, category);
					isResuming = false;
				break;
			}
		}
	}

	/********************************************************************************************************/
	@Override
	protected void onResume() {
		super.onResume();
		if (isResuming) {
			Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).registerTypeAdapter(Uri.class, new UriDeserializer()).create();
			String jsonData = Utility.deserializeData(MainActivity.SAVE_PATH, UPLOAD_LIST_FILE);
			Type t = new TypeToken<ArrayList<GridItem>>() {}.getType();
			ArrayList<GridItem> arr = gson.fromJson(jsonData, t);
			if (arr != null && arr.size() > 0) {
				ImageAdapter.arrImages = arr;
				imgAdapter.notifyDataSetChanged();
				arrUploadTasks = new ArrayList<UploaderTask>();
				for (GridItem gi : ImageAdapter.arrImages) {
					UploaderTask task = new UploaderTask(MainActivity.this, gi);
					if (!gi.isUploaded) {
						task.execute();
					}
					// add the scheduled task to array so that later we stop/restart this task
					arrUploadTasks.add(task);
				}
			}
		}
	}

	/********************************************************************************************************/
	@Override
	protected void onPause() {
		super.onPause();
		Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).registerTypeAdapter(Uri.class, new UriDeserializer()).create();
		String jsonData = gson.toJson(ImageAdapter.arrImages);
		Utility.serializeData(MainActivity.SAVE_PATH, UPLOAD_LIST_FILE, jsonData);
	}

	/********************************************************************************************************/
	private void addImageToGrid(Uri imgUri, String category) {
		GridItem gridItem = new GridItem();
		gridItem.imgUri = imgUri;
		gridItem.category = category;
		if (!ImageAdapter.arrImages.contains(gridItem)) {
			ImageAdapter.arrImages.add(gridItem);
			imgAdapter.notifyDataSetChanged();
			Log.v("---", imgUri.toString());
			UploaderTask task = new UploaderTask(MainActivity.this, gridItem);
			task.execute();
			// add the scheduled task to array so that later we stop/restart this task
			arrUploadTasks.add(task);
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

	/*******************************************************************************************************/
	private void reviewImage(Uri imgUri) {
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
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		File image = Utility.uriToFile(MainActivity.this, picUri);
		emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(image));
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
		deselectAllImagesInGrid(selectedPosition);
		View parentView = (View) v.getParent();
		Log.v("parentView", parentView.toString());
		if (parentView.getBackground() == null) {
			parentView.setBackgroundResource(R.drawable.griditem_border);
			btnEmail.setEnabled(true);
			//
			UploaderTask t = arrUploadTasks.get(selectedPosition);
			t = new UploaderTask(MainActivity.this, t.gridItem);
			t.execute();
		}
		else {
			parentView.setBackgroundDrawable(null);
			btnEmail.setEnabled(false);
		}
	}

	/********************************************************************************************************/
	public void handleImgDelete(View v) {
		int position = (Integer) v.getTag();
		askWheterToDeleteImage(position);
	}

	/********************************************************************************************************/
	private void deselectAllImagesInGrid(int except) {
		for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
			if (i == except)
				continue;
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

	/*******************************************************************************************************/
	private void askWheterToDeleteImage(final int position) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.confirm);
		alert.setMessage(R.string.confirmDeleteImg);
		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				UploaderTask t = arrUploadTasks.get(position);
				t.cancel(true);
				arrUploadTasks.remove(position);
				//
				GridItem gi = ImageAdapter.arrImages.get(position);
				File f = Utility.uriToFile(MainActivity.this, gi.imgUri);
				f.delete();
				//
				ImageAdapter.arrImages.remove(position);
				imgAdapter.notifyDataSetChanged();
			}
		});
		alert.setNegativeButton(R.string.photo_archive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				UploaderTask t = arrUploadTasks.get(position);
				t.cancel(true);
				arrUploadTasks.remove(position);
				//
				ImageAdapter.arrImages.remove(position);
				imgAdapter.notifyDataSetChanged();
			}
		});
		alert.show();
	}

	/*******************************************************************************************************/
	private class UriDeserializer implements JsonDeserializer<Uri>
	{
		@Override
		public Uri deserialize(final JsonElement src, final Type srcType, final JsonDeserializationContext context) throws JsonParseException {
			return Uri.parse(src.getAsString());
		}
	}

	private class UriSerializer implements JsonSerializer<Uri>
	{
		public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}
	}
}
