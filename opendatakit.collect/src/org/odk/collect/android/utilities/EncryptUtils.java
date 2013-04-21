package org.odk.collect.android.utilities;
/**
 * Small util used to encrypt the filename to avoid expose information
 */
import org.opendatakit.httpclientandroidlib.androidextra.Base64;

public class EncryptUtils {
	public static final String DEFAULT_ENCODING="UTF-8";
	
	public static String encodeString(String filename){
		String resultVal;
		byte[] data = filename.getBytes();
		char [] result = Base64.encodeToString(data, Base64.URL_SAFE).toCharArray();
		// avoid \n ending
		if (result[result.length -1] == '\n'){
			resultVal = new String (result, 0, result.length -2);
		}else {
			resultVal = new String (result);
		}
		// return the value
		return resultVal;
	}

}
