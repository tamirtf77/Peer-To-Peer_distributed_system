//###############
//FILE : NSToFMSession.java
//WRITER : tamirtf77
//DESCRIPTION: Does the session in the side of the NS - 
// sends and receives messages from / to the FM.
//###############
package oop.ex3.nameserver;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import oop.ex3.nameserver.NSDB.AddressRecord;
import oop.ex3.protocol.BasicMessage;
import oop.ex3.protocol.FileNameMessage;
import oop.ex3.protocol.Message;
import oop.ex3.protocol.MessageNames;
import oop.ex3.protocol.NetAddressMessage;
import oop.ex3.protocol.NotSupposedToGetException;
import oop.ex3.protocol.Session;

public class NSToFMSession implements Runnable{
	
	/** The NS. */
	MyNameServer _NS;
	
	/** The client's socket. */
	Socket _clientSocket;
	
	/** The current session. */
	Session _session;
	
	/** The NS's DB. */
	NSDB _info;
	
	/** The FM's address 
	 * (The FM which the current NS talks with). */
	AddressRecord _FMAddress;
	
	/** The FM's,which was deleted (the FM sends to the NS
	 * GOODBYE), address.
	 */
	AddressRecord _FMAddressDeleted;
	
	/** The file's name of the file wanted(FM sends WANTFILE)
	 *  or added(FM sends CONTAINFILE, not in the introduction). */
	String _fileWantedOrAdded;
	
	/** The file's name which was deleted 
	 * (FM sends DONTCONTAINFILE). */
	String _fileDeleted;
	
	/** An indicator if a FM sends GOAWAY to this NS. */
	boolean _goAway;
	
	/** Constructs new NSToFMSession.*/
	public NSToFMSession (MyNameServer NS,Socket clientSocket,NSDB info) {
		_NS = NS;
		_clientSocket = clientSocket;
		_info = info;
		_FMAddress = null;
		_FMAddressDeleted = null;
		_fileWantedOrAdded = null;
		_fileDeleted = null;
		_goAway = false;
	}
	
	@Override
	public void run() {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(_clientSocket.getOutputStream());
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
		DataInputStream in = null;
		try {
			in = new DataInputStream(_clientSocket.getInputStream());
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		Session session = new Session();
		setSession(session);
		try{
		doIntroduceNSToFM(_info,session,MessageNames.BEGIN,
				MessageNames.ENDLIST,MessageNames.DONE,in,out);
		_info.copyToNSNames();
		_info.copyFM(getFMAddress());
		doAfterIntroduction(_info,session,MessageNames.ENDSESSION,
					         MessageNames.DONE,in,out);
		saveContent(_info);
		} 
		catch (NullPointerException e){
			
		}
		catch (NotSupposedToGetException e) {
			sendError(out);
			closeAll(_clientSocket,in,out);
		}
		if (wasFireServer()){
			_NS.NSIsQuit();
		}
	}
	
	/** Sets the current session.
	 * 
	 * @param session the current session.
	 */
	private void setSession(Session session){
		_session = session;
	}
	
	/** Gets the current session. it useful for the next method.
	 * 
	 * @return the current session.
	 */
	private Session getSession(){
		 return _session;
	}
	
	/** Checks if the session terminates normally and
	 * the NS gets GOAWAY.
	 * @return true if so else false.
	 */
	private boolean wasFireServer(){
		if ((getSession()
				.previousReceiveSend(MessageNames.ENDSESSION,
									MessageNames.DONE)) &&
									(getGoAway()))
			return true;
		return false;
	}
	
	/** Sends an ERROR message.
	 * 
	 * @param out the DataOutputStream.
	 */
	private void sendError(DataOutputStream out){
		Message messageToSend = createSendMessage(MessageNames.ERROR,_info,null,null,out);
		messageToSend.sendMessage();
	}
	
	/** Close the client's socket,DataInputStream and
	 *  DataOutputStream. This method is after the NS sends ERROR.
	 * @param client the client's socket.
	 * @param in the DataInputStream
	 * @param out the DataOutputStream
	 */
	private void closeAll(Socket client,DataInputStream in,
			DataOutputStream out){
		try {
			in.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		try {
			out.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		try{
			client.close();
		}
		catch (IOException e){
			//We assume it won't happen.
		}
	}
	
	/** Does the introduction between NS to FM.
	 * 
	 * @param info the NS's DB.
	 * @param session the current session.
	 * @param previousReceive1 the String of the first option
	 *                       of the previous receive message.
	 * @param previousReceive2 the String of the second option
	 * 						 of the previous receive message.
	 * @param previousSend the String of the previous send message.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream
	 * @throws NotSupposedToGetException
	 */
	private void doIntroduceNSToFM(NSDB info,Session session,
			String previousReceive1,String previousReceive2,
			String previousSend,DataInputStream in, 
			DataOutputStream out) throws NotSupposedToGetException{
		ArrayList<String> receiveNames = session.NSMessageToReceive(false);
		receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
		String sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),true);
		send(sendName,session,info,null,null,out);
		//if it is BEGIN - DONE then it will not go to the
		// next IF.
		if (!session.previousReceiveSend(previousReceive1,previousSend)){
			//gets CONTAINFILE.
			while (!session.previousReceiveSend(previousReceive2,previousSend))
				receiveSend(session,info,null,null,false,out,in);
			//gets the first of CONTAINNAMESERVER.
			receiveSend(session,info,null,null,false,out,in);
			session.wasContainNameServer();
			//gets CONTAINNAMESERVER.
			while (!session.previousReceiveSend(previousReceive2,previousSend))
				receiveSend(session,info,null,null,false,out,in);
			// copies the information about the current FM from the
			// temp list to the constant list.
			info.copyFM(getFMAddress());
		}
	}
	
	/** Receives and sends messages.
	 * 
	 * @param session the current session.
	 * @param info the NS's DB.
	 * @param fileName a file's Name to send.
	 * @param address the address to send.
	 * @param has true if it has more files / servers to send.
	 * @param out the DataOutputStream.
	 * @param in the DataInputStream.
	 * @throws NotSupposedToGetException
	 */
	private void receiveSend(Session session,
			NSDB info, String fileName,
			AddressRecord address,boolean has,DataOutputStream out,
			DataInputStream in) 
			throws NotSupposedToGetException{
		ArrayList<String> receiveNames = 
			session.NSMessageToReceive(info.containFM(getFMAddress(),NSDB.FM_NAMES));
		receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
		String sendName = 
			session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),has);
		send(sendName,session,info,fileName,address,out);
	}
	
	/** Receives a message.
	 * 
	 * @param receiveNames the receives' names (possibilities) to receive. 
	 * @param index the index in order to get a receive's name
	 * 			possibility from the receiveNames.
	 * @param chance the String which represenets the chance (first or second)
	 * 				the message.
	 * @param info the NS' DB.
	 * @param session the current session.
	 * @param in the DataInputStream.
	 * @param messageNameGet the message's name which was received in 
	 * the first chance.
	 * @throws NotSupposedToGetException
	 */
	private void receive(ArrayList<String> receiveNames,
			int index,String chance,NSDB info,Session session,
			DataInputStream in,String messageNameGet) 
			throws NotSupposedToGetException {
		Message message = null;
		if (index < receiveNames.size()){
			message = createReceiveMessage(receiveNames.get(index),
							in,chance,messageNameGet);
			try{
				message.receiveMessage();
				setFMAddress(message,info);
				session.setPreviousReceiveMessage(receiveNames.get(index));
				saveTempContent(message,getFMAddress(),info);
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
	 * @param session the current session.
	 * @param info the NS' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 */
	private void send(String sendName, Session session,
			NSDB info, String fileName,
			AddressRecord address,
			DataOutputStream out) {
		Message message = createSendMessage(sendName,info,fileName,address,out);
		message.sendMessage();
		session.setPreviousSendMessage(sendName);
	}

	/** Does the session according to the what 
	 *   a FM requests / inform (such as WANTFILE,
	 *   WANTALLFILES,DONTCONTAINFILE...) 
     *
	 * @param info the NS' DB.
	 * @param session the current session.
	 * @param previousSend the previous send message's name.
	 * @param previousReceive the previous receive message's name.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream. 
	 * @throws NotSupposedToGetException
	 */
	private void doAfterIntroduction(NSDB info,
			Session session,String previousReceive,
			String previousSend,DataInputStream in, 
			DataOutputStream out)
			throws NotSupposedToGetException{
		while (!session.previousReceiveSend(previousReceive, previousSend)){
			ArrayList<String> receiveNames = session.NSMessageToReceive(info.containFM(getFMAddress(),NSDB.FM_NAMES));
			receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
			if (!sendListOf(session,info,out)){
				String sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),false);
				send(sendName,session,info,null,null,out);
			}
		}
	}
	
	/** Sends a list of ,probably FILEADDRESS, NSCONTAINFILE
	 *  or CONTAINNAMESERVER.
	 * 
	 * @param session the current session.
	 * @param info the NS' DB.
	 * @param out the DataOutputStream.
	 * @return true if a list of was sent else false.
	 */
	private boolean sendListOf(Session session,NSDB info,
								DataOutputStream out){
		if (session.getPreviousReceiveMessage().equals(MessageNames.WANTFILE))
			return (sendFMWithWantedFile(session,info,getFileWantedOrAdded(),out));
		if (session.getPreviousReceiveMessage().equals(MessageNames.WANTSERVERS))
			return (sendAllServers(session,info,out));
		if (session.getPreviousReceiveMessage().equals(MessageNames.WANTALLFILES))
			return (sendAllFiles(session,info,out));
		return false;
	}
	
	/** Sends a list of FM who has the file.
	 * 
	 * @param session the current session.
	 * @param info the NS's DB.
	 * @param fileName the wanted file's name.
	 * @param out the DataOutput stream.
	 * @return true if there is at least one FM who has the file,
	 * else return false (FILENOTFOUND).
	 */
	private boolean sendFMWithWantedFile(Session session,NSDB info,
			String fileName,DataOutputStream out){
		ArrayList<AddressRecord> addresses = info.getFMContainsFile(getFileWantedOrAdded());
		if (addresses.size() > 0){
			String sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),true);
			for (AddressRecord address: addresses){
				send(sendName,session,info,getFileWantedOrAdded(),
						address,out);
			}
			// sends ENDLIST.
			sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),false);
			send(sendName,session,info,getFileWantedOrAdded(),
					null,out);
			return true;
		}
		return false;
	}
	
	/** Sends all the NS that are known to the NS.
	 * 
	 * @param session the current session.
	 * @param info the NS's DB.
	 * @param out the DataOutput stream.
	 * @return true if the NS sends at least one NS else false.
	 */
	private synchronized boolean sendAllServers(Session session,NSDB info,
			DataOutputStream out) {
		int size = info.getNSSize(NSDB.NS_NAMES);
		AddressRecord NSAddress;
		if (size > 0){
			String sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),true);
			for (int i=0;i<info.getNSSize(NSDB.NS_NAMES);i++){
				NSAddress = info.getNS(i, NSDB.NS_NAMES);
				send(sendName,session,info,null,
						NSAddress,out);
			}
			// sends ENDLIST.
			sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),false);
			send(sendName,session,info,getFileWantedOrAdded(),
					null,out);
			return true;
		}
		return false;
	}
	
	/** Sends all files that are known to the NS.
	 * 
	 * @param session the current session.
	 * @param info the NS's DB.
	 * @param out the DataOutput stream.
	 * @return true if at least one file was sent else false.
	 */
	private boolean sendAllFiles(Session session,NSDB info,
			DataOutputStream out) {
		TreeSet<String> allFiles = info.getAllFiles();
		String fileName;
		if (allFiles.size() > 0){
			String sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),true);
			synchronized (allFiles){
				Iterator<String> iter = allFiles.iterator();
				while (iter.hasNext()){
					fileName = iter.next();
					send(sendName,session,info,fileName,
							null,out);
				}
			}
			// sends ENDLIST.
			sendName = session.NSMessageToSend(info.containFM(getFMAddress(),NSDB.FM_NAMES),false);
			send(sendName,session,info,null,
					null,out);
			return true;
		}
		return false;
	}

	/** Creates the message To receive.
	 * 
	 * @param messageName the message's name.
	 * @param in the DataInputStream.
	 * @param chance the chance to receive the message.
	 * @param messageNameGet the message's receive name of the 
	 * first chance.
	 * @return the message.
	 * @throws NotSupposedToGetException
	 */
	private Message createReceiveMessage(String messageName, DataInputStream in,
			 String chance,String messageNameGet) throws NotSupposedToGetException {
		 Message message = null;
		 String messageType = MessageNames.getMessageType(messageName);
		 if (messageType.equals(MessageNames.BASICMESSAGE)){
			 if (chance.equals(MessageNames.FIRST_CHANCE))
				 message = new BasicMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance);
			 if (chance.equals(MessageNames.SECOND_CHANCE))
				 message = new BasicMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance,messageNameGet);
		 }
			
		 // The case of CONTAINNAMESERVER,BEGIN
		 if (messageType.equals(MessageNames.NETADDRESSMESSAGE)){
			 if (chance.equals(MessageNames.FIRST_CHANCE))
				 message = new NetAddressMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance);
			 if (chance.equals(MessageNames.SECOND_CHANCE))
				 message = new NetAddressMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance,messageNameGet);
		 }
		 
		// The case of CONTAINFILE,DONTCONTIANFILE,WANTFILE	
		 if (messageType.equals(MessageNames.FILENAMEMESSAGE)){
			 if (chance.equals(MessageNames.FIRST_CHANCE))
				 message = new FileNameMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance);
			 if (chance.equals(MessageNames.SECOND_CHANCE))
				 message = new FileNameMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance,messageNameGet);
		 }
		 
		 // If it can't create a message 
		 // (problem with the receive-send session)
		 // then it should throw an exception of NotSupposedToGetException.
		 if (message == null){
			 throw new NotSupposedToGetException();
		 }
			
		 return message;
	 }
	 
	/** Sets the FM's , which the NS is talking with, address.
	 * 
	 * @param message the message which it gets the data.
	 * @param info the NS's DB.
	 */
	 private void setFMAddress(Message message,NSDB info){
		 if (message.getMessageNameGet().equals(MessageNames.BEGIN))
			 _FMAddress = info.new AddressRecord( ((NetAddressMessage)(message)).getIP(),
					 ((NetAddressMessage)(message)).getPort());
	 }
	 
	 /** Gets the FM's , which the NS is talking with, address.
	  * 
	  * @return the FM's address.
	  */
	 private AddressRecord getFMAddress(){
		 return _FMAddress;
	 }
	 
	/** Sets the FM's , which quited, address.
	  * 
	  * @param FMAddress the FM's address.
	  */
	 private void setFMAddressDeleted(AddressRecord FMAddress){
		 _FMAddressDeleted = FMAddress;
	 }
	 
	 /** Gets the FM's , which quited, address.
	  * 
	  * @return the FM's address.
	  */
	 private AddressRecord getFMAddressDeleted(){
		 return _FMAddressDeleted;
	 }
	 
	 /** Sets the file's name of the file wanted(FM sends WANTFILE)
	  *  or added(FM sends CONTAINFILE, not in the introduction).
	  * 
	  * @param message the message which it gets the data.
	  */
	 private void setFileWantedOrAdded(Message message){
		 _fileWantedOrAdded = ((FileNameMessage)(message)).getFileName();
	 }
	 
	 /** Gets the file's name of the file wanted(FM sends WANTFILE)
	  *  or added(FM sends CONTAINFILE, not in the introduction).
	  * 
	  * @return the file's name.
	  */
	 private String getFileWantedOrAdded(){
		 return _fileWantedOrAdded;
	 }
	 
	 /** Sets the file's name which was deleted 
	  * (FM sends DONTCONTAINFILE).
	  * 
	  * @param message the message which it gets the data.
	  */
	 private void setFileDeleted(Message message){
		 _fileDeleted = ((FileNameMessage)(message)).getFileName();
	 }
	 
	 /** Gets the file's name which was deleted 
	  * (FM sends DONTCONTAINFILE).
	  * 
	  * @return the file's name.
	  */
	 private String getFileDeleted(){
		return _fileDeleted;
	 }
	 
	 /** Sets the indicator if a FM sends GOAWAY to this NS.
	  * 
	  * @param bool the indicator.
	  */	
	 private void setGoAway(boolean bool) {
			_goAway = bool;
	 }
	 
	 /** Sets the indicator if a FM sends GOAWAY to this NS.
	  * 
	  * @return true if FM sends GOAWAY to this NS else false.
	  */
	 private boolean getGoAway(){
		 return _goAway;
	 }
	 
	 /** Saves temporarily the data which has been received during
	  * the NS-FM session.
	  * @param message the message which it gets the data.
	  * @param FMAddress the FM's address 
	  * 				 which the NS is talking with.
	  * @param info the NS' DB.
	  */
	 private void saveTempContent(Message message,AddressRecord FMAddress, NSDB info) {
		 if (message.getMessageNameGet().equals(MessageNames.BEGIN))
			 info.addFM(info.new AddressRecord(((NetAddressMessage)(message)).getIP(),
					 ((NetAddressMessage)(message)).getPort()),
					 NSDB.FM_NAMES_TEMP);
		 if (message.getMessageNameGet().equals(MessageNames.CONTAINFILE)){
			 info.addFileToFM(FMAddress,((FileNameMessage)(message)).getFileName(),
					 NSDB.FM_NAMES_TEMP);
			 setFileWantedOrAdded(message);
		 }
		 if (message.getMessageNameGet().equals(MessageNames.CONTAINNAMESERVER))
			 info.addNS( ((NetAddressMessage)(message)).getIP(),
					 ((NetAddressMessage)(message)).getPort(),
					 NSDB.NS_NAMES_TEMP);
		 if (message.getMessageNameGet().equals(MessageNames.DONTCONTAINFILE)){
			 info.deleteFileFromFM(FMAddress,((FileNameMessage)(message)).getFileName(),
					 NSDB.FM_NAMES_TEMP);
			 setFileDeleted(message);
		 }
		 if (message.getMessageNameGet().equals(MessageNames.WANTFILE))
			 setFileWantedOrAdded(message);
		 if (message.getMessageNameGet().equals(MessageNames.GOODBYE)){
			 info.deleteFM(FMAddress,NSDB.FM_NAMES_TEMP);
			 setFMAddressDeleted(getFMAddress());
		 }
		 if (message.getMessageNameGet().equals(MessageNames.GOAWAY))
			 setGoAway(true);
	 }
	 
	/** Saves constantly the data which has been received during
	 *   the NS-FM session.
	 * 
	  * @param info the NS' DB.
	 */
	private void saveContent(NSDB info){
		 if (getFileDeleted() != null)
			 info.deleteFileFromFM(getFMAddress(),getFileDeleted(),
					 NSDB.FM_NAMES);
		 if (getFileWantedOrAdded() != null)
			 info.addFileToFM(getFMAddress(), getFileWantedOrAdded(), 
					 NSDB.FM_NAMES);
		 if (getFMAddressDeleted() != null){
			 info.deleteFM(getFMAddressDeleted(),NSDB.FM_NAMES);
		 }
	 }
	
	/** Creates the message To send.
	 * 
	 * @param messageName the message's name.
	 * @param info the NS' DB.
	 * @param fileName the file's name to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the message.
	 */
	private Message createSendMessage(String messageName,
			NSDB info,String fileName,
			AddressRecord address,DataOutputStream out) {
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.BASICMESSAGE))
			message = new BasicMessage(messageName,
					MessageNames.MESSAGEEND,out);
		if (messageType.equals(MessageNames.NETADDRESSMESSAGE))
			message = createNetAddressMessage(messageName,
							info,address,out);
		if (messageType.equals(MessageNames.FILENAMEMESSAGE))
			message = createFileNameMessage(messageName,info,
					fileName,out);
		return message;
	}
	
	/** Creates a NetAddress message 
	 * 	(such as FILEADDRESS,CONTAINNAMESERVER).
	 * 
	 * @param messageName
	 * @param info the NS' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the NetAddress message.
	 */
	private Message createNetAddressMessage(String messageName,
			NSDB info,AddressRecord address,DataOutputStream out){
		Message message = new NetAddressMessage(messageName,
				address.getIP(),address.getPort(),
				MessageNames.getMessageEnd(),out);
		return message;
	}
	
	/** Creates a FileName message (such as NSCONTAINFILE).
	 * 
	 * @param messageName the message's name.
	 * @param info the NS' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the FileName message.
	 */
	private Message createFileNameMessage(String messageName,
			NSDB info,String fileName, DataOutputStream out) {
		Message message = new FileNameMessage(messageName,
				fileName,MessageNames.getMessageEnd(),out);
		return message;
	}
}