entente
=======

A java - google protocol buffers based communication library

Usage
-----

This relies on google protocol buffer generated messages. We will assume you have
generated a class `Messages` which defines `Ping` and `Ack` messages.

1. Register the generated class(es) via the static `registerMessageOrigin`
call in client and server:

```java
	BaseNetwork.registerMessageOrigin(Messages.class);
```
		
2. Set up Host:

```java
	Host host = new Host(43216);
```

3. Ensure host is being updated every frame/update cycle:

```java
	host.update();
```
	
4. Set up client:

```java
	Client client = new Client("localhost", 43216);
```
	
5. Ensure client is being updated every frame/update cycle:

```java
	client.update();
```
	
6. Register listener(s) for client:

```java
	client.subscribe(Ack.class, new IMessageListener<Ack>(){
		void receive(Ack message, Socket origin){
			System.out.println("Received Ack!");
		}
	});
```
		
7. Register listener(s) for host:

```java
	host.subscribe(Ping.class, new IMessageListener<Ping>(){
		void receive(Ping message, Socket origin){
			host.send(Ack.getDefaultInstance());
		}
	});
```

8. Test by sending the message `Ping` from your client to server:

```java
	client.send(Ping.getDefaultInstance());
```
	
Your host, which subscribed to `Ping.class`, will receive the message and
immediately send and `Ack`. Congrats!

License
-------

Copyright (c) 2016 Jon Robison

See included LICENSE for licensing information