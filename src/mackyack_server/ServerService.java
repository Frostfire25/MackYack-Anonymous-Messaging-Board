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

import mackyack_messages.Message;

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
            System.out.println("Message received: " + msg);

            output.write(msg);
            output.newLine();
            output.close();

            sock.close();
        }
    }

}
