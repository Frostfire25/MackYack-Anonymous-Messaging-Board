package mackyack_messages;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class Message implements JSONSerializable {

    private String data;
    private String timestamp;

    public Message(String data, String timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public Message(JSONObject obj) throws InvalidObjectException {
        deserialize(obj);
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("data", data);
        obj.put("timestamp", timestamp);

        return obj;
    }

    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject message;
        if (obj instanceof JSONObject) {
            message = (JSONObject) obj;

            if (!message.containsKey("data"))
                throw new InvalidObjectException("Message needs a data.");
            else
                data = message.getString("data");

            if (!message.containsKey("timestamp"))
                throw new InvalidObjectException("Message needs a timestamp.");
            else
                timestamp = message.getString("timestamp");
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public String toString() {
        return "["+timestamp+"] - " + data;
    }

    /**
     * Accessors
     */

     public String getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
