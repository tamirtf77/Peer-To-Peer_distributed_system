//###############
//FILE : MyNameServer.java
//WRITER : tamirtf77
//DESCRIPTION: Initialize a NS, which listens 
//(from the main thread) for requests from FMs.
//###############
package oop.ex3.nameserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class MyNameServer {
	
	public static void main(String[] args) throws InvalidUsageException, InitializeException{
		if (args.length < 1)
			throw new InvalidUsageException();
		MyNameServer server = new MyNameServer(args[0]);
		server.createServerSocket();
	}
	
	/** The default timeout of the NS listener. */
	private static final int DEFAULT_TIMEOUT = 5000;
	
	/** The port which the NS listens. */
	private int _port;
	
	/** The NS's DB. */
	private NSDB _info;
	
	/** The requests (threads) of this NS .*/
	private ArrayList<Thread> _requestThreads;
	
	/** An indicator to know if the NS(listener) is still alive. */
	volatile boolean _NSIsAlive = true;
	
	ServerSocket _serverSocket;
	
	/**  Constructs a new MyNameServer.
	 * 
	 * @param port the NS's port to listen.
	 * @throws InvalidUsageException
	 * @throws InitializeException
	 */
	public MyNameServer(String port) throws InvalidUsageException, InitializeException{
		_port = convertPort(port);
		_info = new NSDB();
		_requestThreads = new ArrayList<Thread>();
	}
	
	/** Converts the input string which represents the port
	 * to an integer.
	 * @param port the given input parameter (String).
	 * @return Integer representation of the port.
	 * @throws InvalidUsageException
	 * @throws InitializeException 
	 */
	private int convertPort(String port) 
		throws InvalidUsageException, InitializeException{
		int portInt = -1;
		try{
			portInt = Integer.parseInt(port);
		}
		catch (Exception E){
			throw new InvalidUsageException();
		}
		if ((portInt > 0) && (portInt < 1024))
			throw new InitializeException(); 
		return portInt;
	}
	
	/** Creates the server socket of the NS.
	 * 
	 * @throws InvalidUsageException
	 */
	private void createServerSocket() 
		throws InvalidUsageException{
		while (_NSIsAlive){
			try {
				_serverSocket = new ServerSocket(_port);
				_serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
				try{
					acceptRequest(_serverSocket,_info);
				}
				catch (SocketTimeoutException e) {
					closeServerSocket(_serverSocket);
					createServerSocket();
				}
				catch (IOException e) {
					
				}
			}
			catch (IllegalArgumentException e){
				// case the port is < 0 or > 65535
				throw new InvalidUsageException();
			}
			catch (IOException e) {
				
			}
		}	
    	waitTillAllRequestsFinish();
	}
	
	/** Closes the server socket.
	 * 
	 * @param serverSocket the server socket.
	 */
	private void closeServerSocket(ServerSocket serverSocket){
		try {
			serverSocket.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
	}
	
	/** Accepts requests from FMs.
	 * 
	 * @param serverSocket the server socket.
	 * @param info the NS's DB.
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	private void acceptRequest(ServerSocket serverSocket,NSDB info)
	throws IOException,SocketTimeoutException {
		Socket clientSocket = null;
		while (_NSIsAlive) {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(DEFAULT_TIMEOUT);
				// it that period of time _NSIsAlive could be
				// false so we should not really does this request.
				if (_NSIsAlive){
					NSToFMSession request = new NSToFMSession(this,clientSocket,info);
					Thread thread = new Thread(request);
					addThread(thread);
					thread.start();
				}
		}
	}
	
	/** Adds a request (thread) to the list of requests. */
	private void addThread(Thread thread){
		_requestThreads.add(thread);
	}
	
	/** Waits until all the requests to be finished.
	 * 
	 */
	private void waitTillAllRequestsFinish(){
		for (int i=0; i <_requestThreads.size();i++){
			try {
				_requestThreads.get(i).join();
			} 
			catch (InterruptedException e) {
			}
		}
	}
	
	/** Sets the the indicator to know if 
	 * 	the NS listener is still alive to false.
	 * 
	 */
	public void NSIsQuit(){
		_NSIsAlive = false;
	}
}