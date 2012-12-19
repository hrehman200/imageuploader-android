package dk.ebogholderen.images;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class Utility
{
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
	/**
	 * @param picUri
	 * @return
	 */
	public static File uriToFile(Context ctx, Uri picUri) {
		File image;
		if (picUri.isAbsolute())
			image = new File(Utility.getRealPathFromURI(ctx, picUri));
		else
			image = new File(picUri.toString());
		return image;
	}

	/*****************************************************************************************************/
	public static Bitmap getPreview(Context ctx, Uri imgUri) {
		int THUMBNAIL_SIZE = 100;
		File image = null;
		if (imgUri.isAbsolute())
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

	/*****************************************************************************************************/
	public static void serializeData(String filepath, String filename, String json) {
		FileOutputStream fos;
		try {
			File f = new File(filepath);
			if (!f.exists())
				f.mkdirs();
			f = new File(filepath, filename);
			if (!f.exists())
				f.createNewFile();
			fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(json);
			oos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*****************************************************************************************************/
	@SuppressWarnings("unchecked")
	public static String deserializeData(String filepath, String filename) {
		String s = "";
		try {
			File f = new File(filepath);
			if (!f.exists())
				f.mkdirs();
			f = new File(filepath, filename);
			if (!f.exists())
				f.createNewFile();
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			s = (String) ois.readObject();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return s;
	}

	/*****************************************************************************************************/
	/**
	 * Validate email with regular expression
	 * 
	 * @param String email The email we want to validate
	 * @return true if email is valid, false otherwise
	 */
	public static boolean isValidEmail(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/*****************************************************************************************************/
	/**
	 * Copy file from source to destination address.
	 * 
	 * @param File source The source file object from where to copy.
	 * @param File destination The destination file object.
	 * @return true if file copied, false otherwise.
	 */
	public static boolean copyFile(File source, File destination) {
		boolean isCopied = false;
		if (source.exists()) {
			try {
				FileChannel src = new FileInputStream(source).getChannel();
				FileChannel dst = new FileOutputStream(destination).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				isCopied = true;
			}
			catch (FileNotFoundException e) {
				isCopied = false;
				e.printStackTrace();
			}
			catch (IOException e) {
				isCopied = false;
				e.printStackTrace();
			}
		}
		return isCopied;
	}
}
