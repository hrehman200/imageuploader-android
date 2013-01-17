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
	UploaderService service;
	public NetworkReceiver(UploaderService s) {
		this.service = s;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		switch (info.getType()) {
			case ConnectivityManager.TYPE_MOBILE:
			case ConnectivityManager.TYPE_WIFI:
				if (info.getState() == State.CONNECTED) {
					service.startDownload();
				}
				else if (info.getState() == State.DISCONNECTED) {
					service.sendDisconnectionMessage();
				}
			break;
		}
	}
}
