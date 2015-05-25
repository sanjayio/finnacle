package hpcoe.com.menuhelpdesk.utils;

import android.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Messi10 on 21-May-15.
 */
public class CryptIt {
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = "eoCisFpHisFpHeoCeoCisFpHisFpHeoC".getBytes();
    static Key key;

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

    public static String decrypt(String encryptedValue) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue =Base64.decode(encryptedValue.getBytes(),Base64.DEFAULT);
        byte[] decryptedVal = c.doFinal(decodedValue);
        return new String(decryptedVal);
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }
}
