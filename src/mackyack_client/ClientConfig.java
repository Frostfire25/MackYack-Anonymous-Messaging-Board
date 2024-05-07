package mackyack_client;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Config for the sinkhole app.
 * 
 * @author Brandon
 */
public class ClientConfig implements JSONSerializable {

    private String path;

    private String addr;
    private int port;
    private String serverAddr;
    private int serverPort;
    private String serverPubKey;
    private String routersPath;

    public ClientConfig(String path) throws FileNotFoundException, InvalidObjectException {
        this.path = path;

        // Construct file
        File file = new File(path);

        if (file == null || !file.exists()) {
            throw new FileNotFoundException(
                    "File from path for Config does not point to a valid configuration json file.");
        }

        // Construct JSON Object and load configuration
        JSONObject obj = JsonIO.readObject(file);
        deserialize(obj);
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON();
    }

    @Override
    public void deserialize(JSONType type) throws InvalidObjectException {
        JSONObject obj;
        if (type instanceof JSONObject) {
            obj = (JSONObject) type;
        } else {
            throw new InvalidObjectException("Expected Config Type - JsonObject. ");
        }

        if (obj.containsKey("addr")) {
            this.addr = obj.getString("addr");
        } else {
            throw new InvalidObjectException("Expected a Config object -- addr expected.");
        }

        if (obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else {
            throw new InvalidObjectException("Expected a Config object -- port expected.");
        }

        if (obj.containsKey("serverAddr")) {
            this.serverAddr = obj.getString("serverAddr");
        } else {
            throw new InvalidObjectException("Expected a Config object -- serverAddr expected.");
        }

        if (obj.containsKey("serverPort")) {
            this.serverPort = obj.getInt("serverPort");
        } else {
            throw new InvalidObjectException("Expected a Config object -- serverPort expected.");
        }
        
        if (obj.containsKey("serverPubKey")) {
            this.serverPubKey = obj.getString("serverPubKey");
        } else {
            throw new InvalidObjectException("Expected a Config object -- serverPubKey expected.");
        }

        if (obj.containsKey("routersPath")) {
            this.routersPath = obj.getString("routersPath");
        } else {
            throw new InvalidObjectException("Expected a Config object -- routersPath expected.");
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("addr", addr);
        obj.put("port", port);
        obj.put("serverAddr", this.serverAddr);
        obj.put("serverPort", this.serverPort);
        obj.put("serverPubKey", this.serverPubKey);
        obj.put("routersPath", this.routersPath);
        return obj; // We are never reading this file to JSON.
    }

    /**
     * Accessors
     */

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }
    
    public String getServerAddr() {
        return serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerPubKey() {
        return serverPubKey;
    }

    public String getRoutersPath() {
        return routersPath;
    }
    
}