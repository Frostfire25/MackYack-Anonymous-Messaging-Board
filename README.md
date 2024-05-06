# MackYack: Anonymous Messaging Service using Onion Routing 

## MackYack Protocol



### Client Functionality
---
- Message is sent to the server, where it is added to the MackYack board.
- Requests to the server are made periodically (every 3 seconds) to update the client's local view of the MackYack board.
- Anonymity is maintained by sending requests through an Onion Routing overlay network
    - Onion Routing overlay network is accessed via. an Onion Proxy. More details in "Onion Routing Protocol" section.

### OnionProxy (Client)
The `OnionProxy` class serves as a Proxy within an Onion Routing Network, facilitating communication between a client and an Entrance Onion Router. It manages the establishment of secure communication channels between nodes, message relay, and message reception.

#### Methods


### `public OnionProxy(RoutersConfig routersConfig, ClientConfig conf) throws Exception`
This constructor initializes the Onion Routing System. It requires configurations for routers and clients. Upon instantiation, it constructs the circuit, generates create cells for each OR, sends create cells to initiate circuit keys, and starts polling for new messages on the proxy.

### `public void send(String message) throws UnknownHostException, IOException`
This method sends a string message to the entrance Onion Router. It establishes a socket connection and transmits the message.

### `private void pollProxy()`
Initiates the polling mechanism for new messages on the proxy.

### `private void handJSONObject(JSONObject obj)`
Handles a received JSON object, determining whether it should be handled at the Proxy Layer or at the ApplicationService Layer.

### `private Router findRouterWithCircId(int id)`
Finds the router associated with the specified circuit ID.

### `private void handleRelay(RelayCell relayCell) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidObjectException, InvalidKeySpecException`
Handles a relay cell received from an Onion Router. Decrypts the relay secret and handles the associated JSON serializable cell.

### `private void handleCreated(CreatedCell createdCell) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException`
Handles a created cell received from an Onion Router. Generates the first half of the DH Key Exchange and updates router information with the symmetric key.

### `public JSONSerializable constructOperation(JSONSerializable message, String server_addr, int port) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException`
Constructs a message to be sent, wrapping it in relays from the last router in the circuit to the entry node.

### `private void sendCreateCells(List<CreateCell> createCells) throws UnknownHostException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException`
Sends create cells to each router in the circuit, encrypting and wrapping them in relays as necessary.

### `private List<CreateCell> constructCreateCells() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException`
Constructs create cells for each router in the circuit, generating the first half of the DH Key Exchange and encrypting symmetric keys.

### `private void constructCircuit() throws Exception`
Constructs the circuit from the provided router configuration, selecting a specified number of routers as Onion Routers.


### Server Functionality
---
- Message is received by some unknown client, and its message is appended to the board along with a timestamp (the time it was received).
- Upon receiving a request for the board from a client, a snapshot of the board is sent back to the requesting client.

### Messages 
---

1. Put
```
Client -> Server
Sent from Client through the circuit to the Server containing a message. This message will be appended to the board.

Properties:
    - String - data
```

1. Put Response
```
Server -> Client
Response sent to client to assure that their message has been added to the board.

Properties: (none)
```

2. Get Request
```
Client -> Server
Sent from the Client through the circuit to the Server asking to receive information regarding the board at the current instant.

Properties: (none)
```

3. Get Response
```
Server -> Client
Sent from the Server through the circuit to the Client responding with all of the Messages on the Board.

Properties:
    - List<Message> - messages
        - Where a message is constructed as
            - String - data
            - String - timestamp
```

## Onion Routing Protocol

### Onion Proxy Functionality
---
- Used by MackYack clients to access the Onion Routing overlay network.
    - The Onion Proxy (OP) is run on the client's local computer when the MackYack application starts.
- Constructs the circuit (i.e. sends create + extend cells) by choosing from the Client's list of known routers (located in routers.json).
- Ability to deconstruct the circuit by sending a destroy cell and its associated circID to the entry Onion Router (OR).
- Encrypts MackYack messages in "layers" (like an onion! Get it?).
    - Encryption is done by encrypting the message with the key established with each OR in the circuit starting with the farthest node and ending with the closest.
- Decrypts MackYack responses by peeling back "layers".
    - Decryption is done by decrypting the message with the key establish with each OR in the circuit starting with the closest node and ending with the farthest.

### Cells (Messages)
---

1. Create
```
Client -> First OR
Sent from Client to the first onion router to create a circuit.

Properties:
    - int - circID
    - String - gX; Base 64-encoded first half of Diffie-Hellman KEX encrypted in the ephemeral key (see next property).
    - String - encryptedSymKey; Ephemeral key (symmetric) encrypted using the OR's public key.
```

2. Created
```
First OR -> Client
Sent from the first onion router to the client confirming the creation of a circuit.

Properties:
    - String - gY; Base 64-encoded second half of Diffie-Hellman KEX.
    - String - kHash; Base 64-encoded SHA-3 256 hash: H(K || "handshake")
```

3. Destroy
```
Client -> First OR
Sent from Client to the first onion router to break down the established circuit (recursively).

Properties:
    - int - circID
```

4. Extend
```
Client -> Last OR in Circuit
Sent from Client to the first OR in the circuit but forwarded to the last OR in the circuit to extend the circuit by another node.

Properties:
    - int - circID
    - String - addr; Address of the OR to add to the circuit
    - int - port; Port of the OR to add to the circuit
    - String - gX; Base 64-encoded first half of Diffie-Hellman KEX encrypted in the ephemeral key (see next property).
    - String - encryptedSymKey; Ephemeral key (symmetric) encrypted using the OR's public key.
```

5. Extended
```
New OR's predecessor -> Client
Sent from the newly-created OR's predecessor to the Client acknowledging the extension and sending the second half of the DH KEX.

Properties:
    - String - gY; Base 64-encoded second half of Diffie-Hellman KEX.
    - String - kHash; Base 64-encoded SHA-3 256 hash: H(K || "handshake")
```

6. Data
```
Client -> Last OR in Circuit -> Server
Sent from the client to the last OR in the circuit (which is then passed to the server). Contains the data
that will be sent out to the server.

Properties:
    - String - addr; Address of the OR to add to the circuit
    - int - port; Port of the OR to add to the circuit
    - String - data; Data to be sent over to the server (JSON marshalled; encrypted in Server's public key)
```

7. Relay
```
Client -> OR; OR -> OR
Sent client/OR to an OR in the circuit. Its function is for the receiving OR to relay the data to the
next OR in the chain without interpreting the data.

Properties:
    - int - circID
    - String - relayData; Data that is being relayed. Encrypted in onion layers to be peeled one-at-a-time
                          at the destination OR w/ the symmetric key identified by the circID
```

## Configs

### routers.json
---
Contains information on the onion routers IP / port combinations, as well as their public keys.  \
Example:
```
{
    routers: [
        {
            addr: "127.0.0.1",
            port: 5000,
            pubKey: "<node-pub-key>"
        },
        {
            addr: "127.0.0.1",
            port: 5003,
            pubKey: "<node-pub-key>"
        }
    ]
}
```

### serverConfig.json
---
Contains configuration information for the server to initialize with.  \
Example:
```
{
    port: 5010,
    privKey: "<private-key>"
}
```

### clientConfig.json
---
Contains configuration information for the client to initialize with.  \
Example:
```
{
    "port": 5000,
    serverAddr: "127.0.0.1",
    serverPort: 5010,
    serverPubKey: "<server-pub-key>",
    routersPath: "example-configs/routers.json"
}
```

### router_k.json
Contains configuration information for the router to initialize with.  \
Example:
```
{
    port: 5000,
    privKey: "<private-key>"
}
```
