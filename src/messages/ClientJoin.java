package messages;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Sent whenever a Client wants to enter the Message room.
 */
public class ClientJoin implements JSONSerializable {

    private String srcAddress;
    private int srcPort;

    public ClientJoin(JSONObject obj) throws InvalidObjectException {
        deserialize(obj);
    }

    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject message;
        if (obj instanceof JSONObject) {
            message = (JSONObject) obj;

            if (!message.containsKey("srcAddress"))
                throw new InvalidObjectException("ClientJoin needs an srcAddress.");
            else
                srcAddress = message.getString("srcAddress");

            if (!message.containsKey("srcPort"))
                throw new InvalidObjectException("ClientJoin needs a srcPort.");
            else
                srcPort = message.getInt("srcPort");

            if (message.size() > 2)
                throw new InvalidObjectException("Superflous fields");
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("srcAddress", srcAddress);
        obj.put("srcPort", srcPort);

        return obj;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public int getSrcPort() {
        return srcPort;
    }
}
