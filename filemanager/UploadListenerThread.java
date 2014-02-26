//###############
//FILE : UploadListenerThread.java
//WRITER : tamirtf77
//DESCRIPTION: The thread of an FM which listens for requests
// from other FM in order to get a file.
//###############
package oop.ex3.filemanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class UploadListenerThread extends Thread {

	/** The default timeout of the upload listener. */
	private static final int DEFAULT_TIMEOUT = 5000;
	
	/** The port which the FM listens. */
	private int _port;
	
	/** The FM's DB. */
	private FMDB _info;
	
	/** The server's socket. */
	private ServerSocket _serverSocket;
	
	/** An array of all the files which are uploaded from 
	 * this FM. */
	private ArrayList<SingleFileUploadThread> _singleFileUploadThreads;
	
	/** An indicator to know if the FM listener is still alive. */
	public volatile boolean _FMIsAlive = true;

	/** Constructs a new UploadListenerThread .*/
	public UploadListenerThread(int port,FMDB info){
		_port = port;
		_info = info;
		_singleFileUploadThreads = new ArrayList<SingleFileUploadThread>();
	}
	
	@Override
	public void run() {		
		while (_FMIsAlive){
			try {
				_serverSocket = new ServerSocket(_port);
				_serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
				try{
					acceptRequests(_serverSocket);
				}
				catch (SocketTimeoutException e) {
					closeServerSocket(_serverSocket);
					this.run();
				}
			} 
			catch (IOException e) {
				//We assume it won't happen.
			}
			catch (SecurityException e2){
				//We assume it won't happen.
			}
		}
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
		}
	}
	
	/** Accepts requests from other FMs.
	 * 
	 * @param serverSocket the server socket.
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	private void acceptRequests(ServerSocket serverSocket)
			throws IOException,SocketTimeoutException{
		Socket clientSocket = null;
		while (_FMIsAlive) {
			clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(DEFAULT_TIMEOUT);
			// it that period of time _FMIsAlive could be false
			//so we should not really does this request.
			if (_FMIsAlive){
				SingleFileUploadThread thread = 
					new SingleFileUploadThread(clientSocket,_info);
				addThread(thread);
				thread.start();
			}
		}
	}
	
	/** Adds upload FM-FM session (thread) to the list
	 *  of uploads sessions (threads).
	 * @param thread the FM-FM session (thread) to add.
	 */
	private void addThread(SingleFileUploadThread thread){
		_singleFileUploadThreads.add(thread);
	}
	
	/** Waits until all the uploads of a specific file to be finished.
	 * 
	 * @param fileName the file which it should wait until all
	 *  its uploads are finished.
	 */
	public void waitTillSingleFileUploadFinish(String fileName){
		for (int i=0; i <_singleFileUploadThreads.size();i++){
			if (_singleFileUploadThreads.get(i).
					getFileWanted().equals(fileName)){
				try {
					_singleFileUploadThreads.get(i).join();
				} 
				catch (InterruptedException e) {
				}
			}
		}
	}
	
	/** Waits until all the uploads of files to be finished.
	 * 
	 */
	public void waitTillAllUploadsFinish(){
		for (int i=0; i <_singleFileUploadThreads.size();i++){
			try {
				_singleFileUploadThreads.get(i).join();
			} 
			catch (InterruptedException e) {
			}
		}
		closeServerSocket(_serverSocket);
	}
	
	/** Sets the the indicator to know if 
	 * 	the FM listener is still alive to false.
	 * 
	 */
	public void FMIsQuit(){
		_FMIsAlive = false;	
	}
}