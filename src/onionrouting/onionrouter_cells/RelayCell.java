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
public class RelayCell implements JSONSerializable {

    private final String type = "RELAY";
    private int circID;
    private String addr;
    private int port;
    private String child; // Represented as a JSONObject

    /**
     * Constructor
     * @param circID
     * @param addr
     * @param port
     * @param child
     */
    public RelayCell(int circID, String addr, int port, String child) {
        this.circID = circID;
        this.addr = addr;
        this.port = port;
        this.child = child;
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
                circID = message.getInt("circID");

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

            if (message.size() > 6)
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
        obj.put("addr", addr);
        obj.put("port", port);
        obj.put("child", child);

        return obj;
    }


    public int getCircID() {
        return circID;
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
