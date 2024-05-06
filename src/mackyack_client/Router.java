package mackyack_client;

import java.io.InvalidObjectException;
import java.security.PrivateKey;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class Router implements JSONSerializable {

    private String addr;
    private int port;
    private String publicKey;

    public Router(String addr, int port, String publicKey) {
        this.addr = addr;
        this.port = port;
        this.publicKey = publicKey;
    }

    public Router() {
        
    }

    @Override
    public void deserialize(JSONType arg0) throws InvalidObjectException {

        if(!(arg0 instanceof JSONObject)) {
            throw new InvalidObjectException("Router is not an instance of JSONObject");
        }

        JSONObject obj = (JSONObject) arg0;

        if(obj.containsKey("addr")) {
            this.addr = obj.getString("addr");
        } else {
            throw new InvalidObjectException("Router object does not contain \"addr\" field.");
        }

        if(obj.containsKey("port")) {
            this.port = obj.getInt("port");
        } else {
            throw new InvalidObjectException("Router object does not contain \"port\" field.");
        }

        if(obj.containsKey("publicKey")) {
            this.publicKey = obj.getString("publicKey");
        } else {
            throw new InvalidObjectException("Router object does not contain \"publicKey\" field.");
        }
    }

    @Override
    public String serialize() {
        return toJSONType().toJSON();
    }

    @Override
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("addr", addr);
        obj.put("port", port);
        obj.put("publicKey", publicKey);
        return obj;
    }

    /**
     * Accessors
     */

    public String getAddr() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return addr +":"+port;
    }

    /**
     * Non-Stored code-bits regarding what we know about a Router
     */
    
    private int circuitId;
    private PrivateKey gx;

    public int getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(int circuitId) {
        this.circuitId = circuitId;
    }

    public PrivateKey getGx() {
        return gx;
    }

    public void setGx(PrivateKey gx) {
        this.gx = gx;
    }



}
