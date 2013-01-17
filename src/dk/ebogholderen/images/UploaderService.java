package dk.ebogholderen.images;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.ebogholderen.images.MainActivity.UriDeserializer;
import dk.ebogholderen.images.MainActivity.UriSerializer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;

public class UploaderService extends Service
{
	NetworkReceiver receiverNet;
	ArrayList<UploaderTask> arrUploadTasks;
	public static int currentDownload = -1;
	//
	// a unique string to differentiate our broadcast intent
	public static final String SERVICE_TO_UI_ACTION = "ServiceToUIAction";
	//
	public static final int ADD_UPLOAD = 1;
	public static final int REMOVE_UPLOAD = 2;
	public static final int RESTART_UPLOAD = 3;
	public static final int NETWORK_ERROR = 5;
	public static final int OTHER_ERROR = 6;
	public static final int SERVICE_STARTED = 7;
	public static final int PROGRESS_UPDATE = 8;

	@Override
	public void onCreate() {
		arrUploadTasks = new ArrayList<UploaderTask>();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		// listen for network changes
		IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		receiverNet = new NetworkReceiver(this);
		registerReceiver(receiverNet, mNetworkStateFilter);
		// listen for messages from ui
		registerReceiver(receiverUIMsgs, new IntentFilter(MainActivity.UI_TO_SERVICE_ACTION));
		// tell the ui, service is started
		sendBroadcastToUI(UploaderService.SERVICE_STARTED, null);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiverNet);
		unregisterReceiver(receiverUIMsgs);
		super.onDestroy();
	}

	public void startDownload() {
		if (currentDownload < 0 || currentDownload >= arrUploadTasks.size()) {
			currentDownload = -1;
			return;
		}
		UploaderTask task = arrUploadTasks.get(currentDownload);
		Log.v("---", String.format("uploaded:%d, state:%d ", task.gridItem.isUploaded, task.state));
		if (Utility.isNetAvailable(getApplicationContext())) {
			if (task.gridItem.isUploaded == 0) {
				try {
					if (task.state != task.running && task.state != task.finished) {
						// a tast cannot be executed twice, therefore here create a new task in case we want to restart a task
						task = new UploaderTask(this, task.gridItem);
						task.execute();
					}
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
			else {
				currentDownload++;
				startDownload();
			}
		}
		else
			sendBroadcastToUI(NETWORK_ERROR, null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/********************************************************************************************************/
	private void updateQueue() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).registerTypeAdapter(Uri.class, new UriDeserializer()).create();
		String jsonData = gson.toJson(ImageAdapter.arrImages);
		Utility.serializeData(MainActivity.SAVE_PATH, MainActivity.UPLOAD_LIST_FILE, jsonData);
	}

	private void sendBroadcastToUI(int what, String msg) {
		Intent broadcast = new Intent(SERVICE_TO_UI_ACTION);
		broadcast.putExtra("what", what);
		broadcast.putExtra("obj", msg);
		sendBroadcast(broadcast);
	}

	public void sendDisconnectionMessage() {
		sendBroadcastToUI(NETWORK_ERROR, null);
	}

	public void sendOtherMessage(String msg) {
		sendBroadcastToUI(OTHER_ERROR, msg);
	}

	public void sendProgressMessage(int tag, int progress) {
		GridItem gi;
		try {
			gi = ImageAdapter.arrImages.get(currentDownload);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			currentDownload = arrUploadTasks.size();
			gi = ImageAdapter.arrImages.get(currentDownload);
		}
		ProgressBar pb = gi.pb;
		if (progress >= 100) {
			arrUploadTasks.get(currentDownload).gridItem.isUploaded = 1;
			gi.isUploaded = 1;
			if (pb != null) {
				Rect bounds = pb.getProgressDrawable().getBounds();
				pb.setProgressDrawable(getResources().getDrawable(R.drawable.greenprogress));
				pb.getProgressDrawable().setBounds(bounds);
				pb.setProgress((int) (progress));
			}
			updateQueue();
		}
		if (pb != null) {
			pb.setProgress((int) (progress));
		}
		Intent broadcast = new Intent(SERVICE_TO_UI_ACTION);
		broadcast.putExtra("what", PROGRESS_UPDATE);
		broadcast.putExtra("tag", tag);
		broadcast.putExtra("progress", progress);
		sendBroadcast(broadcast);
	}

	/***********************************************************************************************/
	private BroadcastReceiver receiverUIMsgs = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int what = intent.getIntExtra("what", 0);
			switch (what) {
				case ADD_UPLOAD:
					UploaderTask task = new UploaderTask(UploaderService.this, (GridItem) intent.getParcelableExtra("obj"));
					if (!arrUploadTasks.contains(task))
						arrUploadTasks.add(task);
					updateQueue();
					if (currentDownload == -1) {
						currentDownload = 0;
						startDownload();
					}
				break;
				case REMOVE_UPLOAD:
					int position = intent.getIntExtra("obj", 0);
					UploaderTask t = arrUploadTasks.get(position);
					t.cancel(true);
					arrUploadTasks.remove(position);
					updateQueue();
					// set currentDownload to active download
					for (int i = 0; i < arrUploadTasks.size(); i++) {
						if (arrUploadTasks.get(i).gridItem.isUploaded == 0) {
							currentDownload = i;
							break;
						}
					}
				break;
				case RESTART_UPLOAD:
					int selectedPosition = intent.getIntExtra("obj", 0);
					UploaderTask t2 = arrUploadTasks.get(selectedPosition);
					if (t2.gridItem.isUploaded == 0) {
						if (t2.state != t2.running && t2.state != t2.finished) {
							t2.cancel(true);
							t2 = new UploaderTask(UploaderService.this, t2.gridItem);
							t2.execute();
						}
					}
				break;
			}
		}
	};
}
