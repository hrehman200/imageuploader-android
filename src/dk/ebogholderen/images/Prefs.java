package dk.ebogholderen.images;

import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Prefs {
	private SharedPreferences _prefs = null;
	private Editor _editor = null;
	public String DEVICE_ID = "";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_EMAIL_REGISTERED = "emailRegistered";

	public Prefs(Context context) {
		this._prefs = context.getSharedPreferences("PREFS_IMAGEUPLOADER", Context.MODE_PRIVATE);
		this._editor = this._prefs.edit();
		//
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		DEVICE_ID = tm.getDeviceId();
	}

	public String getValue(String key, String defaultvalue) {
		if (this._prefs == null) {
			return "Unknown";
		}
		return this._prefs.getString(key, defaultvalue);
	}

	public void setValue(String key, String value) {
		if (this._editor == null) {
			return;
		}
		this._editor.putString(key, value);
	}

	public boolean getBoolean(String key, boolean defaultvalue) {
		if (this._prefs == null) {
			return false;
		}
		return this._prefs.getBoolean(key, defaultvalue);
	}

	public void setBoolean(String key, boolean value) {
		if (this._editor == null) {
			return;
		}
		this._editor.putBoolean(key, value);
	}

	public void save() {
		if (this._editor == null) {
			return;
		}
		this._editor.commit();
	}

	public void showPrefs() {
		Map<String, ?> map = this._prefs.getAll();
		for (Map.Entry mapEntry : map.entrySet()) {
			String key = (String) mapEntry.getKey();
			String value = (String) mapEntry.getValue();
			Log.v("PREFS", String.format("%s=%s", key, value));
		}
	}
}
