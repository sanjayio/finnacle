package hpcoe.com.menuhelpdesk.utils;

import android.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This is a helper class to implement encryption and decryption.
 * This uses AES encryption/decryption using a 128-bit key.
 */
public class CryptIt {
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = "eoCisFpHisFpHeoC".getBytes();
    static Key key;

    /**
     * Method for encrypting data.
     * @param valueToEnc : Data to be encrypted in String
     * @return : An encrypted String containing the data.
     * @throws Exception
     */
    public static String encrypt(String valueToEnc) throws Exception {
        System.out.println((new String(keyValue)).length());
        key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);

        System.out.println("valueToEnc.getBytes().length "+valueToEnc.getBytes().length);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        System.out.println("encValue length" + encValue.length);
        byte[] encryptedByteValue = Base64.encode(encValue,Base64.DEFAULT);
        String encryptedValue = new String(encryptedByteValue);
        System.out.println("encryptedValue " + encryptedValue);

        return encryptedValue;
    }

    /**
     * Method for decrypting data.
     * @param encryptedValue : Encrypted data in String
     * @return : Decrypted data in String
     * @throws Exception
     */
    public static String decrypt(String encryptedValue) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue =Base64.decode(encryptedValue.getBytes(),Base64.DEFAULT);
        byte[] decryptedVal = c.doFinal(decodedValue);
        return new String(decryptedVal);
    }

    /**
     * Generates a key using the specified 128-bit byte array.
     * @return : Key
     * @throws Exception
     */
    private static Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, ALGORITHM);
    }
}
