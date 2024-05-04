package mackyack_messages;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class Message {

    private String data;
    private String timestamp;

    public Message(String data, String timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("data", data);
        obj.put("timestamp", timestamp);

        return obj;
    }
}
