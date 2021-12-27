package Client;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Crypto {
    private static final String AES = "AES";
    private static final int keySize = 192;
    // We are using a Block cipher(CBC mode)
    private static final String AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";

    // Function to create a secret key
    public static SecretKey createAESKey() throws Exception {
        // Creating a new instance of
        // SecureRandom class.
        SecureRandom securerandom = new SecureRandom();

        // Passing the string to
        // KeyGenerator
        KeyGenerator keygenerator = KeyGenerator.getInstance(AES);

        // Initializing the KeyGenerator
        // with 256 bits.
        keygenerator.init(keySize, securerandom);
        SecretKey key = keygenerator.generateKey();

        System.out.println("KEY = " + DatatypeConverter.printHexBinary(key.getEncoded()));

        return key;
    }

    //TODO: FIX (dit mag denk ik niet hard gecodeert maar werkt voorlopig anders niet)
    // Function to initialize a vector with an arbitrary value
    public static byte[] createInitializationVector(SecretKey key) {
        // Used with encryption
        byte[] initializationVector = new byte[16];
//        SecureRandom secureRandom = new SecureRandom(key.getEncoded());
//        secureRandom.nextBytes(initializationVector);
//
//        System.out.println("INITIALIZATION VECTOR: " + Arrays.toString(initializationVector));

        return initializationVector;
    }

    // This function takes plaintext, the key with an initialization vector to convert plainTex into CipherText.
    public static byte[] doAESEncryption(String plainText, SecretKey secretKey) throws Exception {
        byte[] initializationVector = createInitializationVector(secretKey);
        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        return cipher.doFinal(plainText.getBytes());
    }

    //This function performs the reverse operation of the do_AESEncryption function.
    // It converts ciphertext to the plaintext using the key.
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
