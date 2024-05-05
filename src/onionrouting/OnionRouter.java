package onionrouting;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.concurrent.ConcurrentHashMap;

import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
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
    private static ConcurrentHashMap<Integer, String> keyTable;      // Used for looking up symmetric keys associated with circuit IDs.
    private static ConcurrentHashMap<String, Integer> fwdTable;      // Used for finding + forwarding the proper circuit ID to the next OR in the sequence.

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
     * @throws FileNotFoundException if the configuration file could not be found.
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
     * The entry point
     * @param args the command line arguments.
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException, IOException
    {
    
        if (args.length > 2)
            usage();

        processArgs(args);

        // Initialize the maps
        keyTable = new ConcurrentHashMap<>();
        fwdTable = new ConcurrentHashMap<>();

        // TODO: Server implementation
        System.out.println("Onion Router built successfully.");

        // We don't care about our threads, just crudely shutdown.
        System.exit(0);
    }
}