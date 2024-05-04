package onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> First OR
 * Sent from Client to the first onion router to break down the established
 * circuit (recursively).
 */
public class Destroy implements JSONSerializable {

    private int circID;

    /**
     * Construct a Destroy cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a Destroy cell.
     */
    public Destroy(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Destroy needs a circID.");
            else
                circID = message.getInt("circID");

            if (message.size() > 1)
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

        return obj;
    }

    public int getCircID() {
        return circID;
    }
}
