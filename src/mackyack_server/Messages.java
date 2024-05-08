package mackyack_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import mackyack_client.Router;
import mackyack_messages.Message;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class Messages implements JSONSerializable {

    private String path;

    private List<Message> messages = new ArrayList<>();

    public Messages(String path) throws FileNotFoundException, InvalidObjectException {
        this.path = path;

        // Construct file
        File file = new File(path);

        if (file == null || !file.exists()) {
            throw new FileNotFoundException(
                    "File from path for Config does not point to a valid configuration json file.");
        }

        // Construct JSON Object and load configuration
        JSONObject obj = JsonIO.readObject(file);
        deserialize(obj);
    }

    /**
     * Adds a message to the messages list,
     * then updates the messages.json file.
     * @param message
     * @throws FileNotFoundException 
     */
    public void addMessage(Message message) throws FileNotFoundException {
        messages.add(message);
        writeToFile();
    }

    private void writeToFile() throws FileNotFoundException {
        JsonIO.writeSerializedObject(this, new File(path));
    }

    @Override
    public void deserialize(JSONType arg0) throws InvalidObjectException {
        messages = new ArrayList<>(); // new List since this file can be written to
        if(!(arg0 instanceof JSONObject)) {
            throw new InvalidObjectException("Messages is not an instance of JSONObject");
        }

        JSONObject obj = (JSONObject) arg0;

        if(!obj.containsKey("messages")) {
            throw new InvalidObjectException("Messages array not present");
        }

        JSONArray arr = obj.getArray("messages");
        for(Object n : arr) {

            if(!(n instanceof JSONObject)) {
                throw new InvalidObjectException("Array element is not an instance of JSONObject");
            }

            JSONObject e = (JSONObject) n;

            messages.add(new Message(e.getString("data"), e.getString("timestamp")));
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    /**
     * Append all messages to a JSONArray stored as "messages"
     */
    @Override
    public JSONType toJSONType() {
        JSONObject o = new JSONObject();

        JSONArray arr = new JSONArray();
        for(Message n : messages) {
                arr.add(n.toJSONType());
        }

        o.put("messages", arr);

       return o;
    }

    /**
     * Accessors
     */

    public List<Message> getMessages() {
        return messages;
    }
}
