package dk.ebogholderen.images;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
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
	boolean isResuming = true;
	NetworkReceiver netReceiver;
	//
	// a unique string to differentiate our broadcast intent
	public static final String UI_TO_SERVICE_ACTION = "UIToServiceAction";
	//
	static final String APP_NAME = "ImageUploader";
	static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/dk.ebogholderen.images/files/";
	static final int CAMERA_RESULT = 0;
	static final int GALLERY_RESULT = 1;
	static final int REVIEW_REQUEST = 2;
	static final int REVIEW_RESULT = 3;
	static final String UPLOAD_LIST_FILE = "uploadlist.txt";
	static final int DESIRED_WIDTH = 1600;
	static final int DESIRED_HEIGHT = 1200;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
		Intent serviceIntent = new Intent(MainActivity.this, UploaderService.class);
		startService(serviceIntent);
		//
		checkQueue(true);
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
				startCamera();
			break;
			case R.id.btnGallery:
				Intent j = new Intent(Intent.ACTION_PICK, Images.Media.EXTERNAL_CONTENT_URI);
				j.setType("image/*");
				startActivityForResult(j, GALLERY_RESULT);
			break;
			case R.id.btnEmail:
				shareViaEmail();
			break;
			case R.id.imgCloseFullImage:
				hideFullImage();
			break;
		}
	}

	/********************************************************************************************************/
	private void startCamera() {
		Intent i = new Intent(MainActivity.this, CameraActivity.class);
		startActivityForResult(i, REVIEW_REQUEST);
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
		registerReceiver(receiverServiceMsgs, new IntentFilter(UploaderService.SERVICE_TO_UI_ACTION));
		checkQueue(false);
	}

	/********************************************************************************************************/
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiverServiceMsgs);
	}

	/********************************************************************************************************/
	private void addImageToGrid(Uri imgUri, String category) {
		GridItem gridItem = new GridItem();
		gridItem.imgUri = imgUri;
		gridItem.category = category;
		gridItem.tag = ImageAdapter.arrImages.size() - 1;
		if (!ImageAdapter.arrImages.contains(gridItem)) {
			ImageAdapter.arrImages.add(gridItem);
			imgAdapter.notifyDataSetChanged();
			sendBroadcastToService(UploaderService.ADD_UPLOAD, gridItem);
			Toast.makeText(MainActivity.this, R.string.imageWillBeSentInBg, Toast.LENGTH_LONG).show();
		}
	}

	/*******************************************************************************************************/
	private void reviewImage(Uri imgUri) {
		Intent i = new Intent(MainActivity.this, ImageReviewActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		i.putExtra("img", imgUri.toString());
		startActivityForResult(i, REVIEW_REQUEST);
	}

	/********************************************************************************************************/
	private void shareViaEmail() {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// emailIntent.setType("vnd.android.cursor.item/email");
		emailIntent.setType("text/html");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
		emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// File image = Utility.uriToFile(MainActivity.this, picUri);
		// emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(image));
		ArrayList<Uri> arrUri = new ArrayList<Uri>();
		for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
			GridItem gi = (GridItem) gridView.getAdapter().getItem(i);
			if (gi.isSelected == 1) {
				File image = Utility.uriToFile(MainActivity.this, gi.imgUri);
				// emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(image));
				arrUri.add(Uri.fromFile(image));
			}
		}
		if (arrUri.size() > 1) {
			emailIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrUri);
		}
		else if (arrUri.size() == 1) {
			emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, arrUri.get(0));
		}
		else
			return;
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
		// deselectAllImagesInGrid(selectedPosition);
		GridItem gi = (GridItem) gridView.getAdapter().getItem(selectedPosition);
		View parentView = (View) v.getParent();
		if (gi.isSelected == 0) {
			gi.isSelected = 1;
			parentView.setBackgroundResource(R.drawable.griditem_border);
			//
			sendBroadcastToService(UploaderService.RESTART_UPLOAD, selectedPosition);
		}
		else {
			gi.isSelected = 0;
			parentView.setBackgroundDrawable(null);
		}
		enableDisableEmailBtn();
	}

	/********************************************************************************************************/
	public void handleImgDelete(View v) {
		int position = (Integer) v.getTag();
		askWheterToDeleteImage(position);
	}

	/********************************************************************************************************/
	private void enableDisableEmailBtn() {
		btnEmail.setEnabled(false);
		for (int i = 0; i < gridView.getAdapter().getCount(); i++) {
			GridItem gi = (GridItem) gridView.getItemAtPosition(i);
			if (gi.isSelected == 1) {
				btnEmail.setEnabled(true);
				break;
			}
		}
	}

	/********************************************************************************************************/
	private void showFullImage(Uri uri) {
		gridView.setVisibility(View.INVISIBLE);
		rlFullImage.setVisibility(View.VISIBLE);
		imgFullImage.setImageURI(uri);
		btnEmail.setEnabled(true);
		enableDisableEmailBtn();
	}

	/********************************************************************************************************/
	private void hideFullImage() {
		gridView.setVisibility(View.VISIBLE);
		rlFullImage.setVisibility(View.INVISIBLE);
		enableDisableEmailBtn();
	}

	/*******************************************************************************************************/
	private void askWheterToDeleteImage(final int position) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.confirm);
		alert.setMessage(R.string.confirmDeleteImg);
		alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				sendBroadcastToService(UploaderService.REMOVE_UPLOAD, position);
				//
				GridItem gi = ImageAdapter.arrImages.get(position);
				File f = Utility.uriToFile(MainActivity.this, gi.imgUri);
				f.delete();
				//
				ImageAdapter.arrImages.remove(position);
				imgAdapter.notifyDataSetChanged();
				//
				enableDisableEmailBtn();
			}
		});
		alert.setNegativeButton(R.string.photo_archive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				sendBroadcastToService(UploaderService.REMOVE_UPLOAD, position);
				//
				ImageAdapter.arrImages.remove(position);
				imgAdapter.notifyDataSetChanged();
				//
				enableDisableEmailBtn();
			}
		});
		alert.show();
	}

	/*******************************************************************************************************/
	private void checkQueue(boolean firstTimeCheck) {
		if (isResuming) {
			Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).registerTypeAdapter(Uri.class, new UriDeserializer()).create();
			String jsonData = Utility.deserializeData(MainActivity.SAVE_PATH, UPLOAD_LIST_FILE);
			Type t = new TypeToken<ArrayList<GridItem>>() {}.getType();
			ArrayList<GridItem> arr = gson.fromJson(jsonData, t);
			if (arr != null && arr.size() > 0) {
				ImageAdapter.arrImages = arr;
				imgAdapter.notifyDataSetChanged();
				for (GridItem gi : ImageAdapter.arrImages) {
					sendBroadcastToService(UploaderService.ADD_UPLOAD, gi);
				}
			}
			else {
				if(firstTimeCheck) {
					startCamera();
				}
			}
		}
		enableDisableEmailBtn();
	}

	/*******************************************************************************************************/
	private void sendBroadcastToService(int what, Parcelable obj) {
		Intent broadcast = new Intent(UI_TO_SERVICE_ACTION);
		broadcast.putExtra("what", what);
		broadcast.putExtra("obj", obj);
		sendBroadcast(broadcast);
	}

	private void sendBroadcastToService(int what, int i) {
		Intent broadcast = new Intent(UI_TO_SERVICE_ACTION);
		broadcast.putExtra("what", what);
		broadcast.putExtra("obj", i);
		sendBroadcast(broadcast);
	}

	private BroadcastReceiver receiverServiceMsgs = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int what = intent.getIntExtra("what", 0);
			switch (what) {
				case UploaderService.NETWORK_ERROR:
					Toast.makeText(MainActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
				break;
				case UploaderService.OTHER_ERROR:
					Toast.makeText(MainActivity.this, intent.getStringExtra("obj"), Toast.LENGTH_LONG).show();
				break;
				case UploaderService.SERVICE_STARTED:
					checkQueue(false);
				break;
				case UploaderService.PROGRESS_UPDATE:
					int tag = intent.getIntExtra("tag", 0);
					int progress = intent.getIntExtra("progress", 0);
					GridItem gi = imgAdapter.getItemByTag(tag);
					if (gi != null) {
//						ProgressBar pb = gi.pb;
//						if (pb != null) {
//							if (progress >= 100) {
//								gi.isUploaded = 1;
//								Rect bounds = pb.getProgressDrawable().getBounds();
//								pb.setProgressDrawable(getResources().getDrawable(R.drawable.greenprogress));
//								pb.getProgressDrawable().setBounds(bounds);
//							}
//							pb.setProgress((int) (progress));
//						}
					}
				break;
			}
		}
	};

	/*******************************************************************************************************/
	public static class UriDeserializer implements JsonDeserializer<Uri>
	{
		@Override
		public Uri deserialize(final JsonElement src, final Type srcType, final JsonDeserializationContext context) throws JsonParseException {
			return Uri.parse(src.getAsString());
		}
	}

	public static class UriSerializer implements JsonSerializer<Uri>
	{
		public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}
	}
}
