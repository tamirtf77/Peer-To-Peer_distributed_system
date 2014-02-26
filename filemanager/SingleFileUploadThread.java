//###############
//FILE : SingleFileUploadThread.java
//WRITER : tamirtf77
//DESCRIPTION: Does the FM -FM session, 
//the side of the supplier FM.
//###############
package oop.ex3.filemanager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import oop.ex3.protocol.BasicMessage;
import oop.ex3.protocol.FileContentsMessage;
import oop.ex3.protocol.FileNameMessage;
import oop.ex3.protocol.Message;
import oop.ex3.protocol.MessageNames;
import oop.ex3.protocol.NotSupposedToGetException;
import oop.ex3.protocol.Session;

public class SingleFileUploadThread extends Thread {
	
	/** The client (another FM, who request the file). */
	Socket _clientSocket;
	
	/** The FM's DB. */
	FMDB _info;
	
	/** The file wanted. */
	String _fileWanted;
	
	/** Constructs new singleFileUploadThread. */
	public SingleFileUploadThread(Socket clientSocket,FMDB info){
		_clientSocket = clientSocket;
		_info = info;
		_fileWanted = null; 
	}

	@Override
	public void run() {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(_clientSocket.getOutputStream());
		} 
		catch (IOException e) {
		}
		DataInputStream in = null;
		try {
			in = new DataInputStream(_clientSocket.getInputStream());
		} 
		catch (IOException e) {
		}
		try {
			receiveSend(in,out,_info);
		} 
		catch (NotSupposedToGetException e) {
				sendError(out);
				closeAll(out,in,_clientSocket);
		}
	}
	
	/** In case the this FM sends ERROR it should close
	 * the streams and the client.
	 * @param out the DataOutputStream.
	 * @param in the DataInputStream.
	 * @param clientSocket the socket.
	 */
	private void closeAll(DataOutputStream out,DataInputStream in,
			Socket clientSocket){
		try{
			in.close();
		}
		catch(IOException e){
			//We assume it won't happen.
		}
		try{
			out.close();
		}
		catch(IOException e){
			//We assume it won't happen.
		}
		try{
			clientSocket.close();
		}
		catch(IOException e){
			//We assume it won't happen.
		}
	}
	
	/** Receives and Sends messages.
	 * 
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream.
	 * @param info The FM's DB.
	 * @throws NotSupposedToGetException
	 */
	private void receiveSend(DataInputStream in,DataOutputStream out,
							FMDB info) throws NotSupposedToGetException{
		Session session = new Session();
		ArrayList<String> receiveNames = session.FMSupplyReceiveUploadSession();
		receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
		String sendName = session.FMSupplySendUploadSession(info.getAvailability(getFileWanted()));
		send(sendName,info,session,out);
	}
		
	/** Receives a message.
	 * 
	 * @param receiveNames the receives' names (possibilities) to receive. 
	 * @param index the index in order to get a receive's name
	 * 			possibility from the receiveNames.
	 * @param chance the String which represenets the chance (first or second)
	 * 				the message.
	 * @param info the FM' DB.
	 * @param session the current session.
	 * @param in the DataInputStream.
	 * @param messageNameGet the message's name which was received in 
	 * the first chance.
	 * @throws NotSupposedToGetException
	 */
	private void receive(ArrayList<String> receiveNames,
			int index,String chance,FMDB info,
			Session session, DataInputStream in,
			String messageNameGet) 
			throws NotSupposedToGetException {
		Message message = null;
		if (index < receiveNames.size()){
			message = createReceiveMessage(receiveNames.get(index),
							info,in,chance,messageNameGet);
			try{
				message.receiveMessage();
				session.setPreviousReceiveMessage(receiveNames.get(index));
				setFileWanted((((FileNameMessage)(message)).getFileName()));
			}
			catch(NotSupposedToGetException e){
				receive(receiveNames,index+1,
						MessageNames.SECOND_CHANCE,info,session,in,
						message.getMessageNameGet());
			}		
		}
		else
			throw new NotSupposedToGetException();
	}
	
	/** Sends a message.
	 * 
	 * @param sendName the message's name to send.
	 * @param info the FM' DB.
	 * @param session the current session.
	 * @param out the DataOutputStream.
	 */
	private void send(String sendName,FMDB info,Session session,
			DataOutputStream out) {
		Message message = createSendMessage(sendName,info,out);
		message.sendMessage();
		session.setPreviousSendMessage(sendName);
	}
	
	/** Sets the file's name wanted.
	 * 
	 * @param fileWanted the file's name wanted.
	 */
	public void setFileWanted(String fileWanted){
		_fileWanted = fileWanted;
	}
	
	/** Gets the the file's name wanted.
	 * 
	 * @return file's name wanted.
	 */
	public String getFileWanted(){
		return _fileWanted;
	}

	/** Creates the message to receive.
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param in the DataInputStream.
	 * @param chance the chance to receive the message.
	 * @param messageNameGet the message's receive name of the 
	 * first chance.
	 * @return the message.
	 * @throws NotSupposedToGetException
	 */
	private Message createReceiveMessage(String messageName,FMDB info,
			DataInputStream in,String chance,String messageNameGet)
			throws NotSupposedToGetException {
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.FILENAMEMESSAGE))
			message = createFileNameMessage(messageName,info,in,chance);
		
		 // If it can't create a message 
		 // (problem with the receive-send session)
		 // then it should throw an exception of NotSupposedToGetException.
		if (message == null){
			 throw new NotSupposedToGetException();
		}
		return message;
	}
	
	/** Creates a FileName message to receive (such as WANTFILE).
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param in the DataInputStream.
	 * @param chance the chance to receive the message.
	 * @return the FileName message.
	 * @throws NotSupposedToGetException
	 */
	private Message createFileNameMessage(String messageName, FMDB info,
			DataInputStream in,String chance) throws NotSupposedToGetException {
		Message message = null;
		if (messageName.equals(MessageNames.WANTFILE))
			message = new FileNameMessage(messageName,
					MessageNames.getMessageEnd(),in,chance);
		return message;
	}
	
	/** Creates the message To send.
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param out the DataOutputStream.
	 * @return the message.
	 */
	private Message createSendMessage(String messageName,
			FMDB info,DataOutputStream out){
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.FILECONTENTSMESSAGE))
			message = createFileContentsMessage(messageName,info,out);
		if (messageType.equals(MessageNames.BASICMESSAGE))
			message = new BasicMessage(messageName,MessageNames.getMessageEnd(),out);
		return message;
	}

	/** Creates a file contents message to send(FILE).
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param out the DataOutputStream.
	 * @return the FileContents message.
	 */
	private Message createFileContentsMessage(String messageName, FMDB info,
			DataOutputStream out) {
		Message message = null;
		if (messageName.equals(MessageNames.FILE)){
			message = new FileContentsMessage(messageName,
					info.getFileDirPath() + MyFileManager.SLASH +
					this.getFileWanted(),
					MessageNames.getMessageEnd(),out);
		}
		return message;
	}
	
	/** Sends ERROR message when the FM receives a message it
	 *  does not suppose to receive.
	 * @param out the DataOutputStream.
	 */
	private void sendError(DataOutputStream out){
		Message messageToSend = createSendMessage(MessageNames.ERROR,_info,out);
		messageToSend.sendMessage();
	}
}