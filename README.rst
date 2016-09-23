=======
entente
=======

A java - google protocol buffers based communication library

Usage
-----

This relies on google protocol buffer generated messages. We will assume you have
generated a class `Messages` which defines `Ping` and `Ack` messages.

1. Register the generated class(es) via the static ``registerMessageOrigin`` call in client and server:

.. code-block::

    BaseNetwork.registerMessageOrigin(Messages.class);
		
2. Set up Host:

.. code-block::

	Host host = new Host(43216);

3. Ensure host is being updated every frame/update cycle:

.. code-block::

	host.update();
	
4. Set up client:

.. code-block::

	Client client = new Client("localhost", 43216);
	
5. Ensure client is being updated every frame/update cycle:

.. code-block::

	client.update();

6. Register listener(s) for client:

.. code-block::

	client.subscribe(Ack.class, new IMessageListener<Ack>(){
		public void receive(Ack message, Socket origin){
			System.out.println("Received Ack!");
		}
	});
		
7. Register listener(s) for host:

.. code-block::

	host.subscribe(Ping.class, new IMessageListener<Ping>(){
		public void receive(Ping message, Socket origin){
			host.send(Ack.getDefaultInstance());
		}
	});

8. Test by sending the message `Ping` from your client to server:

.. code-block::

	client.send(Ping.getDefaultInstance());
	
Your host, which subscribed to `Ping.class`, will receive the message and
immediately send and `Ack`. Congrats! For more examples, please see the samples
directory.

TODO
----

1. Set up a DDM-like mechanism through which we filter messages. Update
subscribe call or add a updateRegion call to take filter-like arguments.

License
-------

Copyright (c) 2016 Jon Robison

See included LICENSE for licensing information