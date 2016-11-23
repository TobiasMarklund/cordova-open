package com.disusered;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.content.Intent;
import android.webkit.MimeTypeMap;
import android.content.ActivityNotFoundException;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * This class starts an activity for an intent to view files
 */
public class Open extends CordovaPlugin {

  private static final String TAG = "Open";

  public static final String OPEN_ACTION = "open";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals(OPEN_ACTION)) {
      String path = args.getString(0);
      this.chooseIntent(path, callbackContext);
      return true;
    }
    return false;
  }

  /**
   * Returns the MIME type of the file.
   *
   * @param path
   * @return
   */
  private static String getMimeType(String path) {
    String mimeType = null;

    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
    if (extension != null) {
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      mimeType = mime.getMimeTypeFromExtension(extension);
    }

    LOG.d(TAG, "Mime type: " + mimeType);

    return mimeType;
  }

  /**
   * Creates an intent for the data of mime type
   *
   * @param path
   * @param callbackContext
   */
  private void chooseIntent(String path, CallbackContext callbackContext) {
    if (path != null && path.length() > 0) {
      try {
        Uri uri = Uri.parse(path);
        String mime = getMimeType(path);
        Intent fileIntent = new Intent(Intent.ACTION_VIEW);

        if (uri.getScheme().equals("file")) {
          String authority = this.cordova.getActivity().getApplicationContext().getPackageName() + ".fileprovider";
          File f = new File(uri.getPath());
          uri = FileProvider.getUriForFile(this.cordova.getActivity(), authority, f);
          fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          LOG.d(TAG, "Resolved content uri: " + uri.toString());
        }
		
        if( Build.VERSION.SDK_INT > 15 ){
          fileIntent.setDataAndTypeAndNormalize(uri, mime); // API Level 16 -> Android 4.1
        } else {
          fileIntent.setDataAndType(uri, mime);
        }

        cordova.getActivity().startActivity(fileIntent);

        callbackContext.success();
      } catch (ActivityNotFoundException e) {
        LOG.e(TAG, "Could not open activity", e);
        callbackContext.error(1);
      }
    } else {
      callbackContext.error(2);
    }
  }
}
