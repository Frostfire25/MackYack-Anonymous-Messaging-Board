package tests;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import onionrouting.OnionRouterService;
import onionrouting.onionrouter_cells.CreateCell;
import onionrouting.onionrouter_cells.CreatedCell;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;

public class OnionTests {
    
    @Test
    public void testOnionIsNotEmpty() {
        String onion = "Onion";
        assertFalse(onion.isEmpty());
    }

    @Test
    public void testIfElGamalExists() throws NoSuchAlgorithmException, NoSuchPaddingException {
        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance("ElGamal/None/NoPadding");

        System.out.println("Success");
    }

    @Test
    public void testCreateCell() throws UnknownHostException, IOException, NoSuchAlgorithmException,
    InvalidKeySpecException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException,
    BadPaddingException, NoSuchPaddingException {
        // Add the BCProvider
        Security.addProvider(new BouncyCastleProvider());

        // Initialize tables to see values after execution.
        // KeyTable -- Used for looking up symmetric keys associated with circuit IDs.
        // FwdTable -- Used for finding + forwarding the proper circuit ID to the next OR in the sequence.
        ConcurrentHashMap<Integer, Key> keyTable = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> fwdTable = new ConcurrentHashMap<>();

        // Set up the pub/priv key to be the pub/priv keys for router 0 (testing, so we can write it in w/o config files)
        PrivateKey privKey = getPrivateKey("MIHbAgEAMIGQBgYrDgcCAQEwgYUCQQD8poLOjhLKuibvzPcRDlJtsHiwXt7LzR60ogjzrhYXrgHzW5Gkfm32NBPF4S7QiZvNEyrNUNmRUb3EPuc3WS4XAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykBEMCQQDG2husnhV1cYYIS/XYhbHONvIRLCOg2Dakq42gTQ39ANffdAiQBKD/vqZZxovXYZVGOHPWuxXKaK6Mlh1Yt3Cz");
        PublicKey pubKey = getPublicKey("MIHZMIGQBgYrDgcCAQEwgYUCQQD8poLOjhLKuibvzPcRDlJtsHiwXt7LzR60ogjzrhYXrgHzW5Gkfm32NBPF4S7QiZvNEyrNUNmRUb3EPuc3WS4XAkBnhHGyepz0TukaScUUfbGpqvJE8FpDTWSGkx0tFCcbnjUDC3H9c9oXkGmzLik1Yw4cIGI1TQ2iCmxBblC+eUykA0QAAkEA6kte8f+YXQaUBfLdfB1eUfigD/DcEVtYDTCfntAAF4RdORWNhhKewzcqcN0aL/oy99aEQGB1LN80pno73B3nUQ==");

        // Start the server socket in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(5010); // Setting up server for p2p testing
                Socket sock = server.accept();
                OnionRouterService ors = new OnionRouterService(keyTable, fwdTable, sock, privKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start(); // Start the server thread

        // Allow some time for the server to start before connecting the test socket
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) { }


        // Set up a fake socket connection
        try (Socket testSocket = new Socket("127.0.0.1", 5010)) { // Connect to our server (that's polling)

            // Initialize the I/O
            Scanner in = new Scanner(testSocket.getInputStream());
            PrintWriter out = new PrintWriter(testSocket.getOutputStream(), true);

            // 1. Generate the first half of the DH KEX.
            KeyAgreement ecdhKex = KeyAgreement.getInstance("ECDH"); // Eliptic Curve Diffie-Hellman
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC"); // Generator for elliptic curves (this is our group)    
            generator.initialize(256);

            // Generate the OR's contribution of the symmetric key.
            KeyPair pair = generator.generateKeyPair();
            byte[] gXBytes = pair.getPublic().getEncoded();
            
            // Initialize the Cipher for encryption
            Cipher cipher = Cipher.getInstance("ElGamal/None/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            // Encrypt the gX string
            String gXEncrypted = Base64.getEncoder().encodeToString(cipher.doFinal(gXBytes));


            // 2. Send a CreateCell 
            CreateCell cell = new CreateCell(gXEncrypted, 5);
            out.println(cell.serialize());
            System.out.println("Sent to OR.");


            // 3. Receive gY and the hash, and assert they're the same.
            JSONObject obj = JsonIO.readObject(in.nextLine());
            if(!obj.containsKey("type") || !obj.getString("type").equals("CREATED"))
                throw new InvalidObjectException("Expected CreatedCell");
            
            CreatedCell recvCell = new CreatedCell(obj);
            String gY = recvCell.getgY();
            String recvKHash = recvCell.getkHash();

            // Generate the shared secret using gY
            // Load the public value from the other side.
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(gY));
            PublicKey gYPubKey = KeyFactory.getInstance("EC").generatePublic(spec);
            
            ecdhKex.init(pair.getPrivate());
            ecdhKex.doPhase(gYPubKey, true);
            byte[] sharedSecret = ecdhKex.generateSecret();

            // 4. Get the kHash for ourselves, and assert.
            MessageDigest md = MessageDigest.getInstance("SHA-3-256");
            md.update(sharedSecret);
            md.update("handshake".getBytes());
            String kHash = Base64.getEncoder().encodeToString(md.digest());

            assertEquals(recvKHash, kHash);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Helper methods
    */

    /**
     * Decodes from Base64 encoding and returns Private Key object.
     * 
     * @param str base 64 encoding.
     * @return Private Key object representation.
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */
    private PrivateKey getPrivateKey(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the base64 encoded private key string
        byte[] privateKeyBytes = Base64.getDecoder().decode(str);

        // Create a PKCS8EncodedKeySpec object from the decoded bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // Get an instance of the KeyFactory for ElGamal algorithm
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal");

        // Generate the PrivateKey object using the KeyFactory
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Decodes from Base64 encoding and returns Public Key object.
     * 
     * @param str base 64 encoding.
     * @return Public Key object representation.
     * @throws InvalidKeySpecException 
     * @throws NoSuchAlgorithmException 
     */
    private PublicKey getPublicKey(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the base64 encoded public key string
        byte[] publicKeyBytes = Base64.getDecoder().decode(str);

        // Create an X509EncodedKeySpec object from the decoded bytes
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        // Get an instance of the KeyFactory for ElGamal algorithm
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal");

        // Generate the PublicKey object using the KeyFactory
        return keyFactory.generatePublic(keySpec);
    }

}
