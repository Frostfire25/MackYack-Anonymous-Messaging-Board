# MackYack
## Anonymous Messaging Service using Onion Routing Protocol 

### Configuration
---
# TODO


## Mack Yack Protocol

### Messages 
---

1. ClientJoin
```
Client -> Server Message
Sent whenever a Client wants to enter the Message room.

Properties:
    - String - srcAddress
    - int - srcPort
```

2. ClientWelcome
```
Server -> Client Message
Sent from the server to client proceeding a ClientJoin Message.

Properties:
    - boolean - accepted
    - String - uid
```


3. ClientLeave
4. ServerPoll
5. ServerPollResponse
6. MessageSend
7. MessageReceive



## Onion Routing Protocol


### Cells (Messages)
---

1. Create
```
Client -> First OR
Sent from client to the first onion router to create a circuit.

Properties:
    - int - circID
    - String - gX; Base 64-encoded first half of Diffie-Hellman KEX encrypted in the OR's public key.
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
Sent from client to the first onion router to break down the established circuit (recursively).

Properties:
    - int - circID
```

4. Relay Extend
```
Client -> Last OR in Circuit
Sent from client to the first OR in the circuit but forwarded to the last OR in the circuit to extend the circuit by another node.

Properties:
    - int - circID
    - String - addr; Address of the OR to add to the circuit
    - int - port; Port of the OR to add to the circuit
```

5. Relay Data
```
Client -> Server
Sent from client through the circuit to the Server containing the REST API request.

Properties:
    - int - circID
    - String - data; Base 64-encoded data
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
    privKey: NUHIDYAWHGDOIW12ih3ih3oa,
    routersPath: "example-configs/routers.json"
}
```

### clientConfig.json
---
Contains configuration information for the client to initialize with.  \
Example:
```
{
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
