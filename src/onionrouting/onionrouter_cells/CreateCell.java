package onionrouting.onionrouter_cells;

import java.io.InvalidObjectException;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

/**
 * Client -> First OR
 * Sent from Client to the first onion router to create a circuit.
 */
public class CreateCell extends Cell {

    private final String type = "CREATE";
    private String gX; // Base 64-encoded first half of Diffie-Hellman KEX encrypted in the ephemeral key.
    private String encryptedSymKey; // Base 64-encoded ephemeral key (symmetric) encrypted in the OR's public key.
    private String srcAddr; // The address from which this cell  was sent
    private int srcPort; // The port from which this cell was sent

    /**
     * Default constructor to initialize an outgoing CreateCell object.
     * 
     * @param gX Encrypted Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public Key.
     * @param circID circuit ID
     * @param encryptedSymKey Symmetric Key used to decrypt gX
     */
    public CreateCell(String gX, String circID, String encyptedSymKey) {
        this.gX = gX;
        this.circID = circID;
        this.encryptedSymKey = encyptedSymKey;
    }

    /**
     * Overloaded constructor to initalize an outgoing CreateCell object w/ the srcAddr/srcPort info.
     * 
     * @param gX Encrypted Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public Key.
     * @param circID circuit ID
     * @param encyptedSymKey Symmetric Key used to decrypt gX
     * @param srcAddr source address
     * @param srcPort source port
     */
    public CreateCell(String gX, String circID, String encyptedSymKey, String srcAddr, int srcPort) {
        this.gX = gX;
        this.circID = circID;
        this.encryptedSymKey = encyptedSymKey;
        this.srcAddr = srcAddr;
        this.srcPort = srcPort;
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
                circID = message.getString("circID");

            if (!message.containsKey("srcAddr"))
                throw new InvalidObjectException("Create needs a srcAddr.");
            else
                srcAddr = message.getString("srcAddr");

            if (!message.containsKey("srcPort"))
                throw new InvalidObjectException("Create needs a srcPort.");
            else
                srcPort = message.getInt("srcPort");

            if (!message.containsKey("gX"))
                throw new InvalidObjectException("Create needs a gX.");
            else
                gX = message.getString("gX");

            if (!message.containsKey("encryptedSymKey"))
                throw new InvalidObjectException("Create needs a encryptedSymKey.");
            else
                encryptedSymKey = message.getString("encryptedSymKey");
                

            if (message.size() > 6)
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
        obj.put("srcAddr", srcAddr);
        obj.put("srcPort", srcPort);
        obj.put("encryptedSymKey", encryptedSymKey);

        return obj;
    }

    public String getType() {
        return type;
    }

    public String getgX() {
        return gX;
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public String getEncryptedSymKey() {
        return encryptedSymKey;
    }

    public void setSrcAddr(String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }
}
