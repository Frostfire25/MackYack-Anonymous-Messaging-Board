package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> First OR
 * Sent from Client to the first onion router to break down the established
 * circuit (recursively).
 */
public class DestroyCell implements JSONSerializable {

    private final String type = "DESTROY";
    private String circID;

    /**
     * Construct a Destroy cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a Destroy cell.
     */
    public DestroyCell(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Destroy needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Destroy cell.");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("Destroy needs a circID.");
            else
                circID = message.getString("circID");

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

    public String getCircID() {
        return circID;
    }
}
