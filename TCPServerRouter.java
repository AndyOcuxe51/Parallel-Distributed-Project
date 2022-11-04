package proj_2_new;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerRouter {

	public static void main(String args[]) throws IOException {
		new TCPServerRouter();
	}

	public TCPServerRouter() throws IOException {
		Object[][] routingTable = new Object[101][2]; // routing table

		ServerSocket serverSocket = null; // server socket for accepting connections
		serverSocket = new ServerSocket(5555);
		int index = 0;

		System.out.println(InetAddress.getLocalHost().getHostName());
		// the other computer we are looking for
		String otherRouterIP = "";

		// sends the message to the other router
		Socket socket = new Socket(otherRouterIP, 5556);
		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
		writer.println("LogMe");
		writer.println("");
		socket.close();

		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println("New connection to R1: " + clientSocket);
				SThread thread = new SThread(routingTable, clientSocket, index);
				thread.start();
				index += 1;
			} catch (IOException e) {
				System.err.println("Client/Server failed to connect.");
				System.exit(1);
			}
		}
	}
}
