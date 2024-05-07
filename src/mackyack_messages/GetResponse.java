package mackyack_messages;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Server -> Client
 * Sent from the Server through the circuit to the Client responding with all of
 * the Messages on the Board.
 */
public class GetResponse implements JSONSerializable {

    private List<Message> messages;

    public GetResponse(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Construct a GetResponse cell from the corresponding JSON object.
     * 
     * @param obj a JSON object representing a GetResponse cell.
     */
    public GetResponse(JSONObject obj) throws InvalidObjectException {
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

            messages = new ArrayList<>();
            if(message.containsKey("messages")) {
                JSONArray arr = (JSONArray) message.getArray("messages");
                for(Object n : arr) {
                    JSONObject o = (JSONObject) n;
                    messages.add(new Message(o.getString("data"), o.getString("timestamp")));
                }
            } else 
                throw new InvalidObjectException("Message must contain a messages field.");
            
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

        JSONArray array = new JSONArray();
        for (int i = 0; i < messages.size(); i++)
            array.add(messages.get(i).toJSONType());

        obj.put("messagetype", "getresponse");
        obj.put("messages", array);

        return obj;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
