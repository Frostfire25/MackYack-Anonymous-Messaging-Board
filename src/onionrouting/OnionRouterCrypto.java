package onionrouting;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import merrimackutil.util.Pair;

public class OnionRouterCrypto {
    
    /**
     * Generates a <Public, Private> key pair for asymetric encryption using Elgamal 
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Pair<String> generateAsymKeys() throws NoSuchAlgorithmException
    {
        // Add the Bouncy Castle provider from The Legion of
            // the Bouncy Castle.
        Security.addProvider(new BouncyCastleProvider());

        // Generate a pair of keys. Using Elgamal key generation.
        // The size of the key should be 512-bits. Anything smaller is
        // too small for practical purposes.
        KeyPairGenerator elgamalKeyGen = KeyPairGenerator.getInstance("ElGamal");
        elgamalKeyGen.initialize(512);
        KeyPair elgamalPair = elgamalKeyGen.generateKeyPair();

        // Get the public and private key pair from the generated
            // pair.
        PublicKey pubKey = elgamalPair.getPublic();
        PrivateKey privKey = elgamalPair.getPrivate();

        return new Pair<String>(Base64.getEncoder().encodeToString(pubKey.getEncoded()), Base64.getEncoder().encodeToString(privKey.getEncoded()));
    }

}
