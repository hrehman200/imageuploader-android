package dk.ebogholderen.images;

import java.util.ArrayList;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	public static ArrayList<Uri> arrImages = new ArrayList<Uri>();

	public ImageAdapter(Context context) {
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View gridView;
		if (convertView == null) {
			gridView = new View(context);
			// get layout from mobile.xml
			gridView = inflater.inflate(R.layout.grid_item, null);
			// set image based on selected text
			ImageView imgView = (ImageView) gridView.findViewById(R.id.imgThumb);
			Uri uri = arrImages.get(position);
			imgView.setImageURI(uri);
		}
		else {
			gridView = (View) convertView;
		}
		return gridView;
	}

	@Override
	public int getCount() {
		if (arrImages != null) {
			return arrImages.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
