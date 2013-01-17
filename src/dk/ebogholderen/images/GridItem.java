package dk.ebogholderen.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ProgressBar;

public class GridItem implements Parcelable
{
	public Uri imgUri;
	public String category;
	public int isUploaded;
	public int isSelected;
	public int tag;
	transient public ProgressBar pb;

	public GridItem() {
		;
	};

	public GridItem(Parcel in) {
		imgUri = in.readParcelable(new ClassLoader() {});
		category = in.readString();
		isUploaded = in.readInt();
		isSelected = in.readInt();
		tag = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(imgUri, PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeString(category);
		dest.writeInt(isUploaded);
		dest.writeInt(isSelected);
		dest.writeInt(tag);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public GridItem createFromParcel(Parcel in) {
			return new GridItem(in);
		}

		public GridItem[] newArray(int size) {
			return new GridItem[size];
		}
	};
}
