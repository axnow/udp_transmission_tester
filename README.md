# udp_transmission_tester

Simple gui tool used during equipment tests. It is used to test underlying connection, like wifi transmission by 
communincating with some with UDP echo server.

It is a GUI tool, that plots basic data - packages sent, packages received and 
delay of the answer.

To create single jar executable use:
```
mvn clean install assembly:single
```
##Running the code:
Simple run with executable jar:
```
java  -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar
```
On high dpi monitors it may be useful to run:
```
java  -Dsun.java2d.uiScale=2 -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar 
```


