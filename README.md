ComProt
=======

TODO: Use JSon (instead of Strings) and jSSC (instead of RXTX). Maybe: Implement an algorithm that receives feedback from the server on the processing time and calculate the average and use it with the algorithm implemented in v.0.5 to get more precise results. 

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
