package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> Last OR in Circuit
 * Sent from Client to the first OR in the circuit but forwarded to the last OR
 * in the circuit to extend the circuit by another node.
 */
public class RelayCell extends Cell {

    private final String type = "RELAY";
    private String base64_IV;
    private String relaySecret;

    /**
     * Constructor
     * @param circID
     * @param addr
     * @param port
     * @param child
     */
    public RelayCell(String circID, String base64_IV, String relaySecret) {
        this.circID = circID;
        this.base64_IV = base64_IV;
        this.relaySecret = relaySecret;
    }

    /**
     * Construct a RelayExtend cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a RelayExtend cell.
     */
    public RelayCell(JSONObject obj) throws InvalidObjectException {
        deserialize(obj);
    }

    /**
     * Coverts json data to an object of this type.
     * 
     * @param obj a JSON type to deserialize.
     * @throws InvalidObjectException the type does not match this object.
     */
    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject message;
        if (obj instanceof JSONObject) {
            message = (JSONObject) obj;

            if (!message.containsKey("type"))
                throw new InvalidObjectException("Relay needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Relay cell.");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("Relay needs an circID.");
            else
                circID = message.getString("circID");

            if (!message.containsKey("relaySecret"))
                throw new InvalidObjectException("Relay needs an relaySecret.");
            else
                relaySecret = message.getString("relaySecret");
                
            if (!message.containsKey("base64_IV"))
                throw new InvalidObjectException("Relay needs an base64_IV.");
            else
                base64_IV = message.getString("base64_IV");
                
            if (message.size() > 4)
                throw new InvalidObjectException("Superflous fields");
        }
    }

    /**
     * Serializes the object into a JSON encoded string.
     * 
     * @return a string representing the JSON form of the object.
     */
    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    /**
     * Converts the object to a JSON type.
     * 
     * @return a JSON type of JSONObject
     */
    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("type", type);
        obj.put("circID", circID);
        obj.put("base64_IV", base64_IV);
        obj.put("relaySecret", relaySecret);

        return obj;
    }

    public String getIV() {
        return base64_IV;
    }

    public String getRelaySecret() {
        return relaySecret;
    }
}
