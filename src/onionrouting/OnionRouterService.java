package onionrouting;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import onionrouting.onionrouter_cells.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64Encoder;

/**
 * Class for the threaded service implementation of the OR (to allow for
 * multiple connections through this OR).
 */
public class OnionRouterService implements Runnable {

    private Socket inSock; // The incoming socket connection to this OR.

    /**
     * Constructor for the threaded service implementation of the OnionRouter. This
     * allows for multiple connections.
     * 
     * @param inSock   socket connection incoming to this OR.
     */
    public OnionRouterService(Socket inSock) {
        this.inSock = inSock;

        // Initialize the BCProvider
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void run() {
        try {

            System.out.println("thread started");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inSock.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(inSock.getOutputStream()));

            // Run while the connection is alive in this circuit:

            // Read the type of the incoming cell
            String msg = reader.readLine();
            System.out.println("Incoming message: " + msg);
            JSONObject obj = JsonIO.readObject(msg);
            if (!obj.containsKey("type")) {
                System.err.println("Could not determine the type of the cell. Cell will be dropped");
                return;
            }

            String type = obj.getString("type");

            System.out.println("type:" + obj.getString("type"));

            try {
                switch (type) {
                    case "RELAY":
                        RelayCell relayCell = new RelayCell(obj);

                        doRelay(relayCell);
                        break;
                    case "CREATE":
                        CreateCell createCell = new CreateCell(obj);

                        try {
                            doCreate(createCell, writer);
                        } catch (Exception e) {
                            System.err.println("Could not complete DH KEX with Alice properly.");
                            System.err.println(e);
                        }
                        break;
                    case "CREATED":
                        CreatedCell createdCell = new CreatedCell(obj);

                        doCreated(createdCell);
                        break;
                    case "DESTROY":
                        DestroyCell destroyCell = new DestroyCell(obj);

                        doDestroy(destroyCell);
                        break;
                    default:
                        System.err.println("Unknown cell type. Closing socket...");
                        inSock.close();

                        break;
                }
                inSock.close();
            } catch (InvalidObjectException ex) {
                System.err.println("Invalid Object parsed.");
                System.err.println(ex.getMessage());
                inSock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                inSock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Performs all the operations to be done on a Relay cell when received.
     * 
     * Steps:
     * 1. Decrypt
     * 2. Pass it along
     * 
     * @param cell cell we're performing the operation on.
     */
    private void doRelay(RelayCell cell) {
        // 1. Check if it's incoming or outgoing. This is done by checking if it's in the inTable or outTable
        String circID = cell.getCircID();
        System.out.println("Entered doRelay()");
        
        // a. If it's incoming from Alice (i.e. the circID is in the inTable).
        if (OnionRouter.getInTable().containsKey(circID)) {
            // 1. Update the iv table with the iv we received. This will be used on the way back to Alice to encrypt.
            OnionRouter.getIVTable().put(circID, cell.getIV());

            // 2. Decrypt the Relay cell's secret (contains destination IP/port + child).
            // Get the key
            Key key = OnionRouter.getKeyTable().get(cell.getCircID());
            RelaySecret secret = null;
            try {
                // Decrypt and get the RelaySecret
                String result = decryptCBC(cell.getRelaySecret(), key, cell.getIV());
                secret = new RelaySecret(JsonIO.readObject(result));
            } catch (InvalidObjectException e) {
                System.err.println("Error. Incorrect format for RelaySecret JSON: ");
                System.err.println(e);
            } catch (Exception e) {
                System.err.println("Error. RelaySecret was unable to be decrypted properly: ");
                System.err.println(e);
            }

            System.out.println("Decrypted secret.");

            // 3. Send the child to its destination
            String addr = secret.getAddr();
            int port = secret.getPort();
            JSONObject child = secret.getChild();

            // a. If we're sending a CreateCell, we save the information of the OR to the outTable
            if(child.containsKey("type")) {
                if(child.getString("type").equals("CREATE")) {
                    int outCircID = child.getInt("circID"); // Get the circID Alice assigned for the next OR in the circuit
                    // Put the:
                    //  i. outCircID -> outgoing OR info in the outTable
                    // ii. outCircID -> inCircID in the askTable (for when we do the reverse direction).
                    OnionRouter.getOutTable().put(outCircID, addr + ":" + port);
                    OnionRouter.getAskTable().put(outCircID, circID);

                    System.out.println("Create cell identified from this Relay cell: Added necessary info to outTable/askTable.");
                }
            }

            // b. Actually send to the socket.
            try (Socket outSock = new Socket(addr, port)) {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outSock.getOutputStream()));

                out.write(child.toJSON());
                out.newLine();
                out.close();

                System.out.println("Sent decrypted message to [" + addr + ":" + port + "].");
                System.out.println("Message sent: ");
                System.out.println(child.getFormattedJSON());
            } catch (Exception e) {
                System.err.println("Error connecting to host: " + addr + ":" + port);
                System.err.println(e);
            }

        }
        // b. If it's returning TO Alice (i.e. the circID is in the outTable).
        else if (OnionRouter.getOutTable().containsKey(circID)) {
            // 1. Get this.circID from the outgoing circID ("outgoing" in this context means we're receiving a returning RelayCell)
            Integer thisCircID = OnionRouter.getAskTable().get(circID);
            if(thisCircID == null) {
                System.err.print("Could not find this.circID from the askTable.");
                return;
            }

            // 2. Use this.circID to get the iv + key. We will use these to encrypt
            String iv = OnionRouter.getIVTable().get(thisCircID);
            byte[] rawIV = Base64.getDecoder().decode(iv);
            Key key = OnionRouter.getKeyTable().get(thisCircID);

            // 3. Encrypt the RelayCell and package it into a RelaySecret (will be wrapped in another RelayCell).
            RelaySecret secret = new RelaySecret("", 0, (JSONObject) cell.toJSONType());
            String ctextSecret = null;
            try {
                ctextSecret = encryptSymmetric(secret.serialize(), key, rawIV);
            } catch (Exception e) {
                System.err.println("Unable to encrypt returning RelayCell message");
                return;
            }
            // 4. Get the addr/port of the previous node from the inTable
            String addrPortCombo = OnionRouter.getInTable().get(thisCircID);
            String[] segments = addrPortCombo.split(":");
            String addr = segments[0];
            int port = Integer.parseInt(segments[1]);

            // 5. Package it in a RelayCell, and send it off
            RelayCell retCell = new RelayCell(thisCircID, iv, ctextSecret);

            try (Socket retSock = new Socket(addr, port)) {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(retSock.getOutputStream()));

                out.write(retCell.serialize());
                out.newLine();
                out.close();
            } catch (Exception e) {
                System.err.println("Error connecting to host: " + addr + ":" + port);
                System.err.println(e);
            }
        }
        // c. Else, drop it
        System.out.println("Exited doRelay()");
    }

    /**
     * Performs all the operations to be done on a Create cell when received.
     * 
     * Steps:
     * 1. Get gX from the cell.
     * 2. Get the shared secret + create K
     * 3. Send back a CreatedCell(gY, H(K || "handshake"))
     * a. Note: No encryption on this part. None needed b/c gY is not enough to make
     * K vulnerable
     * 4. Store circID + K in keyTable
     * 
     * @param cell cell we're performing the operation on.
     * @throws IOException
     */
    private void doCreate(CreateCell cell, BufferedWriter output) throws NoSuchAlgorithmException,
            InvalidKeyException, InvalidKeySpecException, IOException {
        
        System.out.println("do create start");

        // 1. Get gX from the cell. Then convert it to a Public Key for DH magic.
        // Decrypt gX so it can be used.
        byte[] gX = decryptHybrid(cell.getEncryptedSymKey(), cell.getgX());

        // If gX is null, that means we encountered an error. Send back a CreatedCell
        // with all empty fields and return.
        if (gX == null) {
            CreatedCell retCell = new CreatedCell("", "", 0);
            output.write(retCell.serialize());
            output.newLine();
            output.close();
            return;
        }

        // Load the public value from the other side.
        X509EncodedKeySpec spec = new X509EncodedKeySpec(gX);
        PublicKey gXPubKey = KeyFactory.getInstance("EC").generatePublic(spec);

        // 2. Diffie-Hellman stuff
        KeyAgreement ecdhKex = KeyAgreement.getInstance("ECDH"); // Eliptic Curve Diffie-Hellman
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC"); // Generator for elliptic curves (this is our
                                                                         // group)
        generator.initialize(256);

        // Generate the OR's contribution of the symmetric key.
        KeyPair pair = generator.generateKeyPair();
        String gY = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        // Generate the shared secret
        ecdhKex.init(pair.getPrivate());
        ecdhKex.doPhase(gXPubKey, true);
        byte[] sharedSecret = ecdhKex.generateSecret();

        // 3. Send back CreatedCell(gY, H(K || "handshake"))

        // Get the hash
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        md.update(sharedSecret);
        md.update("handshake".getBytes());
        String kHash = Base64.getEncoder().encodeToString(md.digest());

        // 4. Store circID + key K in table. Also store incoming connection + circID in inTable
        OnionRouter.getKeyTable().put(cell.getCircID(), new SecretKeySpec(sharedSecret, "AES"));
        String addr = inSock.getInetAddress().getHostAddress();
        int port = inSock.getPort();
        System.out.println("Added [" + addr + ":" + port + "] to inTable.");
        OnionRouter.getInTable().put(cell.getCircID(), addr + ":" + port);

        System.out.println("sending Created message");

        // Package in CreatedCell and return it back.
        CreatedCell retCell = new CreatedCell(gY, kHash, cell.getCircID());
        output.write(retCell.serialize());
        output.newLine();
        output.close();
        
        System.out.println("Created message sent");
    }

    /**
     * Performs all the operations to be done on a Created cell when received.
     * 
     * @param cell cell we're performing the operation on.
     */
    private void doCreated(CreatedCell cell) {
        // TODO: CREATED

        /*
         * Steps:
         * 1. Encapsulate in an ExtendedCell
         * 2. Send back the ExtendedCell
         * 
         * Notes: CREATE and CREATED are sent + received by the last OR before the
         * extension occurs.
         * Additionally, sending BACK a cell is simple. inSockOut.println(ExtendedCell);
         */
    }

    /**
     * Performs all the operations to be done on a Destroy cell when received.
     * 
     * @param cell cell we're performing the operation on.
     */
    private void doDestroy(DestroyCell cell) {
        // TODO: DESTROY

        /*
         * Steps:
         * 1. Break down connections to inSock
         * 2. Send DestroyCell() forward to next OR (if one exists, check fwd table)
         * 3. Break down connections to outSock
         * 
         * Notes: Might need to bring the Scanners/PrintWriters and sockets
         * themselves as args to this method.
         */
    }

    /*
     * Helper methods
     */

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
     * Decrypts the ciphertext using AES-CBC 256.
     * 
     * @param ctextStr Base64-encoded ciphertext.
     * @param key      key we use to decrypt.
     * @param iv       IV used to encrypt this ctext.
     * @return String representation of the plaintext.
     */
    public String decryptCBC(String ctextStr, Key key, String ivStr)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // Set up an AES cipher object.
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Fill array with random bytes.
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivStr));

        // Put the cipher in encrypt mode with the generated key
        aesCipher.init(Cipher.DECRYPT_MODE, key, iv);

        // Encrypt the entire message at once. The doFinal method
        byte[] plaintext = aesCipher.doFinal(Base64.getDecoder().decode(ctextStr));

        return new String(plaintext);
    }

    /**
     * Used to decrypt for G^x (ciphertext)
     * and decrypt the symmetric key (encrypted_sym_key) using PrivateKey in this
     * OnionRouter.
     * 
     * @param encrypted_sym_key
     * @param cyphertext
     * @return
     */
    public byte[] decryptHybrid(final String encrypted_sym_key, final String cyphertext) {
        try {
            // Decrypt the Symmetric Key
            // Initialize the cipher + decrypt
            Cipher cipher = Cipher.getInstance("ElGamal/None/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, OnionRouter.getPrivKey());
            byte[] sym_key = cipher.doFinal(Base64.getDecoder().decode(encrypted_sym_key));
            String[] sym_key_iv_split = new String(sym_key).split(":");

            // Split the Key:IV string up by colon (:)
            final String key = sym_key_iv_split[0];
            final String iv = sym_key_iv_split[1];

            // Set up an AES cipher object.
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            SecretKeySpec aesKey = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");

            // Put the cipher in decrypt mode with the specified key.
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(Base64.getDecoder().decode(iv)));

            // Decrypt the message all in one call.
            byte[] plaintext = aesCipher.doFinal(Base64.getDecoder().decode(cyphertext));

            return plaintext;
        } catch (Exception e) {
            System.err.println("Error decrypting gX from CreateCell.");
            System.err.println(e);
            return null;
        }
    }

}
