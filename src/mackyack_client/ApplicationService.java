package mackyack_client;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mackyack_messages.GetRequest;
import mackyack_messages.GetResponse;
import mackyack_messages.Message;
import mackyack_messages.PutRequest;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;

public class ApplicationService {

    private static final String COMMAND_MSG = "Please enter a command [GET, PUT, EXIT]: ";

    private OnionProxy proxy;
    private ClientConfig conf;
    
    public ApplicationService(OnionProxy proxy, ClientConfig conf) throws UnknownHostException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.proxy = proxy;
        this.conf = conf;

        handleInput();
    }

    /**
     * Handle any messages that are destined for the Application level.
     * @param obj
     * @throws InvalidObjectException 
     */
    public static void handle(JSONObject obj) throws InvalidObjectException {
        switch(obj.getString("messagetype")) {
            case "getresponse": {
                clearConsole();  // Clear the console
                
                // Print out all of the messages.
                GetResponse resp = new GetResponse(obj);
                for(Message n : resp.getMessages()) {
                    System.out.println(n);
                }
                System.out.println(COMMAND_MSG);

            }; return;

            case "putresponse": {
                System.out.println("Message added to board.");
                System.out.println(COMMAND_MSG);
            }; return;
        }
    }

    /**
     * Clears the console depending on machine
     */
    private final static void clearConsole()
    {
    try
    {
        final String os = System.getProperty("os.name");
        
        if (os.contains("Windows"))
        {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        }
        else
        {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        }
    }
    catch (final Exception e)
    {
        //  Handle any exceptions.
    }
}

    /**
     * Handles command line input
     * Runs a REPL loop on Main thread to gather input.
     * @throws IOException 
     * @throws UnknownHostException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidKeyException 
     */
    private void handleInput() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, UnknownHostException, IOException {
        Scanner scanner = new Scanner(System.in);
        
        // Handle commands
        while(true) {
            System.out.println(COMMAND_MSG);
            String command = scanner.nextLine();

            switch(command.toUpperCase()) {
                case "GET": {
                    GetRequest req = new GetRequest();
                    sendMessage(req);
                }; break;
                case "PUT": {
                    System.out.println("Please enter the message: ");
                    String putMsg = scanner.nextLine();
                    PutRequest req = new PutRequest(putMsg);
                    sendMessage(req);
                }; break;
                case "EXIT": {System.exit(1);} return;
            }
        }
    }

    private void sendMessage(JSONSerializable msg) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, UnknownHostException, IOException {
        JSONSerializable message = proxy.constructOperation(msg, conf.getServerAddr(), conf.getServerPort());
        proxy.send(message.serialize());
    }

}
