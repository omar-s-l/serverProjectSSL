# serverProjectSSL
MPCS 54001 - Networks

CJ Hodnefield and Omar Latif

The main function to run the server is in WebServer.java, with flags --serverPort=<port#> and --sslServerPort=<SSLport#>.

WebServer.java:
This is a Runnable class that handles all of the basic functionality of our server. As mentioned above, the main function to run the server also resides here.

WebServerSSL.java:
This class extends WebServer and adds functionality to support HTTPS requests on an SSL server. All it really adds on top of WebServer is a new constructor and a slightly altered start() method.

Utils.java:
This file includes some utility functions as well as all of our other classes, Request, Response, and RedirectMap.

Persistent Connections:
Our server passes all of the tests for persistent connections in the Python test script that was provided.
However, we had some trouble getting persistent connections to work in any browser. We verified that the browsers were receiving the first response the server sent (including a Connection: keep-alive), but for some reason, the browser refused to render that first response while keeping the connection alive. Basically, it seemed like the browser page was loading forever, even though it had the response.
We're hoping that passing the test script tests is sufficient for this project, as we were not able to successfully debug the browser issue (and we did try to do that for quite a while). If you know a quick fix for this issue, we'd be interested in learning about it.
