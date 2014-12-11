ComProt
=======

We are the communication protocol group of HKR's last year's first semester project named Smart House.
The Smart House project consists of four groups: Devices group, Units group, Servers and Databases group and Communication Protocol group.
Our job is to be able to transmit the messages in a secure and efficient way between the arduinos-servers-clients and prepare a medium for the other groups to be able to access the messages sent. 
We have developped a library for the purpose of keeping things simple by not getting the rest of the groups involved with more complex code and in turn provide them with simple methods to use.

What's next:
------------

Bug fixing, stress testing, optimization, redoing JDOC.

Additional optional future features:
-------------------

Remake the way ConnectionHandler threads are dropped.

Version Revisions:
-------------------

Version 1.2: Added JSON support. Changed the way replies are processed between the server and the client. The server can accept normal sockets and the client can create a normal socket. Removed some softcoded commands that contributed negatively to the complexity of the implementation. Added a timeout feature to the client class.

Version 1.1b: Fixed bugs; the 1.1 release was deleted due to the nature of the bugs found. A small improvement in the way multicast is handled in some special cases was added.

Version 1.1: Reworked the WriteQueue to be more efficient. Also, the dynamic functionality can be readjusted on runtime. 
The Item class now supports aging.
The way the Client class quits communication has been reworked.
Reworked the way hashmaps are handled.

Version 1.0c: Softcoded more commands, refactored the code and fixed a bug that stopped multicasting from working.

Version 1.0b: Softcoded some restricted commands. Syntax can now be more freely chosen.

Version 1.0: Made the server automatically detect when an arduino has disconnected and look for it. Cleaned up the code, adjusted the synchronized methods.

Version 0.9: Made the classes more expandable, got rid of a lot of static classes/methods/objects. The arduino connector can now be instantiated multiple times from the same server, which means you can have multiple arduinos inside the same house. The client can also be connected to multiple servers/houses.

Version 0.8: Added a queue to the client side for messages pushed by the server and updates that are not requested by the client explicitly. Removed the key generator, a different solution was implemented.

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

Contributors:
--------------

SorcerersApprentice - Sozos Assias/Denisa Biala (same person, different computer owner)

roguelikerogue - Cameron Brownlee (his commits are not linked to his username but his name)

rayaq - Raya Bogoslovova

Related projects:
----------------

https://github.com/roguelikerogue/SmartHouseServlet The part created by our group to establish a connection between a webbrowser and the server.

