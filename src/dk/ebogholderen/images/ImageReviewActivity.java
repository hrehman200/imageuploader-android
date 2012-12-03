package dk.ebogholderen.images;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageReviewActivity extends Activity implements OnClickListener {
	ImageView imgView;
	Button btnSend, btnIncomeInBank, btnSpendingCash, btnExpenseInBank, btnOther;
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
		btnSend.setEnabled(false);
		//
		btnIncomeInBank = (Button) findViewById(R.id.btnIncomeInBank);
		btnIncomeInBank.setOnClickListener(this);
		//
		btnSpendingCash = (Button) findViewById(R.id.btnSpendingCash);
		btnSpendingCash.setOnClickListener(this);
		//
		btnExpenseInBank = (Button) findViewById(R.id.btnExpenseInBank);
		btnExpenseInBank.setOnClickListener(this);
		//
		btnOther = (Button) findViewById(R.id.btnOther);
		btnOther.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSend:
				File image = Utility.uriToFile(ImageReviewActivity.this, imgUri);
				File renamedFile = new File(MainActivity.SAVE_PATH, String.format("%s_%d.jpg", v.getTag().toString().replace(" ", "_"), new Date().getTime()));
				if (image.renameTo(renamedFile)) {
					Intent i = new Intent();
					i.putExtra("img", renamedFile.getAbsolutePath());
					i.putExtra("category", v.getTag().toString());
					setResult(RESULT_OK, i);
					finish();
				}
			break;
			case R.id.btnIncomeInBank:
				btnSend.setTag(btnIncomeInBank.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnSpendingCash:
				btnSend.setTag(btnSpendingCash.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnExpenseInBank:
				btnSend.setTag(btnExpenseInBank.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnOther:
				btnSend.setTag(btnOther.getText());
				btnSend.setEnabled(true);
			break;
		}
	}
}
