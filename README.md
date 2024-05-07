# MackYack: 
## Anonymous Messaging Service using Onion Routing 

<p style="align: center;">

 ![MackYack Logo](./images/logo.png) 

</p>

## MackYack Protocol


### Client Functionality
---
- Message is sent to the server, where it is added to the MackYack board.
- Requests to the server are made periodically (every 3 seconds) to update the client's local view of the MackYack board.
- Anonymity is maintained by sending requests through an Onion Routing overlay network
    - Onion Routing overlay network is accessed via. an Onion Proxy. More details in "Onion Routing Protocol" section.

## Application Layer
---
MackYack is designed to be an anonymous messaging board system. We have created a Client Server model for handling operations on the board.
Client is allowed to receive messages on the board and update new messages on the board. While, the server is allowed to respond with the current state of the board and update the board when a new message is received.

<p style="align: center;">

 ![Application workflow](./images/application-workflow.png) 

</p>

There exists three commands in our Client-side application.
 - `GET` - Constructs and sends a GetRequest
 - `PUT` - Prompts the interface for a message, and sends to server
 - `EXIT` - Closes the application and destroys the circuit.

<p style="align: center;">

 ![Messages i.e.](./images/messages.png) 

</p>

#### Application Layer Messages
There exists four types of Application layer messages that can be sent
1. PutRequest
```
Client -> Server
Sent from Client through the circuit to the Server containing a message. This message will be appended to the board.

Properties:
    - String - data
```

1. PutResponse
```
Server -> Client
Response sent to client to assure that their message has been added to the board.

Properties: (none)
```

2. GetRequest
```
Client -> Server
Sent from the Client through the circuit to the Server asking to receive information regarding the board at the current instant.

Properties: (none)
```

3. GetResponse
```
Server -> Client
Sent from the Server through the circuit to the Client responding with all of the Messages on the Board.

Properties:
    - List<Message> - messages
        - Where a message is constructed as
            - String - data
            - String - timestamp
```


## OnionProxy
---
The `OnionProxy` (OP) class serves as a Proxy within an Onion Routing Network, facilitating communication between a client and an Entrance Onion Router. It manages the establishment of secure communication channels between nodes, message relay, and message reception. The Onion Proxy is abstracted out from the client such that it can be ran as a unique layer between any Application layer that utilizes MerrimackUtil JSON Messaging and connects to our devised Onion Network.
The Onion Proxy (OP) is utilized by MackYack clients for accessing the Onion Routing overlay network, running on the client's local computer upon the MackYack application's launch.
 - It constructs the circuit, selecting from the client's list of known routers (located in routers.json), and has the ability to deconstruct the circuit by sending a destroy cell and its associated circID to the entry Onion Router (OR).
 - The OP encrypts MackYack messages to send to the entrance onion router in "layers," akin to an onion, encrypting the message with the key established with each OR in the circuit from the farthest node to the closest.
 - Subsequently, whenever a message is received from the entrance onion router, it decrypts MackYack responses by peeling back "layers," decrypting the message with the key established with each OR in the circuit from the closest node to the farthest.
 - Additionally, the OP handles various tasks such as:
   - Determining whether a received JSON object should be processed at the Proxy Layer or the ApplicationService Layer.
   - Finding the router associated with a specified circuit ID.
   - Managing relay cells received from an Onion Router.
   - Constructing messages to be sent.
   - Sending create cells to each router in the circuit.
   - Generating create cells for each router.
   - Building the circuit from the provided router configuration.

#### Public API
### `public OnionProxy(RoutersConfig routersConfig, ClientConfig conf) throws Exception`
This constructor initializes the Onion Routing System. It requires configurations for routers and clients. Upon instantiation, it constructs the circuit, generates create cells for each OR, sends create cells to initiate circuit keys, and starts polling for new messages on the proxy.

### `public void send(String message) `
This method sends a string message to the entrance Onion Router. It establishes a socket connection and transmits the message.

### `public void pollProxy()`
Initiates the polling mechanism for new messages on the proxy.


## Onion Routing Protocol
---
`TODO`

### Symmetric Key Creation Handleing

### Relay Handling

### Data Handling
There are two streets to talk about when handling data messages.

### Destroy Handleing

## Cells (Messages)
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
