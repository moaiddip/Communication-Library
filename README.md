ComProt
=======

This is the communication protocol library developped for HKR's last year's first semester project named Smart House.
Our job is to be able to transmit the messages in a secure and somewhat efficient way between the arduino-server-clients and prepare a medium for the other groups to be able to access the messages sent. 
The library's purpose is to keep things simple by not getting the rest of the groups involved with more code and to provide simple methods for them to use instead.

What is next:
--------------
Make the server automatically detect when an arduino is disconnected and look for it.

Use NIO instead of IO streams.

Switch from RXTX to jSSC.

Send JSON objects instead of strings.

Perhaps, softcode some of the restricted commands (Could make implementation a lot more complicated)

Version Revisions:
-------------------

Version 0.9: Made the classes more expandable, got rid of a lot of static classes/methods/objects. The arduino connector can now be instantiated multiple times from the same server, which means you can have multiple arduinos inside the same house. The client can also be connected to multiple servers/houses.

Version 0.8: Added a queue to the client side for messages pushed by the server and updates that are not requested by the client explicitly.

Version 0.7: Changed the way threads are handled, threads are now saved in a hashmap. Added Multicast support using a username to find the thread you want to send a message to.

Version 0.6:
Added a queue to the arduino connector where messages that were not queried that are received from the arduino are saved.

Version 0.5:
Added an algorithm that changes after how many queries should they be sent to be processed in the second queue in real time.

Version 0.4:
Added an arduino connector, that sends commands to an arduino through a serial port and waits for an answer.

Version 0.3:
Added a key generator to be used to create tokens.
Added more variables to the Item class.

Version 0.2:
Added two queues, one from which the server reads and one to which server/client communication threads write.
Added an Item class, instead of a String, in which relevant information regarding the queries are saved.

Version 0.1:
Multithreaded server/client using TLS security that queues commands in a single queue called Queue.
