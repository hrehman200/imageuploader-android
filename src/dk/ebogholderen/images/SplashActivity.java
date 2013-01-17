package dk.ebogholderen.images;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
		final Dialog dialogWelcome1 = new Dialog(this, android.R.style.Theme_Translucent);
		dialogWelcome1.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogWelcome1.setContentView(R.layout.dialog_welcome1);
		TextView txtTitle = (TextView) dialogWelcome1.findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.welcomeTo);
		TextView txtMsg = (TextView) dialogWelcome1.findViewById(R.id.txtMsg);
		txtMsg.setText(R.string.welcome1Message);
		Button btnNext = (Button) dialogWelcome1.findViewById(R.id.btnNext);
		btnNext.setText(R.string.nextSide);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogWelcome1.dismiss();
				showWelcome2();
			}
		});
		applyDialogLayoutParams(dialogWelcome1);
		dialogWelcome1.show();
	}

	private void applyDialogLayoutParams(Dialog d) {
		WindowManager.LayoutParams lp = d.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		d.getWindow().setAttributes(lp);
	}

	private void showWelcome2() {
		final Dialog dialogWelcome2 = new Dialog(this, android.R.style.Theme_Translucent);
		dialogWelcome2.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogWelcome2.setContentView(R.layout.dialog_welcome2);
		TextView txtTitle = (TextView) dialogWelcome2.findViewById(R.id.txtTitle);
		txtTitle.setText(R.string.welcomeTo);
		TextView txtMsg = (TextView) dialogWelcome2.findViewById(R.id.txtMsg);
		txtMsg.setText(R.string.welcome2Message);
		Button btnBack = (Button) dialogWelcome2.findViewById(R.id.btnBack);
		btnBack.setText(R.string.back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogWelcome2.dismiss();
				showWelcome1();
			}
		});
		Button btnNext = (Button) dialogWelcome2.findViewById(R.id.btnNext);
		btnNext.setText(R.string.tryUsFreeFor1Month);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogWelcome2.dismiss();
				showEmailForm();
			}
		});
		applyDialogLayoutParams(dialogWelcome2);
		dialogWelcome2.show();
	}

	private void showEmailForm() {
		dialog = new Dialog(this, android.R.style.Theme_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_email);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
				if (txtContactPerson.getText().toString().trim().length() <= 0 || txtContactPerson.getText().toString().equalsIgnoreCase("")) {
					txtContactPerson.requestFocus();
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperContactPerson, Toast.LENGTH_LONG).show();
					return;
				}
				else if (!Utility.isValidEmail(txtEmail.getText().toString())) {
					txtEmail.requestFocus();
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperEmail, Toast.LENGTH_LONG).show();
					return;
				}
				else if (txtTelephone.getText().toString().trim().length() <= 0 || txtTelephone.getText().toString().equalsIgnoreCase("")) {
					txtTelephone.requestFocus();
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperTelephone, Toast.LENGTH_LONG).show();
					return;
				}
				else if (txtCVR.getText().toString().trim().length() <= 0 || txtCVR.getText().toString().equalsIgnoreCase("")) {
					txtCVR.requestFocus();
					Toast.makeText(SplashActivity.this, R.string.plzEnterProperCVR, Toast.LENGTH_LONG).show();
					return;
				}
				dismissDialog();
				showEnjoyMessage(prefs.DEVICE_ID, txtEmail.getText().toString(), txtContactPerson.getText().toString(), txtTelephone.getText().toString(), txtCVR.getText().toString());
			}
		});
		applyDialogLayoutParams(dialog);
		dialog.show();
	}

	private void showEnjoyMessage(final String devicetoken, final String email, final String contactPerson, final String phone, final String VATnumber) {
		final Dialog dialogEnjoy = new Dialog(this, android.R.style.Theme_Translucent);
		dialogEnjoy.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogEnjoy.setContentView(R.layout.dialog_enjoy);
		TextView txtMsg = (TextView) dialogEnjoy.findViewById(R.id.txtMsg);
		txtMsg.setText(R.string.enjoyMessage);
		Button btnNext = (Button) dialogEnjoy.findViewById(R.id.btnNext);
		btnNext.setText(R.string.goAhead);
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogEnjoy.dismiss();
				sendEmail(devicetoken, email, contactPerson, phone, VATnumber);
			}
		});
		applyDialogLayoutParams(dialogEnjoy);
		dialogEnjoy.show();
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
						else {
							Utility.showDialog(SplashActivity.this, "Fejl", "Server modtog ikke registeration info ordentligt. Prøv venligst igen.");
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
			if (event.getAction() == KeyEvent.ACTION_UP) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						// Perform action on key press
						LinearLayout linEmailDialog = (LinearLayout) dialog.findViewById(R.id.linEmailDialog);
						int i = Integer.parseInt(v.getTag().toString());
						if (i == 4) {
							// btnSendInformation.requestFocus();
							hideKeyboard(v);
						}
						else {
							String s = Integer.toString(i + 1);
							View viewToFocus = linEmailDialog.findViewWithTag(s);
							viewToFocus.requestFocus();
						}
						return true;
					default:
					break;
				}
			}
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						return true;
					default:
					break;
				}
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