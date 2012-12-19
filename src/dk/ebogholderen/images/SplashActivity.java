package dk.ebogholderen.images;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends Activity implements OnClickListener
{
	ImageView imgSplash;
	Handler handler;
	SplashHandler splashHandler;
	Prefs prefs;

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
			//
			imgSplash = (ImageView) findViewById(R.id.imgSplash);
			imgSplash.setOnClickListener(this);
		}
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
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_email);
		//
		Button btnBackToWelcome = (Button) dialog.findViewById(R.id.btnBackToWelcome2);
		btnBackToWelcome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				showWelcome2();
			}
		});
		//
		Button btnSendInformation = (Button) dialog.findViewById(R.id.btnSendInformation);
		final EditText txtContactPerson = (EditText) dialog.findViewById(R.id.txtContactPerson);
		final EditText txtEmail = (EditText) dialog.findViewById(R.id.txtEmail);
		final EditText txtTelephone = (EditText) dialog.findViewById(R.id.txtTelephone);
		final EditText txtCVR = (EditText) dialog.findViewById(R.id.txtCVR);
		btnSendInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// make sure user provided valid values
				if (!Utility.isValidEmail(txtEmail.getText().toString()) || txtContactPerson.getText().toString().equalsIgnoreCase("") || txtTelephone.getText().toString().equalsIgnoreCase("") || txtCVR.getText().toString().equalsIgnoreCase("")) {
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperValues, Toast.LENGTH_LONG).show();
					return;
				}
				dialog.dismiss();
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
		if(Utility.isNetAvailable(this))
			client.sendEmail(devicetoken, email, contactPerson, phone, VATnumber);
		else
			Toast.makeText(this, R.string.networkError, Toast.LENGTH_LONG).show();
	}

	protected void onResume() {
		super.onResume();
	}

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
				closeSplash();
			break;
		}
	}

	private void closeSplash() {
		if(handler != null)
			handler.removeCallbacks(splashHandler);
		Intent i = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(i);
		SplashActivity.this.finish();
	}
}