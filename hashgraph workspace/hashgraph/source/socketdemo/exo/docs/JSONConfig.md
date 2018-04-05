Configuring Exo Using a Configuration File
==========================================

You can configure the framework for your application using a configuration file in JSON format.  All aspects of Exo can be configured using JSON.  The biggest advantage to doing so is that you can easily use different configuration files for different deployment options, e.g. local, docker, development, production, etc.

## Initializing the Framework Using a Configuration File

Initializing Exo using a configuration file is a one-line operation:

```java
ExoPlatformLocator.initFromConfig(platform, "path/to/exo-config.json");
```

If your exo-config.json file is located in the same folder as the Hashgraph application will run in, you can omit the path parameter:
```java
ExoPlatformLocator.initFromConfig(platform);
```
An exo-config.json file uses the following structure:
```json
{
    "clientConfig": {
        ...
    },
    "hashgraphConfig": {
      "transactionTypesClassName": "com.txmq.socketdemo.SocketDemoTransactionTypes",
      "transactionProcessors": [
        "com.txmq.exo.messaging.rest",
        "com.txmq.socketdemo.transactions"
      ],
      "socketMessaging": {
        "port": -1,
        "derivedPort": 1000,
        "handlers": [
          "com.txmq.socketdemo.socket"
        ]
      },
      "rest": {
        "port": -1,
        "derivedPort": 2000,
        "handlers": [
          "com.txmq.socketdemo.rest"
        ]
      },
      "blockLogger": {
        "loggerClass": "com.txmq.exo.persistence.couchdb.CouchDBBlockLogger",
        "parameters": [
          { "key": "databaseName", "value": "zoo-"},
          { "key": "useAsPrefix", "value": "true"},
          { "key": "protocol", "value": "http"},
          { "key": "host", "value": "couchdb"},
          { "key": "port", "value": "5984"},
          { "key": "blockSize", "value": 5},
          { "key": "createDb", "value": "true"}
        ]
      }
   }
}
```

The file is designed to be "portable" between socket clients and hashgraphs - configuration for socket clients is segregated from configuration for hashgraph applications.  

## Configuring Transaction Types
You can configure the class that Exo uses for its transaction type pseudo-enum by setting the "transactionTypesClassName" property to the full path to your ExoTransactionType subclass:
```json
"transactionTypesClassName": "com.txmq.socketdemo.SocketDemoTransactionTypes",
```

## Configuring Transaction Processors
You can configure automatic transaction routing to processors by setting the "transactionProcessors" property.  Pass an array of package names containing transaction processors for Exo to scan:
```json
"transactionProcessors": [
  "com.txmq.exo.messaging.rest",
  "com.txmq.socketdemo.transactions"
]
```

## Configuring Java Socket Messaging
You can configure Exo's socket messaging feature by defining the port or derived port Exo should listen on, and a list of packages for Exo to search for socket message handlers:
```json
"socketMessaging": {
  "port": -1,
  "derivedPort": 1000,
  "handlers": [
    "com.txmq.socketdemo.socket"
  ]
}
```

In this example, we've asked Exo to calculate the port it should listen on based on the port the hashgraph node listens on.  This is typical for applications that run on the alpha SDK where the Swirlds browser starts up multiple nodes on the same host.  The above definition will add 1000 to the hashgraph's port to determine which port to listen on.  You can also define a fixed port by setting a value other than -1 in the port property.  If the port is set, then derivedPort willb e ignored.
If no socketMessaging object is defined, Exo will not create a transaction server instance to listen for socket requests.

The example above sets up an unsecured socket - there is no encyption in transait, nor are clients authenticated.  This configuration should knly be used for playing with the framework or while troubleshooting.  To set up a TLS-secured socket authenticated using X.509 certificates, we can pass in the locations and passwords for the keystores containing client and server side keys, and Exo will configure a secured socket:
```json
"socketMessaging": {
    "port": -1,
    "derivedPort": 1000,
    "secured": true,
    "clientKeystore": {
        "path": "client.public",
        "password": "client"
    },
    "serverKeystore": {
        "path": "server.private",
        "password": "server"
    },
    "handlers": [
        "com.txmq.socketdemo.socket"
    ]
}
```

## Configuring REST Endpoints
REST endpoints are configured using the same configuration object format as socket messaging.  Set the port or derivedPort to accept requests on, and a list of packages that contain JAX-RS-annotated request handlers:
```json
"rest": {
  "port": -1,
  "derivedPort": 2000,
  "handlers": [
    "com.txmq.socketdemo.rest"
  ]
}
```
As with socket messaging, if no REST configuration is defined, then REST will be disabled.

## Configuring Block Logging
Block logging can be configured by supplying the logger class and a list of logger-specific parameters in the "blockLogger" property.  If no blockLogger is set, logging will be disabled.  The following example shows how to initialize the CouchDB-based logger included in Exo:
```json
"blockLogger": {
  "loggerClass": "com.txmq.exo.persistence.couchdb.CouchDBBlockLogger",
  "parameters": [
    { "key": "databaseName", "value": "zoo-"},
    { "key": "useAsPrefix", "value": "true"},
    { "key": "protocol", "value": "http"},
    { "key": "host", "value": "couchdb"},
    { "key": "port", "value": "5984"},
    { "key": "blockSize", "value": 5},
    { "key": "createDb", "value": "true"}
  ]
}
```
Note that the parameters collection is specific to the logger implementation.  Different loggers will expect different parameters.  See the documentation for your specific logger for information on which parameters it expects.
