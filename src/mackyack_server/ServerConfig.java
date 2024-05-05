package mackyack_server;
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
public class ServerConfig implements JSONSerializable {

    private String path;

    private String privKey;
    private int port;
    private String routersPath;

    public ServerConfig(String path) throws FileNotFoundException, InvalidObjectException {
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

        if (obj.containsKey("privKey")) {
            this.privKey = obj.getString("privKey");
        } else {
            throw new InvalidObjectException("Expected a Config object -- privKey expected.");
        }

        if (obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else {
            throw new InvalidObjectException("Expected a Config object -- port expected.");
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
        obj.put("privKey", this.privKey);
        obj.put("port", this.port);
        obj.put("routersPath", this.routersPath);
        return obj; // We are never reading this file to JSON.
    }

    /**
     * Accessors
     */

    public String getPrivKey() {
        return privKey;
    }

    public int getPort() {
        return port;
    }

    public String getRoutersPath() {
        return routersPath;
    }

    /**
     * Modifiers
     */

     public void setPrivKey(String key) {
        this.privKey = key;
     }
}