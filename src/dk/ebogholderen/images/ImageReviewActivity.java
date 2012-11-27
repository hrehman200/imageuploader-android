package dk.ebogholderen.images;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageReviewActivity extends Activity implements OnClickListener {
	ImageView imgView;
	Button btnSend;
	Uri imgUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imgreview);
		//
		String img = (String) getIntent().getExtras().get("img");
		imgUri = Uri.parse(img);
		//
		imgView = (ImageView) findViewById(R.id.imgReview);
		imgView.setImageURI(imgUri);
		//
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSend:
				Intent i = new Intent();
				i.putExtra("img", imgUri.toString());
				setResult(RESULT_OK, i);
				finish();
			break;
		}
	}
}
