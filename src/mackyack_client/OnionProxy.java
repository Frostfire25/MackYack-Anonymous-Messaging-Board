package mackyack_client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.util.Pair;
import onionrouting.onionrouter_cells.CreateCell;
import onionrouting.onionrouter_cells.CreatedCell;
import onionrouting.onionrouter_cells.DataCell;
import onionrouting.onionrouter_cells.RelayCell;
import onionrouting.onionrouter_cells.RelaySecret;

public class OnionProxy {

    private final Random rand = new Random();

    private final static int ROUTER_COUNT = 3;
    private RoutersConfig routersConfig;
    private ClientConfig conf;

    private List<Router> circuit = new ArrayList<>();

    private KeyPairGenerator generator;
    private KeyAgreement ecdhKex;

    private Socket sock = null;     // Socket with the entrance node, will be null if it isn't being used

    public Router getEntryRouter() {
        return circuit.get(0);
    }

    /**
     * Default Constructor to initialize the Onion Routing System.
     * @throws Exception 
     */
    public OnionProxy(RoutersConfig routersConfig, ClientConfig conf) throws Exception {
        this.routersConfig = routersConfig;
        this.conf = conf;

        // Initialize the BCProvider
        Security.addProvider(new BouncyCastleProvider());

        this.ecdhKex = KeyAgreement.getInstance("ECDH"); // Eliptic Curve Diffie-Hellman
        this.generator = KeyPairGenerator.getInstance("EC"); // Generator for elliptic curves (this is our group)    
        this.generator.initialize(256);

        // Poll for new messages on the proxy
        //pollProxy(true);

        // build the circuit
        constructCircuit();

        // Construct create cells for each OR
        List<CreateCell> createCells = constructCreateCells();

        // Debug Message
        System.out.println(Arrays.toString(circuit.toArray()));


        // Construct a list of messages (Relays) to initiate the circuit keys
        sendCreateCells(createCells);


        //for(JSONSerializable n : create_and_relay_messages) {
        //    System.out.println(n.toJSONType().getFormattedJSON());
        //}
    }

    /**
     * Abstract Function to send a Message.
     * Message construction does not happen at this level.
     * This function strictly sends a Message to the entrance OR.
     * Once the entrance OR receives, the socket will be closed on the OR's end.
     * @param message
     * @throws UnknownHostException
     * @throws IOException
     */
    public void send(String message) throws UnknownHostException, IOException {
        // We can only send to the entrance node in a OR scheme.
        // So that's what we'll do
        Router en_Router = getEntryRouter();
        Socket sock = new Socket(en_Router.getAddr(), en_Router.getPort());
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        writer.write(message);
        writer.newLine();
        writer.flush();

        // Assign the socket
        this.sock = sock;
    }

    private void pollProxy(boolean async) {
        if(async) {
            Thread thread = new Thread(() -> {
                poll();
            });
            thread.start();
        } else {
            poll();
        }
    }

    private void poll() {
        while(true) {
            try {
                System.out.println("Polling...");
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.sock.getOutputStream()));

                System.out.println("Connection accepted ["+this.sock.getInetAddress().getHostAddress()+":"+this.sock.getPort()+"]" );

                // Determine if the packet is handled at the Proxy Layer or at the ApplicationService Layer
                JSONObject obj = JsonIO.readObject(reader.readLine());
                handJSONObject(obj);
                // Protocol is to close the socket after a message has been handled.
                sock.close();
                sock = null;
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handJSONObject(JSONObject obj) {
        try {
            if(obj.containsKey("type")) {
                // TODO
                // If this is a CreatedCell, then handle.
                switch(obj.getString("type")) {
                    case "CREATED": {
                        handleCreated(new CreatedCell(obj));
                    }; return; // If we receive a created element, then we want to stop the thread.

                    case "RELAY": {
                        handleRelay(new RelayCell(obj));
                    }; return;

                    case "DATA": {
                        // ??
                    }; return;
                }
                // ?
            } else {
                ApplicationService.handle(obj);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }  
    }

    /**
     * Find the associating router with circId {@code id}
     * @param id
     * @return
     */
    private Router findRouterWithCircId(int id) {
        return circuit.stream().filter(n -> n.getCircuitId() == id).findFirst().orElse(null);
    }

    private void handleRelay(RelayCell relayCell) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidObjectException, InvalidKeySpecException {
        // If we receive a relay cell, it is wrapping either a CreatedCell or a DataCell.

        // Step 1 Decrypt Relay Secret
        // How are we going to do this?
        // Trivial, Go to the last OR in the circuit. Attempt to decrypt, 
        //      If decrypted (the plaintext is JSONSearlizable)
        //      Then handle the associating JSONSearlizable Cell, (type)
        //  else
        //      Go to the OR prior to the last one, attempt to decrypt
        //  This is not a great way of handling.

        // We have the variable router_encrypted to determine the router in the circuit that 
        
        // When receiving an Onion Router Message it wrapped starting with OR enter's keys

        // Decrypt and handle

        RelayCell message = relayCell;
        // Loop through all of the OR's 
        for(int i = 0; i < circuit.size(); i++) {
            // Router
            Router router = circuit.get(i);

            // Decrypt the child of the Relay message.
            String child = OnionProxyUtil.decryptSymmetric(message.getRelaySecret(), router.getSymmetricKey(), Base64.getDecoder().decode(router.getB64_IV()));

            // Turn into a JSONObject
            JSONObject obj = JsonIO.readObject(child);
            
            if(obj.containsKey("type")) {
                // TODO
                // If this is a CreatedCell, then handle.
                switch(obj.getString("type")) {
                    case "CREATED": {
                        handleCreated(new CreatedCell(obj));
                    }; return; // If we receive a created element, then we want to stop the thread.

                    case "RELAY": {
                        message = new RelayCell(obj);
                    }; break;

                    case "DATA": {
                        // ToDo Handle Data? I believe this is sent to the ApplicationService layer.
                    }; return;
                }
                // ?
            } else {
                ApplicationService.handle(obj);
            }
        }
    }

    /**
     * Handles the created cell
     * @param createdCell
     * @throws InvalidKeyException 
     * @throws InvalidKeySpecException 
     * @throws NoSuchAlgorithmException 
     */
    private void handleCreated(CreatedCell createdCell) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        // First, find the associating router with the createdCell ID
        Router router = findRouterWithCircId(createdCell.getCircId());
        
        // 1. Generate the first half of the DH KEX.
        PublicKey gYPubKey = OnionProxyUtil.getPublicKey("EC", createdCell.getgY());
        String recvKHash = createdCell.getkHash();

        // Do the DH magic.
        ecdhKex.init(router.getGx());
        ecdhKex.doPhase(gYPubKey, true);
        byte[] sharedSecret = ecdhKex.generateSecret();

        // 4. Get the kHash for ourselves, and assert.
        MessageDigest md = MessageDigest.getInstance("SHA3-256");
        md.update(sharedSecret);
        md.update("handshake".getBytes());
        String kHash = Base64.getEncoder().encodeToString(md.digest());

        // Generate random IV
        byte[] rawIV = new byte[16];                             // An AES init. vector.                                                                 // may have different specifications.
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(rawIV);                                   // Fill array with random bytes.
        
        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, "AES");

        // Update the router with the correct information
        router.setSymmetricKey(secretKeySpec);
        router.setB64_IV(Base64.getEncoder().encodeToString(rawIV));
    }

    /**
     * Construct a message that is to be sent (onion)
     * @param message
     * @param server_addr
     * @param port
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidKeyException 
     */
    public JSONSerializable constructOperation(JSONSerializable message, String server_addr, int port) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        // Wrap the operation in Relays.
        // This is quite simple

        // 1. Take the message and wrap it in a datacell
        DataCell cell = new DataCell(conf.getServerAddr(), conf.getServerPort(), (JSONObject) message.toJSONType());

        // Message to be returned
        JSONSerializable ret = cell;
        // Last Router in the message
        Router lastRouter = circuit.get(circuit.size()-1);

        // Wrap the DataCell in Relays from circuit[circuit.len - 1] -> circuit[0]
        for(int i = circuit.size() - 2 ; i >= 0; i--) {
            // Get the current router for the current relay
            Router router = circuit.get(i); 

            // Create the RelaySecret
            RelaySecret secret = new RelaySecret(lastRouter.getAddr(), lastRouter.getPort(), (JSONObject) ret.toJSONType());

            // Encrypt the relay secret with this routers symmetric key
            String ciphertext = OnionProxyUtil.encryptSymmetric(secret.serialize(), router.getSymmetricKey(), Base64.getDecoder().decode(router.getB64_IV()));

            // Create a new RelayCell wrapping 
            RelayCell newRelayCell = new RelayCell(router.getCircuitId(), router.getB64_IV(), ciphertext);
            
            // Update ret and lastRouter
            ret = newRelayCell;
            lastRouter = router;
        }

        return ret;
    }

    /**
     * Construct all of the relay messages from each cell
     * Each CreateCell is associated with the respective Router from {@code circuit} 
     * 
     * @param createCells List<CreateCell> 
     * @return List<JSONSerializable> - A list containing either RelayCells or CreateCells that will be used to send a message. Each node in the list will be sent to the entry node. 
     
    private List<JSONSerializable> createRelays(List<CreateCell> createCells) {
        List<JSONSerializable> relayCells = new ArrayList<>();

        // Start at index 1 since the entry node does not receive a relay cell
        relayCells.add(createCells.get(0));
        for(int i = 1; i < createCells.size(); i++) {

            RelayCell relayCell = new RelayCell(circuit.get(i).getCircuitId(), circuit.get(i).getAddr(), circuit.get(i).getPort(), (JSONObject) createCells.get(i).toJSONType());

            if( i > 1 ) {
                // Loop through all of the Routers from 0 -> (i-2), and append them to the RelayCell
                // This should only be ran if there are 2+ Relays to be made
                for(int j = i - 1; j > 0; j--) {
                    // Create a new RelayCell wrapping 
                    RelayCell newRelayCell = new RelayCell(circuit.get(j).getCircuitId(), circuit.get(j).getAddr(), circuit.get(j).getPort(), (JSONObject) relayCell.toJSONType());
                    relayCell = newRelayCell;
                }
            }
            
            // Append to the array
            relayCells.add(relayCell);
        }

        return relayCells;
    }
     * @throws IOException 
     * @throws UnknownHostException 
     * @throws InterruptedException 
    */

    private void sendCreateCells(List<CreateCell> createCells) throws UnknownHostException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, InterruptedException {

        // Loop through every element in the circuit
        for(int i = 0; i < circuit.size(); i++) {

            // Get the create cell destined for this router (NOT ENCRYPTED)
            JSONSerializable message = createCells.get(i);
            System.out.println("Create message:");
            System.out.println(message.toJSONType().getFormattedJSON());
            Router lastRouter = circuit.get(i);

            // If there needs to be a relay
            if( i > 0 ) {
                // Loop through all of the Routers from 0 -> (i-2), and append them to the RelayCell
                // This should only be ran if there are 2+ Relays to be made
                for(int j = i - 1; j >= 0; j--) {
                    // Get the current router for the current relay
                    Router router = circuit.get(j); 

                    // Create the RelaySecret
                    RelaySecret secret = new RelaySecret(lastRouter.getAddr(), lastRouter.getPort(), (JSONObject) message.toJSONType());

                    // Encrypt the relay secret with this routers symmetric key
                    String ciphertext = OnionProxyUtil.encryptSymmetric(secret.serialize(), router.getSymmetricKey(), Base64.getDecoder().decode(router.getB64_IV()));

                    // Create a new RelayCell wrapping 
                    // - DEBUG - RelayCell newRelayCell = new RelayCell(router.getCircuitId(), router.getB64_IV(), (JSONObject) secret.toJSONType());
                    RelayCell newRelayCell = new RelayCell(router.getCircuitId(), router.getB64_IV(), ciphertext);

                    // Update the message and lastRouter
                    message = newRelayCell;
                    lastRouter = router;
                }
            }

            System.out.println(message.toJSONType().getFormattedJSON());
            send(message.serialize());
            pollProxy(false);
        }


    }

    /**
     * Constructs a list of CreateCells
     * @return
     */
    private List<CreateCell> constructCreateCells() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        List<CreateCell> ret = new ArrayList<>();

        for(Router n : circuit) {
            // 1. Generate the first half of the DH KEX.
            
            // Generate the OR's contribution of the symmetric key.
            KeyPair pair = generator.generateKeyPair();
            byte[] gXBytes = pair.getPublic().getEncoded();
            
            // Initialize the Cipher for encryption
            Cipher cipher = Cipher.getInstance("ElGamal/None/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, OnionProxyUtil.getPublicKey("ElGamal", n.getPublicKey()));

            // Pair of <SymmetricKey:IV> & <Cipher text of Symmetric Encrypted g^x as bytes.
            Pair<String> symmetricKey_CipherText = OnionProxyUtil.encryptHybrid(gXBytes);

            // Encrypt the symmetricKey_CipherText Key+IV
            byte[] encrypted_sym_key = cipher.doFinal(symmetricKey_CipherText.getFirst().getBytes());

            // B64_Encrypted SYM Key
            String B64_encrypted_sym_key = Base64.getEncoder().encodeToString(encrypted_sym_key);

            n.setGx(pair.getPrivate());

            // Get the circuit ID
            int circID = rand.nextInt();
            n.setCircuitId(circID);

            // 2. Send a CreateCell 
            CreateCell cell = new CreateCell(symmetricKey_CipherText.getSecond(), circID, B64_encrypted_sym_key);
            ret.add(cell);
        }

        return ret;
    } 

    /**
     * Constructs the circuit from the routers config.
     * @throws Exception 
     */
    private void constructCircuit() throws Exception {

        if(routersConfig.getRouters() == null || routersConfig.getRouters().size() < ROUTER_COUNT)
            throw new Exception("Invalid count of unique routers in routers.json");

        // Create a copy of getRouters
        List<Router> copy = new ArrayList<Router>();
        copy.addAll(routersConfig.getRouters());
        //Shuffle
        Collections.shuffle(copy);
        // Select the top {ROUTER_COUNT} Routers as OR.
        
        for(int i = 0; i < ROUTER_COUNT; i++)
            circuit.add(copy.get(i));
    }
}
