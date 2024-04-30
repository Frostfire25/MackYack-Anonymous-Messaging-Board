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
public class Config implements JSONSerializable {

    private String path;

    private String dnsAddress;
    private int sinkholePort;
    private String blockFile;

    public Config(String path) throws FileNotFoundException, InvalidObjectException {
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

        if (obj.containsKey("dns-address")) {
            this.dnsAddress = obj.getString("dns-address");
        } else {
            throw new InvalidObjectException("Expected a Config object -- dns-address expected.");
        }

        if (obj.containsKey("sinkhole-port")) {
            this.sinkholePort = obj.getInt("sinkhole-port");
        } else {
            throw new InvalidObjectException("Expected a Config object -- sinkhole-port expected.");
        }
        
        if (obj.containsKey("block-file")) {
            this.blockFile = obj.getString("block-file");
        } else {
            throw new InvalidObjectException("Expected a Config object -- block-file expected.");
        }

    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("dns-address", this.dnsAddress);
        obj.put("sinkhole-port", this.sinkholePort);
        obj.put("block-file", this.blockFile);
        return obj; // We are never reading this file to JSON.
    }

    public String getDnsAddress() {
        return dnsAddress;
    }

    public int getSinkholePort() {
        return sinkholePort;
    }

    public String getBlockFile() {
        return blockFile;
    }

    
}