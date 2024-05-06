package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> Last OR in Circuit
 * Contains a Message, in child field, that should be sent to the server.
 */
public class DataCell implements JSONSerializable {

    private final String type = "DATA";
    private String serverAddr;
    private int serverPort;
    private JSONObject child;

    /**
     * Constructor
     * @param circID
     * @param addr
     * @param port
     * @param child
     */
    public DataCell(String serverAddr, int serverPort, JSONObject child) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.child = child;
    }

    /**
     * Construct a RelayExtend cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a RelayExtend cell.
     */
    public DataCell(JSONObject obj) throws InvalidObjectException {
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

            if (!message.containsKey("serverPort"))
                throw new InvalidObjectException("Relay needs an serverPort.");
            else
                serverPort = message.getInt("serverPort");

            if (!message.containsKey("serverAddr"))
                throw new InvalidObjectException("Relay needs a serverAddr.");
            else 
                serverAddr = message.getString("serverAddr");

            if (!message.containsKey("child"))
                throw new InvalidObjectException("Relay needs an circID.");
            else
                child = message.getObject("child");

                
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
        obj.put("relaySecret", child);
        obj.put("serverAddr", serverAddr);
        obj.put("serverPort", serverPort);

        return obj;
    }


    public JSONObject getChild() {
        return child;
    }

}
