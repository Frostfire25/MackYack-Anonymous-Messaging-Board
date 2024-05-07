package mackyack_client;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.util.Tuple;
import onionrouting.onionrouter_cells.DataCell;


/**
 * This is the main client-side application. Its job is to contact the MackYack server through Onion Routing.
 */
public class MackYackClient
{
    public static boolean doHelp = false;                       // True if help option present.
    private static ClientConfig conf = null;                    // The configuration information.
    private static String configFile = "configs/client-config.json";    // Default configuration file.
    private static RoutersConfig routersConfig;

    private static OnionProxy proxy;

    /**
     * Prints the usage to the screen and exits.
     */
    public static void usage() {
        System.out.println("usage:");
        System.out.println("  mackyack_client --config <config>");
        System.out.println("  mackyack_client --help");
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
            conf = new ClientConfig(configFile);
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
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        // Append bouncy castle Provider
        Security.addProvider(new BouncyCastleProvider());

        if (args.length > 2)
            usage();

        processArgs(args); 

        routersConfig = new RoutersConfig(conf.getRoutersPath());

        proxy = new OnionProxy(routersConfig, conf);

        // TODO: Client implementation
        System.out.println("Mack Yack Client built successfully.");

        JSONObject obj = new JSONObject();
        obj.put("greetings", "Hello, World!");
        DataCell cell = new DataCell(conf.getServerAddr(), conf.getServerPort(), obj);

        JSONSerializable toSend = proxy.constructOperation(cell, conf.getServerAddr(), conf.getServerPort());

        proxy.send(toSend.serialize());

        // We don't care about our threads, just crudely shutdown.
        System.exit(0);
    }
}