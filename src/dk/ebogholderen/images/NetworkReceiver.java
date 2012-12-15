package dk.ebogholderen.images;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver
{
	MainActivity mainActivity;
	public NetworkReceiver(MainActivity m) {
		this.mainActivity = m;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		switch (info.getType()) {
			case ConnectivityManager.TYPE_MOBILE:
			case ConnectivityManager.TYPE_WIFI:
				if (info.getState() == State.CONNECTED) {
					mainActivity.startDownload();
				}
				else if (info.getState() == State.DISCONNECTED) {
					Toast.makeText(mainActivity, R.string.networkError, Toast.LENGTH_LONG).show();
				}
			break;
		}
	}
}
