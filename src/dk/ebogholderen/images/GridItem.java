package dk.ebogholderen.images;

import android.net.Uri;
import android.widget.ProgressBar;

public class GridItem {
	public Uri imgUri;
	public String category;
	public boolean isUploaded;
	public boolean isSelected;
	transient public ProgressBar pb;
}
