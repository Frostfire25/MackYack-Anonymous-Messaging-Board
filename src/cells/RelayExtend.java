package cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> Last OR in Circuit
 * Sent from Client to the first OR in the circuit but forwarded to the last OR
 * in the circuit to extend the circuit by another node.
 */
public class RelayExtend implements JSONSerializable {

    private int circID;
    private String addr; // Address of the OR to add to the circuit
    private int port; // Port of the OR to add to the circuit

    /**
     * Construct a RelayExtend cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a RelayExtend cell.
     */
    public RelayExtend(JSONObject obj) throws InvalidObjectException {
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

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("RelayExtend needs an circID.");
            else
                circID = message.getInt("circID");

            if (!message.containsKey("addr"))
                throw new InvalidObjectException("RelayExtend needs a addr.");
            else
                addr = message.getString("addr");

            if (!message.containsKey("port"))
                throw new InvalidObjectException("RelayExtend needs a port.");
            else
                port = message.getInt("gX");

            if (message.size() > 3)
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

        obj.put("circID", circID);
        obj.put("addr", addr);
        obj.put("port", port);

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
}
