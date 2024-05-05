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
public class ExtendCell implements JSONSerializable {

    private final String type = "EXTEND";
    private int circID;
    private String addr; // Address of the OR to add to the circuit
    private int port; // Port of the OR to add to the circuit
    private String gX; // Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public Key.

    /**
     * Construct a RelayExtend cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a RelayExtend cell.
     */
    public ExtendCell(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Extend needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Extend cell.");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("RelayExtend needs an circID.");
            else
                circID = message.getInt("circID");

            if (!message.containsKey("addr"))
                throw new InvalidObjectException("RelayExtend needs an addr.");
            else
                addr = message.getString("addr");

            if (!message.containsKey("port"))
                throw new InvalidObjectException("RelayExtend needs a port.");
            else
                port = message.getInt("port");

            if(!message.containsKey("gX"))
                throw new InvalidObjectException("RelayExtend needs gX.");
            else
                gX = message.getString("gX");

            if (message.size() > 5)
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
        obj.put("gX", gX);

        return obj;
    }

    public String getType() {
        return type;
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

    public String getgX() {
        return gX;
    }

}
