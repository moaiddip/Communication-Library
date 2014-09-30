ComProt
=======

1. Server:
	1.1: The server is a thread.
	1.2: It requires an int as a port, an int to indicate if the server should be run locally only where 1 means local only and 0        local and remote.
	1.3: It also requires a string with the keystore name, a string with the keystore password and a string with the key password.
2. WriteQueue:
	2.1: This is the class that the queries are passed onto, which then creates an instance of the Items class which is                  responsible
	     for all the necessary information regarding each query and are then stored in a hashmap.
	2.2: After a certain amount of queries are created, all the queries that are not yet processed are passed onto another
	     hashmap. This hashmap is the one that should be used by the server to process the queries and can be retrieved using the        method returnList().
3. Items:
	3.1: The items class holds the following information:
		3.1.1: The query or the message as a string, which can be retrieved using getMsg().
		3.1.2: If the query has been answered, as a Boolean. Can be returned using isAnswered(). Default = False
		3.1.3: If the message is old or new, as a Boolean. Can be returned using getState(). New (Default)= True, Old = False
		3.1.4: The reply as a string, which can be set using the method putReply(). Default=null
4. Implementing a Queue for the server to process the queries:
	4.1: The hashmap should be retrieved from the WriteQueue class using returnList().
	4.2: When picking the next query to process, one needs to make sure that isAnswered is false (unanswered) and getState is true        (new).
	4.3: To implement priorities, one needs to quickly go through the queries, save the first found of each priority level and           then skip the rest until a higher priority level is found which should also be saved. In the end execute the one with the        highest priority level.
	4.4: Repeat 4.3 every time one needs to process a new query.
	NOTE: Check ReadQueue as an example (Uses FIFO, no priorities).
5. Client:
	5.1: This class creates an SSLSocket and returns it through the method createSocket.
	5.2: The method createSocket requires a destination address as a string, a port as an int, the truststore name as a string and   the truststore password as a string.
6. Sender:
	6.1: This class is used to exchange messages between the client and the server using the method send.
	6.2: The method requires a query as a string and the sslsocket received from the method createSocket.
	6.3: The method returns a string with the answer from the server.
