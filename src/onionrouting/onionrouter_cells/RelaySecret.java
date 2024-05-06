package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class RelaySecret implements JSONSerializable {

    private final String type = "RELAYSECRET";
    private String addr; // ENCRYPTED
    private int port; // ENCRYPTED 
    private String child; // Represented as a Ek(JSONObject) // ENCRYPTED

    public RelaySecret(String addr, int port, String child) {
        this.addr = addr;
        this.port = port;
        this.child = child;
    }

    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject message;
        if (obj instanceof JSONObject) {
            message = (JSONObject) obj;

            if (!message.containsKey("type"))
                throw new InvalidObjectException("RelaySecret needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for RelaySecret cell.");

            if (!message.containsKey("port"))
                throw new InvalidObjectException("Relay needs an port.");
            else
                port = message.getInt("port");

            if (!message.containsKey("addr"))
                throw new InvalidObjectException("Relay needs a addr.");
            else 
                addr = message.getString("addr");

            if (!message.containsKey("child"))
                throw new InvalidObjectException("Relay needs a child.");
            else 
                child = message.getString("child");
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("type", type);
        obj.put("addr", addr);
        obj.put("port", port);
        obj.put("child", child);

        return obj;
    }

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public String getChild() {
        return child;
    }
    
}
