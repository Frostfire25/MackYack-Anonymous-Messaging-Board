package onionrouting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Config for the onion routers.
 * 
 * @author Brandon Cash, Derek Costello
 */
public class OnionRouterConfig implements JSONSerializable {

    private String privateKey;
    private int port;

    public OnionRouterConfig(String path) throws FileNotFoundException, InvalidObjectException {
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
        return toJSONType().toJSON();
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
            this.privateKey = obj.getString("privKey");
        } else {
            throw new InvalidObjectException("Expected a Config object -- privKey expected.");
        }

        if (obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else {
            throw new InvalidObjectException("Expected a Config object -- port expected.");
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("privKey", this.privateKey);
        obj.put("port", this.port);
        return obj; // We are never reading this file to JSON.
    }

    /**
     * Accessors 
     * @return
     */

    public String getPrivateKey() {
        return privateKey;
    }

    public int getPort() {
        return port;
    }

    /**
     * Modifiers
     */
    public void setPrivateKey(String key) {
        this.privateKey = key;
    }

}