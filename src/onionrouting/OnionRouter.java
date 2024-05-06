package onionrouting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Key;
import java.security.KeyFactory;

import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.json.JsonIO;
import merrimackutil.util.Pair;
import merrimackutil.util.Tuple;


/**
 * This is the Onion Router object. Its job is to provide onion routing through an overlay network for some application layer protocol.
 * 
 * @author Derek Costello
 */
public class OnionRouter
{
    // Basic fields:
    public static boolean doHelp = false;               // True if help option present.
    private static OnionRouterConfig conf = null;       // The configuration information.
    private static String configFile = "";   // Default configuration file.
    
    // OR-specific fields:
    private static ConcurrentHashMap<Integer, Key> keyTable;       // The table used to find symmetric keys based on.
    private static ConcurrentHashMap<Integer, String> ivTable;     // This.circID -> iv
    private static ConcurrentHashMap<Integer, Integer> askTable;   // Outgoing circID -> This.circID. Used to lookup the path back to Alice.
    private static ConcurrentHashMap<Integer, String> inTable;     // This.circID -> SRC OR IP/port combo.
    private static ConcurrentHashMap<Integer, String> outTable;    // Outgoing circID -> Outgoing OR IP/port combo.
    private static PrivateKey privKey;                             // Private key for this OR

    /**
     * Prints the usage to the screen and exits.
     */
    public static void usage() {
        System.out.println("usage:");
        System.out.println("  onionrouter --config <config>");
        System.out.println("  onionrouter --help");
        System.out.println("options:");
        System.out.println("  -c, --config\t\tConfig file to use.");
        System.out.println("  -h, --help\t\tDisplay the help.");
        System.exit(1);
    }

    /**
     * Processes the command line arugments.
     * @param args the command line arguments.
     */
    public static void processArgs(String[] args)
    {
        OptionParser parser;
        boolean doHelp = false;
        boolean doConfig = false;

        LongOption[] opts = new LongOption[2];
        opts[0] = new LongOption("help", false, 'h');
        opts[1] = new LongOption("config", true, 'c');
        
        Tuple<Character, String> currOpt;

        parser = new OptionParser(args);
        parser.setLongOpts(opts);
        parser.setOptString("hc:");


        while (parser.getOptIdx() != args.length)
        {
            currOpt = parser.getLongOpt(false);

            switch (currOpt.getFirst())
            {
                case 'h':
                    doHelp = true;
                break;
                case 'c':
                    doConfig = true;
                    configFile = currOpt.getSecond();
                break;
                case '?':
                    System.out.println("Unknown option: " + currOpt.getSecond());
                    usage();
                break;
            }
        }

        // Verify that that this options are not conflicting.
        if ((doConfig && doHelp))
            usage();
        
        if (doHelp)
            usage();
        
        try 
        {
            loadConfig();
        } 
        catch (FileNotFoundException e) 
        {
            System.exit(1);
        }
    }

    /**
     * Loads the configuration file.
     * @throws FileNotFoundException if the config file is not found.
     */
    public static void loadConfig() throws FileNotFoundException
    {
        try
        { 
            if(configFile == null || configFile.isEmpty()) {
                throw new FileNotFoundException("File config can not be empty for Onion Router. Please choose a specific router.");
            }

            conf = new OnionRouterConfig(configFile);
        }
        catch(InvalidObjectException ex)
        {
            System.err.println("Invalid configuration file from JSON.");
            System.out.println(ex);
            System.exit(1);
        }
        catch(FileNotFoundException ex)
        {
            System.out.println(ex);
            System.exit(1);
        }
    }

    /**
     * Saves the configuration file.
     */
    public static void saveConfig() {
        try
        { 
            if(configFile == null || configFile.isEmpty()) {
                throw new FileNotFoundException("File config can not be empty for Onion Router. Please choose a specific router.");
            }

            JsonIO.writeSerializedObject(conf, new File(configFile));
        }
        catch(FileNotFoundException ex)
        {
            System.out.println(ex);
            System.exit(1);
        }
    }

    /**
     * The entry point
     * @param args the command line arguments.
     * @throws IOException 
     * @throws InterruptedException 
     * @throws NoSuchAlgorithmException 
     */
    public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException
    {
        // Register BouncyCastleProvider
        Security.addProvider(new BouncyCastleProvider());

        if (args.length > 2)
            usage();

        processArgs(args);

        // Assure that this router has a pub/private key pair, 
        // if not 
        //  update the config, 
        //  stop program, 
        //  and print out public key.
        if(conf.getPrivateKey() == null || conf.getPrivateKey().isEmpty()) {

            Pair<String> keys = OnionRouterCrypto.generateAsymKeys();

            conf.setPrivateKey(keys.getSecond());
        
            saveConfig();

            System.out.println("Please update the corresponding routers.json table with the public key below.");
            System.out.println(keys.getFirst());

            // We don't care about our threads, just crudely shutdown.
            System.exit(0);
        }

        // Initialize the private key as a PrivateKey object.
        try {
            privKey = convertToPrivateKey("ElGamal", conf.getPrivateKey());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error deserializing private key for this OR.");
            e.printStackTrace();
            System.exit(1);            
        }

        // Initialize the tables
        keyTable = new ConcurrentHashMap<>();
        ivTable = new ConcurrentHashMap<>();
        askTable = new ConcurrentHashMap<>();
        inTable = new ConcurrentHashMap<>();
        outTable = new ConcurrentHashMap<>();

        // Initialize the router's "Server" capability (AKA allow for incoming connections)
        ServerSocket server = new ServerSocket(conf.getPort());

        System.out.println("Onion Router started on port: " + conf.getPort());

        while(true) {
            Socket sock = server.accept();

            System.out.println("OR Connection received with addr ["+sock.getInetAddress().getHostAddress()+":"+sock.getLocalPort()+"]" );

            Thread ORServiceThread = new Thread(new OnionRouterService(sock));
            ORServiceThread.start();
        }
    }

    /**
     * Decodes from Base64 encoding and returns Private Key object.
     * 
     * @param str base 64 encoding.
     * @return Private Key object representation.
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */
    private static PrivateKey convertToPrivateKey(String algorithm, String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Decode the base64 encoded private key string
        byte[] privateKeyBytes = Base64.getDecoder().decode(str);

        // Create a PKCS8EncodedKeySpec object from the decoded bytes
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        // Get an instance of the KeyFactory for ElGamal algorithm
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        // Generate the PrivateKey object using the KeyFactory
        return keyFactory.generatePrivate(keySpec);
    }


    /*
        Accessors
    */

    /**
     * @return static reference to outTable (<Integer, Key>; this.circID -> symmetric key for a particular circuit).
     */
    public static ConcurrentHashMap<Integer, Key> getKeyTable() {
        return keyTable;
    }

    /**
     * @return static reference to ivTable (<Integer, String>; this.circID -> iv for a particular circuit).
     */
    public static ConcurrentHashMap<Integer, String> getIVTable() {
        return ivTable;
    }

    /**
     * @return static reference to askTable (<Integer, Integer>; Outgoing circID -> this.circID for a particular circuit).
     */
    public static ConcurrentHashMap<Integer, Integer> getAskTable() {
        return askTable;
    }

    /**
     * @return static reference to inTable (<Integer, String>; this.circID -> Source OR IP/port combo for a particular circuit).
     */
    public static ConcurrentHashMap<Integer, String> getInTable() {
        return inTable;
    }

    /**
     * @return static reference to outTable (<Integer, String>; Outgoing circID -> Outgoing OR IP/port combo for a particular circuit).
     */
    public static ConcurrentHashMap<Integer, String> getOutTable() {
        return outTable;
    }

    /**
     * @return Private Key object representation of the OR's private key.
     */
    public static PrivateKey getPrivKey() {
        return privKey;
    }
}