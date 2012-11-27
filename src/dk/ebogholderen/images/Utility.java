package dk.ebogholderen.images;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;

public class Utility {
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATETIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
	public static final String MIME_TYPE_PDF = "application/pdf";

	//
	public static String getUDID(Context c) {
		TelephonyManager telMngr = (TelephonyManager) c.getSystemService(c.TELEPHONY_SERVICE);
		return telMngr.getDeviceId().toString();
	}

	/*****************************************************************************************************/
	public static Message createMessage(int what, Object obj) {
		Message m = new Message();
		m.what = what;
		m.obj = obj;
		return m;
	}

	/*****************************************************************************************************/
	public static boolean isTokenExpired(String expiryDate) {
		Date dateCurrent = new Date();
		Date dateExpiry = strToDate(expiryDate);
		if (dateExpiry.after(dateCurrent))
			return false;
		return true;
	}

	/*****************************************************************************************************/
	public static String dateToString(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		String formattedDate = formatter.format(d);
		return formattedDate;
	}

	/*****************************************************************************************************/
	public static Date strToDate(String s) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		Date d = null;
		try {
			d = formatter.parse(s);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/*****************************************************************************************************/
	public static String dateTimeToString(Date d) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
		String formattedDate = formatter.format(d);
		return formattedDate;
	}

	/*****************************************************************************************************/
	public static Date strToDateTime(String s) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
		Date d = null;
		try {
			d = formatter.parse(s);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/*****************************************************************************************************/
	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*****************************************************************************************************/
	public static String readInputStreamAsString(InputStream in) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			byte b = (byte) result;
			buf.write(b);
			result = bis.read();
		}
		return buf.toString();
	}

	/*****************************************************************************************************/
	public static String arrayToString(String[] arr, String separator) {
		StringBuffer result = new StringBuffer();
		if (arr.length > 0) {
			result.append(arr[0]);
			for (int i = 1; i < arr.length; i++) {
				result.append(separator);
				result.append(arr[i]);
			}
		}
		return result.toString();
	}

	/*****************************************************************************************************/
	public static boolean isNetAvailable(Context c) {
		boolean isWifiConnected = false;
		boolean isMobileConnected = false;
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					isWifiConnected = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					isMobileConnected = true;
		}
		return isWifiConnected || isMobileConnected;
	}

	/*****************************************************************************************************/
	public static int getItemPositionInCursor(Cursor c, String item, String itemColumn) {
		int itemPosition = 0;
		if (c != null) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String tempItem = c.getString(c.getColumnIndex(itemColumn));
				if (tempItem.equalsIgnoreCase(item)) {
					itemPosition = c.getPosition();
					break;
				}
				c.moveToNext();
			}
			// dont close cursor here as it is being passed by reference from other activity where it will be used
			c.moveToFirst();
		}
		return itemPosition;
	}

	/*****************************************************************************************************/
	public String stipHtml(String html) {
		return Html.fromHtml(html).toString();
	}

	/*****************************************************************************************************/
	public static String streamToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			Log.v("lll", "line");
			while ((line = reader.readLine()) != null) {
				Log.v("ddd", line);
				sb.append(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				is.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/*****************************************************************************************************/
	public static String getLastBitFromUrl(final String url) {
		// return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
		return url.replaceFirst(".*/([^/?]+).*", "$1");
	}

	/*****************************************************************************************************/
	public static void logCursor(Cursor c) {
		while (!c.isAfterLast()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < c.getColumnCount(); i++) {
				sb.append(String.format("%s=%s, ", c.getColumnName(i), c.getString(i)));
			}
			Log.v("cusor", sb.toString());
			c.moveToNext();
		}
		// move to first or else exception will occur if we try to get data
		c.moveToFirst();
	}

	/*****************************************************************************************************/
	public static String getRealPathFromURI(Context ctx, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(ctx, contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/*****************************************************************************************************/
	public static Bitmap getPreview(Context ctx, Uri imgUri) {
		int THUMBNAIL_SIZE = 100;
		File image = null;
		if(imgUri.isAbsolute())
			image = new File(Utility.getRealPathFromURI(ctx, imgUri));
		else
			image = new File(imgUri.toString());
		Log.v("getPreview", image.getAbsolutePath());
		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), bounds);
		if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
			return null;
		int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
		return BitmapFactory.decodeFile(image.getPath(), opts);
	}
}
