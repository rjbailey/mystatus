package edu.washington.cs.mystatus.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

/**
 * This class provide functionalities for encrypting stored data such as: XML
 * forms and Media files...
 * 
 * @author: IT solutions
 * @source: 
 *          http://www.itcsolutions.eu/2011/08/24/how-to-encrypt-decrypt-files-in
 *          -java-with-aes-in-
 *          cbc-mode-using-bouncy-castle-api-and-netbeans-or-eclipse/
 * 
 */
public class DataEncryptionUtils {
	PaddedBufferedBlockCipher encryptCipher = null;
	PaddedBufferedBlockCipher decryptCipher = null;

	// Buffer used to transport the bytes from one stream to another
	byte[] buf = new byte[16]; // input buffer
	byte[] obuf = new byte[512]; // output buffer
	// The key
	byte[] key = null;
	// The initialization vector needed by the CBC mode
	byte[] IV = null;

	// The default block size
	public static int blockSize = 16;

	/**
	 * default constructor -- rarely used
	 */
	public DataEncryptionUtils() {
		// default 192 bit key
		key = "SECRET_1SECRET_2SECRET_3".getBytes();
		// default IV vector with all bytes to 0
		IV = new byte[blockSize];
	}

	/**
	 * should use this constructor
	 * 
	 * @param keyBytes
	 */
	public DataEncryptionUtils(byte[] keyBytes) {
		// get the key
		key = new byte[keyBytes.length];
		System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);

		// default IV vector with all bytes to 0
		IV = new byte[blockSize];
	}

	public DataEncryptionUtils(byte[] keyBytes, byte[] iv) {
		// get the key
		key = new byte[keyBytes.length];
		System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);

		// get the IV
		IV = new byte[blockSize];
		System.arraycopy(iv, 0, IV, 0, iv.length);
	}

	public void InitCiphers() {
		// create the ciphers
		// AES block cipher in CBC mode with padding
		encryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(
				new AESEngine()));

		decryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(
				new AESEngine()));

		// create the IV parameter
		ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(
				key), IV);

		encryptCipher.init(true, parameterIV);
		decryptCipher.init(false, parameterIV);
	}
	/**
	 * method for encrypting data
	 * @param in
	 * @param out
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws DataLengthException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IOException
	 */
	public void CBCEncrypt(InputStream in, OutputStream out)
			throws ShortBufferException, IllegalBlockSizeException,
			BadPaddingException, DataLengthException, IllegalStateException,
			InvalidCipherTextException, IOException {
		// Bytes written to out will be encrypted
		// Read in the cleartext bytes from in InputStream and
		// write them encrypted to out OutputStream

		// optionaly put the IV at the beggining of the cipher file
		// out.write(IV, 0, IV.length);

		int noBytesRead = 0; // number of bytes read from input
		int noBytesProcessed = 0; // number of bytes processed

		while ((noBytesRead = in.read(buf)) >= 0) {
			// System.out.println(noBytesRead +" bytes read");

			noBytesProcessed = encryptCipher.processBytes(buf, 0, noBytesRead,
					obuf, 0);
			// System.out.println(noBytesProcessed +" bytes processed");
			out.write(obuf, 0, noBytesProcessed);
		}

		// System.out.println(noBytesRead +" bytes read");
		noBytesProcessed = encryptCipher.doFinal(obuf, 0);

		// System.out.println(noBytesProcessed +" bytes processed");
		out.write(obuf, 0, noBytesProcessed);

		out.flush();

		in.close();
		out.close();
	}
	
	
	
	
	/**
	 * method for decrypting to a given output stream
	 * @param in
	 * @param out
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws DataLengthException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IOException
	 */
	public void CBCDecrypt(InputStream in, OutputStream out)
			throws ShortBufferException, IllegalBlockSizeException,
			BadPaddingException, DataLengthException, IllegalStateException,
			InvalidCipherTextException, IOException {
		int noBytesRead = 0; // number of bytes read from input
		int noBytesProcessed = 0; // number of bytes processed

		while ((noBytesRead = in.read(buf)) >= 0) {
			// System.out.println(noBytesRead +" bytes read");
			noBytesProcessed = decryptCipher.processBytes(buf, 0, noBytesRead,
					obuf, 0);
			// System.out.println(noBytesProcessed +" bytes processed");
			out.write(obuf, 0, noBytesProcessed);
		}
		// System.out.println(noBytesRead +" bytes read");
		noBytesProcessed = decryptCipher.doFinal(obuf, 0);
		// System.out.println(noBytesProcessed +" bytes processed");
		out.write(obuf, 0, noBytesProcessed);

		out.flush();

		in.close();
		out.close();
	}
	
	/**
	 * method for decrypting data to byte array
	 * @param in
	 * @param out
	 * @throws ShortBufferException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws DataLengthException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IOException
	 */
	public byte[] CBCDecryptAsByteArray(InputStream in, long size)
			throws ShortBufferException, IllegalBlockSizeException,
			BadPaddingException, DataLengthException, IllegalStateException,
			InvalidCipherTextException, IOException {
		int noBytesRead = 0; // number of bytes read from input
		int noBytesProcessed = 0; // number of bytes processed
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) size);
		//int totalProcesses = 0;
		while ((noBytesRead = in.read(buf)) >= 0) {
			noBytesProcessed = decryptCipher.processBytes(buf, 0, noBytesRead,
					obuf, 0);
			byteBuffer.put(obuf, 0, noBytesProcessed);
		}
		// last byte processed
		noBytesProcessed = decryptCipher.doFinal(obuf, 0);
		// final copy
		byteBuffer.put(obuf, 0, noBytesProcessed);
		in.close();
		return byteBuffer.array();
	}
}
