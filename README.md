# serverProjectSSL
MPCS 54001 - Networks

CJ Hodnefield and Omar Latif

The main function to run the server is in WebServer.java, with flags --serverPort=<port#> and --sslServerPort=<SSLport#>.

WebServer.java:
This is a Runnable class that handles all of the basic functionality of our server.

WebServerSSL.java:
This class extends WebServer and adds functionality to support HTTPS requests on an SSL server. All it really adds on top of WebServer is a new constructor.

Utils.java:
This file includes some utility functions as well as all of our other classes, Request, Response, and RedirectMap.

Persistent Connections:
Our server passes all of the tests for persistent connections in the Python test script that was provided.
However, we had some trouble getting persistent connections to work in any browser.
