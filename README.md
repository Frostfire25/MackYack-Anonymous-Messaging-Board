# MackYack: 
## Anonymous Messaging Service using Onion Routing 

<p style="align: center;">

 ![MackYack Logo](./images/logo.png) 

</p>

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
    privKey: "<private-key>",
    messagesPath: "./configs/messages.json"
}
```

### clientConfig.json
---
Contains configuration information for the client to initialize with.  \
Example:
```
{
    addr: "127.0.0.1",
    port: 5000,
    serverAddr: "127.0.0.1",
    serverPort: 5010,
    serverPubKey: "<server-pub-key>",
    routersPath: "example-configs/routers.json"
}
```

### messages.json
---
Contains messages and timestamps stored on the board for the server to reference at startup + write to on each Put request.  \
Example:
```
{
    messages: [
        {
            data: "Hello world.",
            timestamp: "2024/05/07 00:24:12"
        },
        {
            data: "This is Alice, saying hello from the client!",
            timestamp: "2024/05/07 00:25:09"
        }
    ]
}
```

### router_k.json
---
Contains configuration information for the router to initialize with.  \
Example:
```
{
    addr: "127.0.0.1",
    port: 5000,
    privKey: "<private-key>",
    verbose: "false"
}
```

## Build
---
To build the project first make sure you have the correct libraries to build the jar. They can be found in the [lib](/lib/) folder.

First, run the `ant clean` command to remove old builds.
Then, run `ant` to build the `mackyack_client`, `mackyack_server`, and `onionrouter`.
| :zap:        Please make sure  bcprov-ext-jdk18on-172 is in the same directory as the build files when running!   |
|-----------------------------------------|


## Initiation
--- 
Before initiation, all configs in [config](./configs/) should be base implemented.
Reference the Configs section 

All commands should be ran in the root directory of the project

1. Build & Run the `Onion Routers`
   - First need to run k amount of Onion Routers (OR), where each value of k represents a unique Onion Router.
   - Run the following command for a specific Onion Router k. Replace $ with k.
   - `java -jar .\dist\onionrouter.jar --config .\configs\router-$.json`
   - You will receive a public key, on the initiation run. Update the `routers.json` public configuration accordingly.
   - To run the Onion Router, simply run the `java -jar` command again.

2. Build & Run the `Application Server`
   - Run the following command to run the MackYack application server.
   - `java -jar .\dist\mackyack_server.jar --config .\configs\server-config.json`
   - On initiation you will receive a public key, update the `client-config.json` file accordingly.
  
3. Build & Run the `Application Client and Onion Proxy`
    - Run the following command to start the Application Client and Onion Proxy
    - `java -jar .\dist\mackyack_client.jar --config .\configs\client-config.json`
    - Issue commands into the REPL loop for results and information.
