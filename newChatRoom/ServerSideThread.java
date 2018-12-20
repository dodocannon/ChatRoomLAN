import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/*
 * ServerSideThread.java
 * @author Kevin Kim
 * @version 1.8.0_141 2/8/18
 */
public class ServerSideThread implements Runnable {
	// current connected socket
	private Socket currSocket;
	// name of socket address
	private String address;
	// name of user that user inputs
	private String name;
	// reads information from socket
	private BufferedReader in;
	// this printwriter to broadcast to sockets
	private PrintWriter out;
	// time stamp to get time
	private String timeStamp;

	// enables color output in unix machines
	protected static final String ANSI_RESET = "\u001B[0m";
	protected static final String ANSI_BLACK = "\u001B[30m";
	protected static final String ANSI_RED = "\u001B[31m";
	protected static final String ANSI_GREEN = "\u001B[32m";
	protected static final String ANSI_YELLOW = "\u001B[33m";
	protected static final String ANSI_BLUE = "\u001B[34m";
	protected static final String ANSI_PURPLE = "\u001B[35m";
	protected static final String ANSI_CYAN = "\u001B[36m";
	protected static final String ANSI_WHITE = "\u001B[37m";

	public ServerSideThread(Socket currSocket) {
		this.currSocket = currSocket;
		address = currSocket.getLocalAddress().getHostName();

	}

	public void run() {
		try {
			out = new PrintWriter(currSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					currSocket.getInputStream()));
			/*
			 * can't use broadcast here, since it is only outputting to the specific client to enter their name,
			 * whereas broadcast outputs it all to the connected sockets, asking for their names,
			 * and that's not the goal here.
			 */

			out.println("Server > Enter your name: ");
			name = in.readLine().toUpperCase();

			out.println("Server > Welcome " + name);
			broadcast(name + " has joined the room", true);

			// server side socket listens for user input
			while (true) {
				out = new PrintWriter(currSocket.getOutputStream(), true);

				// stdin is user input from current socket
				String stdin = in.readLine();

				/* if the input is null, then the server implies the user has
				 * disconnected, and calls disconnect()
				 */

				if (stdin == null) {
					disconnect();
					return;
				}
				// server broadcasts message to all connected sockets
				broadcast(stdin, false);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void disconnect() throws IOException
	{
		// removes the currentsocket from the arraylist of connected sockets.
		for(int i = 0; i < ServerSide.CONNECTIONS.size(); i++)
			{
				if (ServerSide.CONNECTIONS.get(i) == currSocket)
				{
					ServerSide.CONNECTIONS.remove(i);
				}
			}

			broadcast(name + " has disconnected",true);
			timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar
					.getInstance().getTime());
			System.out.println(timeStamp + "[" + ANSI_RED + "ServerThread/DISCONNECT" + ANSI_RESET + "]:" + address + "(" + name +")");
	}

	private void broadcast(String x, boolean server) throws IOException {
		String userOut = "";
		String serverOut = "";

		timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar
				.getInstance().getTime());

		if (server) {
			serverOut += timeStamp + "["+ ANSI_PURPLE + "ServerThread/BORADCAST" + ANSI_RESET + "]: ";
			userOut += "Server: ";
		} else {
			serverOut += timeStamp + "["+ ANSI_CYAN + "ServerThread/TRAFFIC" + ANSI_RESET + "]: ";
			serverOut +=  address + "(" + name +") > ";
			userOut += name + ": ";

		}
		System.out.println(serverOut + x);
		for (Socket s : ServerSide.CONNECTIONS) {
			out = new PrintWriter(s.getOutputStream(), true);
			if (currSocket == s)
			{
				out.println("you: " + x);
			}
			out.println(userOut + x);


		}
	}

}
