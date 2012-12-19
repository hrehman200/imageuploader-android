package dk.ebogholderen.images;

import java.io.File;
import java.util.Date;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageReviewActivity extends Activity implements OnClickListener
{
	ImageView imgView;
	Button btnSend, btnIncomeInBank, btnSpendingCash, btnExpenseInBank, btnOther;
	Uri imgUri;
	Drawable originalDrawable;

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
		originalDrawable = btnSend.getBackground();
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
		deselectAllButtons();
		switch (v.getId()) {
			case R.id.btnSend:
				File image = Utility.uriToFile(ImageReviewActivity.this, imgUri);
				String imgTag = v.getTag().toString().replace(" ", "_");
				imgTag = imgTag.replace("\n", "");
				File renamedFile = new File(MainActivity.SAVE_PATH, String.format("%s_%d.jpg", imgTag, new Date().getTime()));
				// if its a gallery image, copy it to our app folder
				if (imgUri.isAbsolute()) {
					Utility.copyFile(image, renamedFile);
					Intent i = new Intent();
					i.putExtra("img", renamedFile.getAbsolutePath());
					i.putExtra("category", v.getTag().toString());
					setResult(RESULT_OK, i);
					finish();
				}
				// else rename it to proper category
				else if (image.renameTo(renamedFile)) {
					Intent i = new Intent();
					i.putExtra("img", renamedFile.getAbsolutePath());
					i.putExtra("category", v.getTag().toString());
					setResult(RESULT_OK, i);
					finish();
				}
				else {
					Toast.makeText(ImageReviewActivity.this, "Failed in renaming file", Toast.LENGTH_LONG).show();
				}
			break;
			case R.id.btnIncomeInBank:
				btnIncomeInBank.setBackgroundResource(R.drawable.button_bg_border);
				btnSend.setTag(btnIncomeInBank.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnSpendingCash:
				btnSpendingCash.setBackgroundResource(R.drawable.button_bg_border);
				btnSend.setTag(btnSpendingCash.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnExpenseInBank:
				btnExpenseInBank.setBackgroundResource(R.drawable.button_bg_border);
				btnSend.setTag(btnExpenseInBank.getText());
				btnSend.setEnabled(true);
			break;
			case R.id.btnOther:
				btnOther.setBackgroundResource(R.drawable.button_bg_border);
				btnSend.setTag(btnOther.getText());
				btnSend.setEnabled(true);
			break;
		}
	}

	private void deselectAllButtons() {
		btnIncomeInBank.setBackgroundDrawable(originalDrawable);
		btnSpendingCash.setBackgroundDrawable(originalDrawable);
		btnExpenseInBank.setBackgroundDrawable(originalDrawable);
		btnOther.setBackgroundDrawable(originalDrawable);
	}
}
