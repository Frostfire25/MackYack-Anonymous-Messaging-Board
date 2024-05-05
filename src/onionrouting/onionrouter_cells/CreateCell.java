package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> First OR
 * Sent from Client to the first onion router to create a circuit.
 */
public class CreateCell implements JSONSerializable {

    private final String type = "CREATE";
    private int circID;
    private String gX; // Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public Key.

    /**
     * Default constructor to initialize an outgoing CreateCell object.
     * 
     * @param gX Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public Key.
     * @param circID circuit ID
     */
    public CreateCell(String gX, int circID) {
        this.gX = gX;
        this.circID = circID;
    }
    
    /**
     * Construct a Create cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a Create cell.
     */
    public CreateCell(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Create needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Create cell.");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("Create needs a circID.");
            else
                circID = message.getInt("circID");

            if (!message.containsKey("gX"))
                throw new InvalidObjectException("Create needs a gX.");
            else
                gX = message.getString("gX");

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

        obj.put("type", type);
        obj.put("circID", circID);
        obj.put("gX", gX);

        return obj;
    }

    public String getType() {
        return type;
    }

    public int getCircID() {
        return circID;
    }

    public String getgX() {
        return gX;
    }
}
