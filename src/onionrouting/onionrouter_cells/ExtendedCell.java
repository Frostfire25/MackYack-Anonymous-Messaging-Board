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
public class ExtendedCell implements JSONSerializable {

    private final String type = "EXTENDED";
    private int circID;

    /**
     * Construct a RelayExtend cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a RelayExtend cell.
     */
    public ExtendedCell(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Extended needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Extended cell.");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("Extended needs an circID.");
            else
                circID = message.getInt("circID");

            if (message.size() > 2)
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

        return obj;
    }

    public String getType() {
        return type;
    }

    public int getCircID() {
        return circID;
    }
}
