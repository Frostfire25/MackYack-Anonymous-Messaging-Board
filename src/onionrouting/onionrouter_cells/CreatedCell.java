package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * First OR -> Client
 * Sent from the first onion router to the client confirming the creation of a
 * circuit.
 */
public class CreatedCell extends Cell {

    private final String type = "CREATED";
    private String gY;          // Base 64-encoded second half of Diffie-Hellman KEX.
    private String kHash;       // Base 64-encoded SHA-3 256 hash: H(K || "handshake").

    /**
     * Default constructor to initialize a returning CreatedCell object.
     * 
     * @param gY Base 64-encoded second half of Diffie-Hellman KEX.
     * @param kHash Base 64-encoded SHA-3 256 hash: H(K || "handshake").
     */
    public CreatedCell(String gY, String kHash, String circID) {
        this.gY = gY;
        this.kHash = kHash;
        this.circID = circID;
    }

    /**
     * Construct a Created cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a Created cell.
     */
    public CreatedCell(JSONObject obj) throws InvalidObjectException {
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
                throw new InvalidObjectException("Created needs a type.");
            else if(!message.getString("type").equals(type))
                throw new InvalidObjectException("Type is incorrectly specified for Created cell.");

            if (!message.containsKey("gY"))
                throw new InvalidObjectException("Created needs a gY.");
            else
                gY = message.getString("gY");

            if (!message.containsKey("kHash"))
                throw new InvalidObjectException("Created needs a kHash.");
            else
                kHash = message.getString("kHash");

            if (!message.containsKey("circID"))
                throw new InvalidObjectException("Created needs a circID.");
            else
                circID = message.getString("circID");

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
        obj.put("gY", gY);
        obj.put("kHash", kHash);
        obj.put("circID", circID);

        return obj;
    }

    /**
     * Accessors
     */

    public String getType() {
        return type;
    }

    public String getgY() {
        return gY;
    }

    public String getkHash() {
        return kHash;
    }
}
