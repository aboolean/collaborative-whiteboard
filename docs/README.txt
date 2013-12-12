########################################
########### RUN INSTRUCTIONS ###########
########################################

Server:
1. Run the server application.
	A. Run "app/WhiteboardServer.jar" directly.
	B. Alternatively, build and run "src/server/WhiteboardServer.java" within Eclipse.
2. You will be prompted to select a port.
	(OK to default to PORT 55000)
3. A window will display your IP address and port number. Leave this window open.

Client:
1. Run the client application on any machine.
	A. Run "app/WhiteboardClient.jar" directly.
	B. Alternatively, build and run "src/client/WhiteboardGUI.java" within Eclipse.
2. You will be prompted to enter an IP and port. The information displayed on the server should be entered here.
3. After successfully connecting to the server, you will be prompted to request a username.
4. The main application window will appear. Closing this window will logout the user.