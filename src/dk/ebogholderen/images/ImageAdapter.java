package dk.ebogholderen.images;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	public static ArrayList<GridItem> arrImages = new ArrayList<GridItem>();

	public ImageAdapter(Context context) {
		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = convertView;
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_main, null);
		ViewGroup grid = (ViewGroup) viewGroup.findViewById(R.id.gridImages);
		GridItemView holder = null;
		if (itemView == null) {
			itemView = inflater.inflate(R.layout.grid_item, grid, false);
			holder = new GridItemView();
			holder.imageView = (ImageView) itemView.findViewById(R.id.imgThumb);
			holder.progressBar = (ProgressBar) itemView.findViewById(R.id.imgProgress);
			holder.imgSelect = (ImageView) itemView.findViewById(R.id.imgSelect);
			holder.imgDelete = (ImageView) itemView.findViewById(R.id.imgDelete);
			itemView.setTag(holder);
		}
		else {
			holder = (GridItemView) itemView.getTag();
		}
		GridItem gridItem = arrImages.get(position);
		//holder.imageView.setImageURI(gridItem.imgUri);
		holder.imageView.setImageBitmap(Utility.getPreview(context, gridItem.imgUri));
		holder.imgSelect.setTag(position);
		holder.imgDelete.setTag(position);
		gridItem.pb = holder.progressBar;
		if(gridItem.isUploaded)
			gridItem.pb.setProgress(100);
		else
			gridItem.pb.setProgress(0);
		//
		View parentView = (View)gridItem.pb.getParent();
		if(gridItem.isSelected)
			parentView.setBackgroundResource(R.drawable.griditem_border);
		else
			parentView.setBackgroundDrawable(null);
		return itemView;
	}

	@Override
	public int getCount() {
		if (arrImages != null) {
			return arrImages.size();
		}
		return 0;
	}

	@Override
	public GridItem getItem(int position) {
		return arrImages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
