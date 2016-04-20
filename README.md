
#CSC 573 – Internet Protocols
##Project #2
##Spring 2016
###Project Objectives
In this project, you will implement the Go-back-N automatic repeat request (ARQ) scheme and carry out a number
of experiments to evaluate its performance. In the process I expect that you will develop a good understanding of
ARQ schemes and reliable data transfer protocols and build a number of fundamental skills related to writing
transport layer services, including:

* encapsulating application data into transport layer segments by including transport headers,
* buffering and managing data received from, or to be delivered to, the application,
* managing the window size at the sender,
* computing checksums, and
* using the UDP socket interface.

Simple File Transfer Protocol (Simple-FTP)
The FTP protocol provides a sophisticated file transfer service, but uses TCP to ensure reliable data transmission.
You will implement Simple-FTP that provides a simple service: transferring a file from one host to another.
However, Simple-FTP will use UDP to send packets from one host to another, hence it has to implement a reliable
data transfer service using the Go-back-N ARQ scheme. Using the unreliable UDP protocol allows us to implement
a “transport layer” service such as reliable data transfer in user space.
Client-Server Architecture of Simple-FTP
To keep things simple so that you focus on the details of the Go-back-N protocol, you will implement SimpleFTP
in a client-server architecture and omit the steps of opening up and terminating a connection. The Simple-FTP
server will play the role of the receiver in the reliable data transfer, and the Simple-FTP client will play the role of
the sender. All data transfer is from sender (client) to receiver (server) only; only ACK packets travel from receiver
to sender.

###The Simple-FTP Client (Sender)
The Simple-FTP client implements the sender in the reliable data transfer. When the client starts, it reads data from a
file specified in the command line, and calls rdt_send() to transfer the data to the Simple-FTP server. For this
project, we will assume that rdt_send() provides data from the file on a byte basis. The client also implements the
sending side of the reliable Go-back-N protocol, receiving data from rdt_send(), buffering the data locally, and
ensuring that the data is received correctly at the server. The client also reads the value of the maximum segment
size (MSS) from the command line. The Go-back-N buffers the data it receives from rdt_send() until it has at
least one MSS worth of bytes. At that time it forms a segment that includes a header and MSS bytes of data; as a
result, all segments sent, except possibly for the very last one, will have exactly MSS bytes of data.
The client also reads the window size N from the command line, and implements the sending size of the Go-back-N
protocol. Specifically, if less than N segments are outstanding (i.e., have not been ACKed), it transmits the newly
formed segment to the server in a UDP packet. Otherwise, it buffers the segment and waits until the window has
advanced to transmit it. Note that if N = 1, the protocol reduces to Stop-and-Wait.
The header of the segment contains three fields:

* a 32-bit sequence number,
* a 16-bit checksum of the data part, computed in the same way as the UDP checksum, and
* a 16-bit field that has the value 0101010101010101, indicating that this is a data packet.

For this project, you may have the sequence numbers start at 0.
The client implements the full Go-back-N protocol as described in the book, including setting the timeout counter,
processing ACK packets (discussed shortly), advancing the window, and retransmitting packets as necessary 

###The Simple-FTP Server (Receiver)
The server listens on the well-known port 7735. It implements the receive side of the Go-back-N protocol, as
described in the book. Specifically, when it receives a data packet, it computes the checksum and checks whether it
is in-sequence, and if so, it sends an ACK segment (using UDP) to the client; it then writes the received data into a
file whose name is provided in the command line. If the packet received is out-of-sequence, or the checksum is
incorrect, it does nothing.
The ACK segment consists of three fields and no data:

* the 32-bit sequence number that is being ACKed,
* a 16-bit field that is all zeroes, and
* a 16-bit field that has the value 1010101010101010, indicating that this is an ACK packet.

####Generating Errors
Despite the fact that UDP is unreliable, the Internet does not in general lose packets. Therefore, we need a
systematic way of generating lost packet so as to test that the Go-back-N protocol works correctly (and to obtain
performance measurements, as will be explained shortly).
To this end, you will implement a probabilistic loss service at the server (receiver). Specifically, the server will read
the probability value p, 0 < p < 1 from the command line, representing the probability that a packet is lost. Upon
receiving a data packet, and before executing the Go-back-N protocol, the server will generate a random number r in
(0, 1). If r ≤ p, then this received packet is discarded and no other action is taken; otherwise, the packet is accepted
and processed according to the Go-back-N rules.

###Command Line Arguments
The Simple-FTP server must be invoked as follows:

Simple_ftp_server port# file-name p

where port# is the port number to which the server is listening (for this project, this port number is always 7735),
file-name is the name of the file where the data will be written, and p is the packet loss probability discussed
above.

The Simple-FTP client must be invoked as follows:

Simple_ftp_server server-host-name server-port# file-name N MSS

where server-host-name is the host name where the server runs, server-port# is the port number of the server
(i.e., 7735), file-name is the name of the file to be transferred, N is the window size, and MSS is the maximum
segment size.

###Output
The code you submit must print the following to the standard output:

* Simple-FTP server: whenever a packet with sequence number X is discarded by the probabilistic loss
service, the server should print the following line:
Packet loss, sequence number = X

* Simple-FTP client: whenever a timeout occurs for a packet with sequence number Y , the client should
print the following line:
Timeout, sequence number = Y 

###Task 1: Effect of Window Size N
For this first task, MSS was set to 500 bytes and the loss probability p was set to 0.05. 
Go-back-N protocol was run to transfer the file and for the value of the window size N = 1,
2, 4, 8, 16, 32, 64, 128, 256, 512, 1024.
For each value of N, file was transmitted 5 times and average delay was computed over
the five transmissions. Report can be found in Report.xlsx

###Task 2: Effect of MSS
In this task, window size is fixed to N = 64 and the loss probability p = 0.05. Go-back-N protocol was run to
transfer the same file, and MSS was changed from 100 bytes to 1000 bytes in increments of 100 bytes. For each value of
MSS, file was transmitted 5 times, and average delay was computed over the five transmissions.

###Task 3: Effect of Loss Probability p
For this task, MSS was set to 500 bytes and the window size N = 64. Go-back-N protocol was run to transfer the
same file, and loss probability was changed from p = 0.01 to p = 0.10 in increments of 0.01. For each value of p file was transmitted 5 times, and average delay was computed over the five transfers.

###Instructions to Compile and Run

####Compiling

Source files are availabe in src folder which can be compiled by running javac. Output classes should be placed under bin directory. Alternatively, Compile.sh can be run to compile the files.

./Compile.sh 

before running any of the shell files, use "chmod 744 *.sh" to make these files executable

####Executing

To Run Server(receiver), run following shell script

./Server.sh &lt;port#&gt; &lt;file-name&gt; &lt;p&gt; [SR]

To start a client(sender) with Go Back N, run following command

./Client.sh &lt;server-host-name&gt; &lt;server-port#&gt; &lt;file-name&gt; &lt;N&gt; &lt;MSS&gt;


To start a client(sender) with Selective Repeat, run following command

./Client.sh &lt;server-host-name&gt; &lt;server-port#&gt; &lt;file-name&gt; &lt;N&gt; &lt;MSS&gt; <b>SR</b>
