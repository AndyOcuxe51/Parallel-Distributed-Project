package proj_2_new;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerRouter2
{
	
	public static void main(String args[]) throws IOException
	{
		new TCPServerRouter2();
	}
	
	private String thisIP = "";
	//boolean flag to send this to other router first, then never again
	private boolean isRouterLogged = false;
	
	public TCPServerRouter2() throws IOException
	{
		//100 spaces as directed by the instructions, 100 clients and 100 servers in their respective routers
		Object[][] routingTable = new Object[101][2]; // routing table
	      
	      ServerSocket serverSocket = null; // server socket for accepting connections
		  serverSocket = new ServerSocket(5556);
	      int index = 0;

	      thisIP = InetAddress.getLocalHost().getHostAddress();
		System.out.println("TCPServerRouter2 IP: " + thisIP);

	      
	      System.out.println("R2 waiting for connections!");
	      while(true) {
	         try {
	            Socket clientSocket = serverSocket.accept();
	            System.out.println("New connection to R2: " + clientSocket);
	            SThread thread = new SThread(routingTable, clientSocket, index);
				 thread.start();
				 index+=1;


				 //the other computer we are looking for
				 String otherRouterIP = thisIP; //thisIP has to be changed to specific destinationIP if a remote computer is used

				 if(!isRouterLogged)
				 {
					 //sends the message to the other router
					 Socket socket = new Socket(otherRouterIP, 5555);
					 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
					 writer.println("LogMe");
					 writer.println(thisIP);
					 socket.close();
					 isRouterLogged = true;
		         } 
				 
			  } catch (IOException e) 
	         {
		            System.err.println("Client/Server failed to connect.");
		            System.exit(1);
		         }
	      }
	}
}
	
