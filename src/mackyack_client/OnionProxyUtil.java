package mackyack_client;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import merrimackutil.util.Pair;

public class OnionProxyUtil {

    /**
     * Function to encrypt a message given an AES key and 16-bit salt.
     * @param message String plaintext-message to be encrypted
     * @param aesKey AES Key that will be used to encrypt the message
     * @param rawIV byte[] 16-bit IV that is used to make the encryption non-deterministic
     * @return Result of the encryption as a String.

     */
    public static String encryptSymmetric(String message, Key aesKey, byte[] rawIV) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Fill array with random bytes.
        IvParameterSpec iv = new IvParameterSpec(rawIV);
                                          
        // Put the cipher in encrypt mode with the generated key 
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);

        // Encrypt the entire message at once. The doFinal method 
        byte[] ciphertext = aesCipher.doFinal(message.getBytes());

        return Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * Function to decrypt a message given an AES key and 16-bit salt.
     * @param message String plaintext-message to be encrypted
     * @param aesKey AES Key that will be used to encrypt the message
     * @param rawIV byte[] 16-bit IV that is used to make the encryption non-deterministic
     * @return Result of the decryption as a String
     */
    public static String decryptSymmetric(String message, Key aesKey, byte[] rawIV) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Fill array with random bytes.
        IvParameterSpec iv = new IvParameterSpec(rawIV);
                                          
        // Put the cipher in encrypt mode with the generated key 
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, iv);

        // Encrypt the entire message at once. The doFinal method 
        byte[] plaintext = aesCipher.doFinal(Base64.getDecoder().decode(message));

        return new String(plaintext);
    }

    /**
     * Decodes from Base64 encoding and returns Public Key object.
     * 
     * @param str base 64 encoding.
     * @return Public Key object representation.
     * @throws InvalidKeySpecException 
     * @throws NoSuchAlgorithmException 
     */
    public static PublicKey getPublicKey(String algorithm, String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the base64 encoded public key string
        byte[] publicKeyBytes = Base64.getDecoder().decode(str);

        // Create an X509EncodedKeySpec object from the decoded bytes
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        // Get an instance of the KeyFactory for ElGamal algorithm
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        // Generate the PublicKey object using the KeyFactory
        return keyFactory.generatePublic(keySpec);
    }

        /**
     * Decodes from Base64 encoding and returns Private Key object.
     * 
     * @param str base 64 encoding.
     * @return Private Key object representation.
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */
    public static PrivateKey getPrivateKey(String algorithm, String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the base64 encoded private key string
        byte[] privateKeyBytes = Base64.getDecoder().decode(str);

        // Create a PKCS8EncodedKeySpec object from the decoded bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // Get an instance of the KeyFactory for ElGamal algorithm
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        // Generate the PrivateKey object using the KeyFactory
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Constructs a B64Encoded(key):B64Encoded(IV) that represents a Symmetric Key+IV pair.
     * 
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public static Pair<String> encryptHybrid(byte[] data) throws
        NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException, InvalidAlgorithmParameterException
    {
        Cipher aesCipher;                // The cipher object
        KeyGenerator aesKeyGen;          // The AES keygenerator.
        SecureRandom rand;               // A secure random number generator.
        byte[] rawIV = new byte[16];     // An AES init. vector.
        IvParameterSpec iv;              // The IV parameter for CBC. Different ciphers
                                         // may have different specifications.
                            
        // Set up an AES cipher object.
        aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Get a key generator object and set the key size to 128 bits.
        aesKeyGen = KeyGenerator.getInstance("AES");
        aesKeyGen.init(128);

        // Generate the key.
        Key aesKey = aesKeyGen.generateKey();

        // Generate the IV for CBC mode.
        rand = new SecureRandom();
        rand.nextBytes(rawIV);          // Fill array with random bytes.
        iv = new IvParameterSpec(rawIV);

        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);

        byte[] ciphertext = aesCipher.doFinal(data);

        
        //return new Pair<byte[]>(aesKey.getEncoded(), iv.getIV());
        //return new Pair<String>(Base64.getEncoder().encodeToString(aesKey.getEncoded()), Base64.getEncoder().encodeToString(iv.getIV()));
        //Base64.getEncoder().encodeToString(aesKey.getEncoded()) + ":" + Base64.getEncoder().encodeToString(iv.getIV());

        return new Pair<String>(
            Base64.getEncoder().encodeToString(aesKey.getEncoded()) + ":" + Base64.getEncoder().encodeToString(iv.getIV()), // Symmetric Key:IV pair as a String
            Base64.getEncoder().encodeToString(ciphertext)                                                                  // CipherText encoded to String
        );
    }
}
