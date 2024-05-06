package mackyack_client;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
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
