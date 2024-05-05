package mackyack_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class RoutersConfig implements JSONSerializable {

    private String path;

    private List<Router> routers = new ArrayList<>();

    public RoutersConfig(String path) throws FileNotFoundException, InvalidObjectException {
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

    @Override
    public void deserialize(JSONType arg0) throws InvalidObjectException {
        if(!(arg0 instanceof JSONObject)) {
            throw new InvalidObjectException("Router is not an instance of JSONObject");
        }

        JSONObject obj = (JSONObject) arg0;

        if(!obj.containsKey("routers")) {
            throw new InvalidObjectException("Routers array not present");
        }

        JSONArray arr = obj.getArray("routers");
        for(Object n : arr) {

            if(!(n instanceof JSONObject)) {
                throw new InvalidObjectException("Array element is not an instance of JSONObject");
            }

            JSONObject e = (JSONObject) n;

            routers.add(new Router(e.getString("addr"), e.getInt("port"), e.getString("pubKey")));
        }

    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public JSONType toJSONType() {
        JSONObject object = new JSONObject();
        JSONArray routers = new JSONArray();

        routers.addAll(this.routers);

        object.put("routers", routers);
        return object;
    }
    
}
