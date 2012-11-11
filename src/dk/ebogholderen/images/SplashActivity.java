package dk.ebogholderen.images;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class SplashActivity extends Activity implements OnClickListener {
	ImageView imgSplash;
	Handler handler;
	SplashHandler splashHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		//
		handler = new Handler();
		splashHandler = new SplashHandler();
		handler.postDelayed(splashHandler, 3000);
		//
		imgSplash = (ImageView) findViewById(R.id.imgSplash);
		imgSplash.setOnClickListener(this);
	}

	protected void onResume() {
		super.onResume();
	}

	class SplashHandler implements Runnable {
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
		handler.removeCallbacks(splashHandler);
		Intent i = new Intent(SplashActivity.this, MainActivity.class);
		startActivity(i);
		SplashActivity.this.finish();
	}
}