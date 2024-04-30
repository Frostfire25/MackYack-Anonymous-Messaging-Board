# MackYack
## Anonymous Messaging Service using Onion Routing Protocol 

### Configuration
---
# TODO


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