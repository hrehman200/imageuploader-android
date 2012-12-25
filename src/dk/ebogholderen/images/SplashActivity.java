package dk.ebogholderen.images;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SplashActivity extends Activity implements OnClickListener
{
	ImageView imgSplash;
	Handler handler;
	SplashHandler splashHandler;
	Prefs prefs;
	Dialog dialog;
	Button btnSendInformation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		//
		prefs = new Prefs(this);
		if (!prefs.getBoolean(Prefs.KEY_EMAIL_REGISTERED, false)) {
			showWelcome1();
		}
		else {
			handler = new Handler();
			splashHandler = new SplashHandler();
			handler.postDelayed(splashHandler, 3000);
		}
		//
		imgSplash = (ImageView) findViewById(R.id.imgSplash);
		imgSplash.setOnClickListener(this);
	}

	private void showWelcome1() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.welcomeTo);
		alert.setMessage(R.string.welcome1Message);
		alert.setPositiveButton(R.string.nextSide, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				showWelcome2();
			}
		});
		alert.show();
	}

	private void showWelcome2() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.welcomeTo);
		alert.setMessage(R.string.welcome2Message);
		alert.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				showWelcome1();
			}
		});
		alert.setNegativeButton(R.string.tryUsFreeFor1Month, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				showEmailForm();
			}
		});
		alert.show();
	}

	private void showEmailForm() {
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_email);
		//
		Button btnBackToWelcome = (Button) dialog.findViewById(R.id.btnBackToWelcome2);
		btnBackToWelcome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard(v);
				dismissDialog();
				showWelcome2();
			}
		});
		//
		btnSendInformation = (Button) dialog.findViewById(R.id.btnSendInformation);
		final EditText txtContactPerson = (EditText) dialog.findViewById(R.id.txtContactPerson);
		txtContactPerson.setOnKeyListener(keyListener);
		final EditText txtEmail = (EditText) dialog.findViewById(R.id.txtEmail);
		txtEmail.setOnKeyListener(keyListener);
		final EditText txtTelephone = (EditText) dialog.findViewById(R.id.txtTelephone);
		txtTelephone.setOnKeyListener(keyListener);
		final EditText txtCVR = (EditText) dialog.findViewById(R.id.txtCVR);
		txtCVR.setOnKeyListener(keyListener);
		btnSendInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard(v);
				// make sure user provided valid values
				if (txtContactPerson.getText().toString().equalsIgnoreCase("")) {
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperContactPerson, Toast.LENGTH_LONG).show();
					return;
				}
				else if (!Utility.isValidEmail(txtEmail.getText().toString())) {
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperEmail, Toast.LENGTH_LONG).show();
					return;
				}
				else if (txtTelephone.getText().toString().equalsIgnoreCase("")) {
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperTelephone, Toast.LENGTH_LONG).show();
					return;
				}
				else if (txtCVR.getText().toString().equalsIgnoreCase("")) {
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperCVR, Toast.LENGTH_LONG).show();
					return;
				}
				dismissDialog();
				showEnjoyMessage(Prefs.DEVICE_ID, txtEmail.getText().toString(), txtContactPerson.getText().toString(), txtTelephone.getText().toString(), txtCVR.getText().toString());
			}
		});
		dialog.show();
	}

	private void showEnjoyMessage(final String devicetoken, final String email, final String contactPerson, final String phone, final String VATnumber) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(R.string.enjoyMessage);
		alert.setPositiveButton(R.string.goAhead, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				sendEmail(devicetoken, email, contactPerson, phone, VATnumber);
			}
		});
		alert.show();
	}

	private void sendEmail(String devicetoken, final String email, String contactPerson, String phone, String VATnumber) {
		final RESTServiceClient client = new RESTServiceClient(this, new Handler() {
			public void handleMessage(Message m) {
				switch (m.what) {
					case 1:
						RESTServiceClient.myprogress.hide();
						String response = (String) m.obj;
						if (response.equalsIgnoreCase("<response><code>100</code></response>")) {
							prefs.setValue(Prefs.KEY_EMAIL, email);
							prefs.setBoolean(Prefs.KEY_EMAIL_REGISTERED, true);
							prefs.save();
							closeSplash();
						}
					break;
					case 2:
						RESTServiceClient.myprogress.hide();
						Toast.makeText(SplashActivity.this, (String) m.obj, Toast.LENGTH_LONG).show();
					break;
				}
			}
		});
		if (Utility.isNetAvailable(this))
			client.sendEmail(devicetoken, email, contactPerson, phone, VATnumber);
		else
			Toast.makeText(this, R.string.networkError, Toast.LENGTH_LONG).show();
	}

	protected void onResume() {
		super.onResume();
	}

	OnKeyListener keyListener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// If the event is a key-down event on the "enter" button
			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
				// Perform action on key press
				LinearLayout linEmailDialog = (LinearLayout) dialog.findViewById(R.id.linEmailDialog);
				int i = Integer.parseInt(v.getTag().toString());
				if (i == 4) {
					btnSendInformation.requestFocus();
					hideKeyboard(v);
				}
				else {
					View viewToFocus = linEmailDialog.findViewWithTag(Integer.toString(i + 1));
					viewToFocus.requestFocus();
				}
				return true;
			}
			return false;
		}
	};

	class SplashHandler implements Runnable
	{
		@Override
		public void run() {
			closeSplash();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imgSplash:
				if (!prefs.getBoolean(Prefs.KEY_EMAIL_REGISTERED, false))
					showWelcome1();
				else
					closeSplash();
			break;
		}
	}

	private void closeSplash() {
		if (handler != null)
			handler.removeCallbacks(splashHandler);
		Intent i = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(i);
		SplashActivity.this.finish();
	}

	private void dismissDialog() {
		dialog.dismiss();
	}

	private void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}