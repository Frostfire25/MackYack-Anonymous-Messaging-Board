package mackyack_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import mackyack_messages.GetResponse;
import mackyack_messages.Message;
import mackyack_messages.PutRequest;
import mackyack_messages.PutResponse;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

public class ServerService {
    
    public ServerService() throws IOException {
        poll();
    }

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
    /**
     * Constructs a message object to be sent
     * @param data
     * @return
     */
    private Message createMessage(String data) {
        
        LocalDateTime now = LocalDateTime.now();
        return new Message(data, dtf.format(now));
    }

    private void poll() throws IOException {
        ServerSocket server = new ServerSocket(MackYackServer.getConf().getPort());

        while(true) {
            Socket sock = server.accept();
            System.out.println("Connection established");
            BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

            String msg = input.readLine();

            // Read msg as a JSONObject
            JSONObject obj = JsonIO.readObject(msg);

            JSONSerializable ret = null;
            switch(obj.getString("messagetype")) {
                case "getrequest": {
                    ret = new GetResponse(MackYackServer.getMessages().getMessages());
                }; break;
                case "putrequest": {
                    // Deserialize the message
                    PutRequest req = new PutRequest(obj);
                    // Create a new message and append to Messages array. 
                    Message putMessage = createMessage(req.getData());
                    MackYackServer.getMessages().addMessage(putMessage);
                    // Send a PutResponse
                    ret = new PutResponse();
                }; break;
            }

            // If the message received was not supported, report and move along.
            if(ret == null) {
                System.out.println("Invalid message received: ");
                System.out.println(msg);
                continue;
            }

            output.write(ret.serialize());
            output.newLine();
            output.close();
        }
    }

}
