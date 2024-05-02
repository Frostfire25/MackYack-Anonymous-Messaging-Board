package messages;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Sent from the server to client proceeding a ClientJoin Message.
 */
public class ClientWelcome implements JSONSerializable {

    private boolean accepted;
    private String uid;

    public ClientWelcome(JSONObject obj) throws InvalidObjectException {
        deserialize(obj);
    }

    @Override
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject message;
        if (obj instanceof JSONObject) {
            message = (JSONObject) obj;

            if (!message.containsKey("accepted"))
                throw new InvalidObjectException("ClientWelcome needs an accepted.");
            else
                accepted = message.getBoolean("accepted");

            if (!message.containsKey("uid"))
                throw new InvalidObjectException("ClientWelcome needs a uid.");
            else
                uid = message.getString("uid");

            if (message.size() > 2)
                throw new InvalidObjectException("Superflous fields");
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        obj.put("accepted", accepted);
        obj.put("uid", uid);

        return obj;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getUid() {
        return uid;
    }
}
