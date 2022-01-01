package Client;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Crypto {
    private static final String AES = "AES";
    private static final int keySize = 256;
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";

    public static SecretKey createAESKey() throws Exception {
        SecureRandom securerandom = new SecureRandom();
        KeyGenerator keygenerator = KeyGenerator.getInstance(AES);
        keygenerator.init(keySize, securerandom);
        return keygenerator.generateKey();
    }

    public static byte[] createInitializationVector(SecretKey key) {
        // Used with encryption
        byte[] initializationVector = new byte[16];
//        SecureRandom secureRandom = new SecureRandom(key.getEncoded());
//        secureRandom.nextBytes(initializationVector);
        return initializationVector;
    }

    public static byte[] doAESEncryption(String plainText, SecretKey secretKey) throws Exception {
        byte[] initializationVector = createInitializationVector(secretKey);
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(plainText.getBytes());
    }

    public static String doAESDecryption(byte[] cipherText, SecretKey secretKey) throws Exception {
        byte[] initializationVector = createInitializationVector(secretKey);
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        byte[] result = cipher.doFinal(cipherText);
        return new String(result);
    }

    public static SecretKey deriveKey(SecretKey pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[64];
        String encodedKey = Base64.getEncoder().encodeToString(pass.getEncoded());
        char[] password = encodedKey.toCharArray();
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec specs = new PBEKeySpec(password, salt, 1024, keySize);
        SecretKey key = kf.generateSecret(specs);
        byte[] newKey = key.getEncoded();
        // We maken een nieuwe sleutel met de byte waarden van de vorige zodat het bijhorende algoritme klopt
        return new SecretKeySpec(newKey, 0, newKey.length, AES);
    }

}
