package mackyack_messages;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> Server
 * Sent from Client through the circuit to the Server containing a message. This
 * message will be appended to the board.
 */
public class Put implements JSONSerializable {

    private String data;

    /**
     * Construct a Put cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a Put cell.
     */
    public Put(JSONObject obj) throws InvalidObjectException {
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

            if (!message.containsKey("data"))
                throw new InvalidObjectException("Put needs a data.");
            else
                data = message.getString("data");

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

        obj.put("data", data);

        return obj;
    }

    public String getData() {
        return data;
    }
}
