package org.odk.collect.android.utilities;

/**
 * Small util used to encrypt the filename to avoid expose information
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.application.Collect;
import org.opendatakit.httpclientandroidlib.androidextra.Base64;

import android.util.Log;

public class EncryptUtils {
	private static final String TAG = "ODK-ENCRYPT-UTIL";
	private static final String DEFAULT_ENCODING = "UTF-8";
	// key constants used for cipher encrypt
	private static final String PBE_ALGORITHM = "PBEWithSHA256And256BitAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int NUM_OF_ITERATIONS = 1000;
	private static final int KEY_SIZE = 256;
	private static final byte[] salt = "axsdqwrqwskalsjfjspske".getBytes();
	private static final byte[] iv = "12345678910abcds".getBytes();
	//public static Cipher dataEncryptor;
	//public static Cipher dataDecryptor;

	// initialize encryptor and decryptor for later use
//	static {
//		try {
////			PBEKeySpec pbeKeySpec = new PBEKeySpec(
////					Collect.KEY_ENCRYPT_DATA.toCharArray(), salt,
////					NUM_OF_ITERATIONS, KEY_SIZE);
////			SecretKeyFactory keyFactory = SecretKeyFactory
////					.getInstance(PBE_ALGORITHM);
////			SecretKey tempKey = keyFactory.generateSecret(pbeKeySpec);
////			SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
////			IvParameterSpec ivSpec = new IvParameterSpec(iv);
////			dataEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
////			dataDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
////			dataEncryptor.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
////			dataDecryptor.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.d(TAG, "Failed to initialize");
//		}
//
//	}
	/**
	 * helper used to read byte array 
	 * @param f
	 * @return byte array of data
	 * @throws IOException 
	 */
	public static byte[] readFile(File f) throws IOException{
		FileInputStream is = null;
		byte [] val = null;
		try {
			is = new FileInputStream(f);
			val = IOUtils.toByteArray(is);		
		}
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG,"File not found encrypted error\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG,"IO file encrypted error\n");
		}finally{
			if (is != null) {is.close();}
		}
		
		return val;
	}

	/**
	 * encrypt data to store in disk
	 * 
	 * @param data
	 * @return
	 * @throws IOException 
	 */
	public static void encryptedData(File f) throws IOException {
		// Cipher encCipher = getCipherInstances(Cipher.ENCRYPT_MODE);
		FileOutputStream output = null;
		try {
			PBEKeySpec pbeKeySpec = new PBEKeySpec(
					Collect.KEY_ENCRYPT_DATA.toCharArray(), salt,
					NUM_OF_ITERATIONS, KEY_SIZE);
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(PBE_ALGORITHM);
			SecretKey tempKey = keyFactory.generateSecret(pbeKeySpec);
			SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher dataEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			dataEncryptor.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] data = readFile(f);
			if (data != null){
				
				byte[] encrypteddata = dataEncryptor.doFinal(data);
				// get the path
				String fname = f.getAbsolutePath();
				// delete the old files
				f.delete();
				// write the encrypted data back to disk
				output = new FileOutputStream(new File(fname));
				IOUtils.write(encrypteddata, output);
				output.flush();
			}
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.e(TAG, "illegal block size in encrypted data");
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.e(TAG, "bad padding output data");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "file not found output data");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "IO error output data");
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if (output != null ) { output.close();}
		}
	}

	/**
	 * decrypt stored encrypt data
	 * 
	 * @param data
	 *            - blocks of data need to be decrypted
	 * @return
	 */
	public static byte[] decryptedFiletoByteArray(File f) {
		// Cipher encCipher = getCipherInstances(Cipher.ENCRYPT_MODE);
		try {
			FileInputStream is = new FileInputStream(f);
			// initialize for decrypt
			PBEKeySpec pbeKeySpec = new PBEKeySpec(
					Collect.KEY_ENCRYPT_DATA.toCharArray(), salt,
					NUM_OF_ITERATIONS, KEY_SIZE);
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(PBE_ALGORITHM);
			SecretKey tempKey = keyFactory.generateSecret(pbeKeySpec);
			SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher dataDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			dataDecryptor.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			if (is != null){
				// make a buffer of appropriate size
				byte[] encryptedData = new byte[(int) f.length()];
				IOUtils.readFully(is, encryptedData);
				is.close();
				return dataDecryptor.doFinal(encryptedData);
			}
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Log.e(TAG, "illegal block size in encrypted data");
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Log.e(TAG, "Bad badding deccrypt file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "file not found decrypting file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "I/o exception decrypt file");
		}catch (Exception e){
			e.printStackTrace();
			Log.w(TAG, "Exception decryption: "+e);			
		}
		// failed to encrypted data
		return null;
	}

	public static String encodeString(String filename) {
		String resultVal;
		byte[] data = filename.getBytes();
		char[] result = Base64.encodeToString(data, Base64.URL_SAFE)
				.toCharArray();
		// avoid \n ending
		if (result[result.length - 1] == '\n') {
			resultVal = new String(result, 0, result.length - 2);
		} else {
			resultVal = new String(result);
		}
		// return the value
		return resultVal;
	}

}
