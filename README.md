# MackYack: Anonymous Messaging Service using Onion Routing 

## MackYack Protocol

### Client Functionality
---
- Message is sent to the server, where it is added to the MackYack board.
- Requests to the server are made periodically (every 3 seconds) to update the client's local view of the MackYack board.
- Anonymity is maintained by sending requests through an Onion Routing overlay network
    - Onion Routing overlay network is accessed via. an Onion Proxy. More details in "Onion Routing Protocol" section.

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

2. Get Request
```
Client -> Server
Sent from the Client through the circuit to the Server asking to receive information regarding the board at the current instant.

Properties: (none)
```

3. Get Response
```
Client -> Server
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
            pubKey: JIASDOH~!3`1ho2ioHSDIAOD -- Base 64 encoding
        },
        {
            addr: "127.0.0.1",
            port: 5003,
            pubKey: NUHIDYAWHGDOIW12ih3ih3oa -- Base 64 encoding
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
    privKey: NUHIDYAWHGDOIW12ih3ih3oa
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
    serverPubKey: NUHIDYAWHGDOIW12ih3ih3oa,
    routersPath: "example-configs/routers.json"
}
```

### router_k.json
Contains configuration information for the router to initialize with.  \
Example:
```
{
    port: 5000,
    privKey: NUHIDYAWHGDOIW12ih3ih3oa
}
```
