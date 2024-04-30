import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import merrimackutil.cli.LongOption;
import merrimackutil.cli.OptionParser;
import merrimackutil.util.Tuple;

public class App {

    private static final String CONFIG_DEFAULT_LOC = "./config.json";
    private static Config config;
    private static Block block;

    private static boolean running = true;

    public static void main(String[] args) throws Exception {

        OptionParser op = new OptionParser(args);
        LongOption[] ar = new LongOption[2];
        ar[0] = new LongOption("config", true, 'c');
        ar[1] = new LongOption("help", false, 'h');
        op.setLongAndShortOpts(ar);
        op.setOptString("hc:");
        Tuple<Character, String> opt = op.getLongOpt(false);
        if (opt == null) {
            // Initialize config
            config = new Config(CONFIG_DEFAULT_LOC);
        } else if (Objects.equals(opt.getFirst(), 'h')) {
            System.out.println(
                    "$ java -jar dist/sinkhole.jar -h\r\n" + //
                            "usage:\r\n" + //
                            "  sinkhole --config <config>\r\n" + //
                            "  sinkhole --help\r\n" + //
                            "options:\r\n" + //
                            "  -c, --config Config file to use.\r\n" + //
                            "  -h, --help Display the help. \n");
            System.exit(0);
        } else if (Objects.equals(opt.getFirst(), 'c')) {
            // Initialize config
            config = new Config(opt.getSecond());
            block = new Block(new File(config.getBlockFile()));
        }

        DatagramSocket socket = new DatagramSocket(config.getSinkholePort());
        System.out.println("Sinkhole service running on port " + socket.getLocalPort());

        // Basic echo-server for DatagramSocket.
        while(running) {

            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            // Construct the DNS Header object
            DNSHeader header = new DNSHeader.DNSHeaderBuilder(buf).build();
            System.out.println("Header constructed for UDP Packet recv.");

            // Construct the DNSQuery object
            // Represented by bytes [header(12)::]
            DNSQuery query = new DNSQuery.DNSQueryBuilder(Arrays.copyOfRange(buf, 12, buf.length)).build();
            System.out.println("Query constructed for the UDP Packet recv.");
            
            // Do checking and filtering here based on the query
            // If permitted, query the upstream DNS server
            if(!block.isPresent(query.getQNAME(), query.getQTYPE())) {

            } 
            // Else, construct a "not allowed" packet and return the the sender
            else {
                
            }
        

        }

        socket.close();

    }

    /**
     * Sends a request to the upstreams DNS server.
     * Reference. 
     * https://stackoverflow.com/questions/36743226/java-send-udp-packet-to-dns-server
     * 
     * @param buf Original request received by the sinkhole to query.
     * @return DatagramPacket representing the response from the upstream DNS server.
     * @throws IOException 
     */
    public static DatagramPacket queryUpstream(byte[] buf) throws IOException {
        final String DNS_Server_Address = config.getDnsAddress();
        final int DNS_Server_Port = 53;

        InetAddress ipAddress = InetAddress.getByName(DNS_Server_Address);
                
        // Send out to DNS server
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(buf, buf.length, ipAddress, DNS_Server_Port);
        socket.send(dnsReqPacket);

        // Await for response
        byte[] recVBuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(recVBuf, recVBuf.length);
        socket.receive(packet);

        System.out.println("packet received");

        // Close the socket and return recVBuf
        socket.close();
        return packet;
    }

}
