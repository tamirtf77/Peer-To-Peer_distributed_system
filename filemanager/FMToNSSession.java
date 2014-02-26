//###############
//FILE : FMToNSSession.java
//WRITER : tamirtf77
//DESCRIPTION: Does the session in the side of the FM - 
// sends and receives messages from / to the NS.
// It also does the session with other FM - here is The FM
// which wants a file. (The side of supplier FM is implemented
// in the SingleFileUpload.java).
//###############
package oop.ex3.filemanager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;


import oop.ex3.filemanager.FMDB.AddressRecord;
import oop.ex3.filemanager.FMDB.FileRecord;
import oop.ex3.protocol.BasicMessage;
import oop.ex3.protocol.FileContentsMessage;
import oop.ex3.protocol.FileNameMessage;
import oop.ex3.protocol.Message;
import oop.ex3.protocol.MessageNames;
import oop.ex3.protocol.NetAddressMessage;
import oop.ex3.protocol.NotSupposedToGetException;
import oop.ex3.protocol.Session;

public class FMToNSSession{
	
	/** The default timeout. */
	private static final int DEFAULT_TIMEOUT = 5000;
	
	/** The invalid session termination's String. */
	private static final String INVALID_SESSION_TERMINATION = 
		"Invalid session termination:No final response from "
		+ "NameServer";
	
	/** The command of the user which according to it,
	 * the FM sends messages. */
	private String _command;
	
	/** The NS's IP to connect to. */
	private String _NSIP;
	
	/** The NS's port to connect to. */
	private int _NSPort;
	
	/** The DB of the FM */
	private FMDB _info;
	
	/** Constructs new FMToNSSession.*/
	public FMToNSSession (String command,
							AddressRecord NS,FMDB info){
		_command = command;
		_NSIP = NS.getIP();
		_NSPort = NS.getPort();
		_info = info;
	}
	
	/** Does the FM to Ns session.
	 * 
	 * @throws IOException
	 * @throws SocketException
	 */
	public void doFMToNSSession() 
			throws IOException,SocketException{
		Socket client = new Socket(_NSIP,_NSPort);
	    client.setSoTimeout(DEFAULT_TIMEOUT);
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(client.getOutputStream());
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
		DataInputStream in = null;
		try {
			in = new DataInputStream(client.getInputStream());
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
		//initiate conversation with NS
		Session session = new Session();
		try{
		doIntroduceFMToNS(_command,_info,session,
				MessageNames.BEGIN,MessageNames.DONE,in,out);
		doAccordingToCommand(_command,_info,session,
						MessageNames.ENDSESSION,
						MessageNames.DONE,in,out);
		saveContent(_info);
		}
		catch (NotSupposedToGetException e){
			try{
				ErrorCase(out,in,client,_info);
			}
			catch (NotSupposedToGetException e2){
				
			}
		}
		try {
			out.close();
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
		try {
			in.close();
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
		try {
			client.close();
		} 
		catch (IOException e) {
			// We assume it won't happen.
		}
	}
	
	/** Does the introduction between FM to NS.
	 * 
	 * @param command The user's command which according to it,
	 * the FM sends messages.
	 * @param info the FM's DB.
	 * @param session the current session.
	 * @param previousSend the String of the previous send message.
	 * @param previousReceive the String of the previous receive message.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream
	 * @throws NotSupposedToGetException
	 */
	private void doIntroduceFMToNS(String command,FMDB info,
			Session session,String previousSend,
			String previousReceive,DataInputStream in, 
			DataOutputStream out) throws NotSupposedToGetException{
		String sendName = null;
		ArrayList<String> receiveNames = new ArrayList<String>();
		if (session.previousSendReceive(MessageNames.EMPTY, MessageNames.EMPTY))
			sendReceive(command,session,info,null,info.getMyAddress(),true,out,in);
		//if it is BEGIN - DONE then it will not go to the
		// while loop.
		if (!session.previousSendReceive(previousSend, previousReceive)){
			if (info.getFilesNamesSize(FMDB.MY_FILES) > 0){
				sendName = session.FMMessageToSend(command,true);
				doIntroduceContainFile(sendName,receiveNames,
						command,FMDB.MY_FILES,info,
						session,in,out);
			}
			// Send ENDLIST.
			sendReceive(command,session,info,null,null,false,out,in);
			if (info.getNSOrFMSize(FMDB.NS_NAMES) > 0){
				sendName = session.FMMessageToSend(command,true);
				doIntroduceContainServer(sendName,receiveNames,
						command,FMDB.NS_NAMES,info,
						session,in,out);
			}
			// Send ENDLIST.
			sendReceive(command,session,info,null,null,false,out,in);
		}
	}
	
	/** Sends and receives messages.
	 * 
	 * @param command The user's command which according to it,
	 * the FM sends messages.
	 * @param session the current session.
	 * @param info the FM' DB.
	 * @param file the file's name to send.
	 * @param address the address to send.
	 * @param has true if it has more files / servers to send.
	 * @param out the DataOutputStream.
	 * @param in the DataInputStream.
	 * @throws NotSupposedToGetException
	 */
	private void sendReceive(String command, Session session,
			FMDB info, FileRecord file,
			AddressRecord address,boolean has,DataOutputStream out,
			DataInputStream in) 
			throws NotSupposedToGetException{
		String sendName = session.FMMessageToSend(command,has);
		send(sendName,session,info,file,address,out);
		ArrayList<String> receiveNames = session.FMMessageToReceive(command);
		receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
	}
	
	/** Sends the CONTAINFILE to the NS.
	 * 
	 * @param sendName the message's name to send.
	 * @param receiveNames the receives' names (possibilities) to receive.
	 * @param command The user's command which according to it,
	 * the FM sends messages.
	 * @param whichList the String that represents the
	 * 					 data structure where the files are kept.
	 * @param info the FM' DB.
	 * @param session the current session.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream.
	 * @throws NotSupposedToGetException
	 */
	private void doIntroduceContainFile(String sendName, 
			ArrayList<String> receiveNames,String command,String which,
			FMDB info, Session session, DataInputStream in,
			DataOutputStream out) throws NotSupposedToGetException {
		ConcurrentSkipListSet<FileRecord> files = 
			             info.getFilesNames(which);
		Iterator<FileRecord> iter = files.iterator();
		while (iter.hasNext()){
			FileRecord file = iter.next();
			send(sendName,session,info,file,null,out);
			receiveNames = session.FMMessageToReceive(command);
			receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
		}
	}
	
	/** Sends the CONTAINFILE to the NS.
	 * 
	 * @param sendName the message's name to send.
	 * @param receiveNames the receives' names (possibilities) to receive.
	 * @param command The user's command which according to it,
	 * the FM sends messages.
	 * @param whichList the String that represents the
	 * 					 data structure where the files are kept.
	 * @param info the FM' DB.
	 * @param session the current session.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream.
	 * @throws NotSupposedToGetException
	 */
	private void doIntroduceContainServer(String sendName, 
			ArrayList<String> receiveNames,String command,String whichList,
			FMDB info, Session session, DataInputStream in,
			DataOutputStream out) throws NotSupposedToGetException {
		for (int i=0; i< info.getNSOrFMSize(whichList);i++){
			send(sendName,session,info,null,info.getNSOrFMName(i, whichList),out);
			receiveNames = session.FMMessageToReceive(command);
			receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
		}
	}

	/** Sends a message.
	 * 
	 * @param sendName the message's name to send.
	 * @param session the current session.
	 * @param info the FM' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 */
	private void send(String sendName, Session session,
			FMDB info, FileRecord file,
			AddressRecord address,
			DataOutputStream out){
		Message message = createSendMessage(sendName,info,file,address,out);
		message.sendMessage();
		session.setPreviousSendMessage(sendName);
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
			Session session, DataInputStream in,String messageNameGet) 
			throws NotSupposedToGetException {
		Message message = null;
		if (index < receiveNames.size()){
			message = createReceiveMessage(receiveNames.get(index),in,chance,messageNameGet);
			try{
				message.receiveMessage();
				session.setPreviousReceiveMessage(receiveNames.get(index));
				saveTempContent(message,info);
			}
			catch(NotSupposedToGetException e){
				receive(receiveNames,index+1,
						MessageNames.SECOND_CHANCE,info,session,in,
						message.getMessageNameGet());
			}		
		}
		else{
			throw new NotSupposedToGetException();
		}
	}

	/** Does the session according to the user's command
	 * (such as ADD,DIRALLFILES...)
	 * 
	 * @param command the user's command.
	 * @param info the FM' DB.
	 * @param session the current session.
	 * @param previousSend the previous send message's name.
	 * @param previousReceive the previous receive message's name.
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream. 
	 * @throws NotSupposedToGetException
	 */
	private void doAccordingToCommand(String command,FMDB info,
			Session session,String previousSend,
			String previousReceive,DataInputStream in, 
			DataOutputStream out) throws NotSupposedToGetException{
		while (!session.previousSendReceive(previousSend, previousReceive)){
			String sendName = session.FMMessageToSend(command,false);
			send(sendName,session,info,null,null,out);
			if (!receiveListOf(command,session,info,in)){
				ArrayList<String> receiveNames = session.FMMessageToReceive(command);
				receive(receiveNames, 0, MessageNames.FIRST_CHANCE, 
						info, session, in, MessageNames.EMPTY);
			}
		}
	}
	
	/** Receives a list of ,probably FILEADDRESS, NSCONTAINFILE
	 *  or CONTAINNAMESERVER.
	 * @param command the user's command.
	 * @param session the current session.
	 * @param info the FM' DB.
	 * @param in the DataInputStream.
	 * @return true if a list of was received else false.
	 * @throws NotSupposedToGetException
	 */
	private boolean receiveListOf(String command,
			Session session, FMDB info,
			DataInputStream in) throws NotSupposedToGetException {
		if ((session.getPreviousSendMessage().equals(MessageNames.WANTFILE)) ||
				(session.getPreviousSendMessage().equals(MessageNames.WANTSERVERS)) ||
				(session.getPreviousSendMessage().equals(MessageNames.WANTALLFILES))){
			receiveSequentially(command,session,info,in);
			return true;
		}
		return false;
	}
	
	/** Receives sequentially (without send between receiving) 
	 *  a list of,probably FILEADDRESS, NSCONTAINFILE
	 *  or CONTAINNAMESERVER.
	 * @param command the user's command.
	 * @param session the current session.
	 * @param info the FM' DB.
	 * @param in the DataInputStream.
	 * @throws NotSupposedToGetException
	 */
	private void receiveSequentially(String command,
			Session session,FMDB info,
			DataInputStream in) throws NotSupposedToGetException{
			ArrayList<String> receiveNames = session.FMMessageToReceive(command);
			receive(receiveNames, 0, MessageNames.FIRST_CHANCE, 
						info, session, in, MessageNames.EMPTY);
			while (!session.getPreviousReceiveMessage().
					equals(MessageNames.ENDLIST)){
				receiveNames = session.FMMessageToReceive(command);
				receive(receiveNames, 0, MessageNames.FIRST_CHANCE, 
						info, session, in, MessageNames.EMPTY);
			}	
	}

	/** The error case - this method is called when there is 
	 * no response after the default timeout was over or
	 * the FM received a message that it does not suppose to.
	 * @param out the DataOutputStream.
	 * @param in the DataInputStream.
	 * @param client the socket.
	 * @throws NotSupposedToGetException
	 */
	private void ErrorCase(DataOutputStream out,DataInputStream in,
							Socket client,FMDB info)
				throws NotSupposedToGetException {
		Message message = new BasicMessage(MessageNames.ENDSESSION,
				MessageNames.getMessageEnd(),out);
			if (!client.isClosed()){
				message.sendMessage();
				message = new BasicMessage(MessageNames.DONE,
						MessageNames.getMessageEnd(),in,MessageNames.FIRST_CHANCE);
				message.receiveMessage();
			}
			else
				System.out.println(INVALID_SESSION_TERMINATION);
			
			// It should clears them because the FM-NS session
			// terminated abnormally, so we doesn't need
			// to save the temp content, it will cause
			// that in the next FM-NS (with any NS) it will
			// "achieve" wrong system files or FM names.
			 info.clear(FMDB.SYSTEM_FILES_TEMP);
			 info.clear(FMDB.FM_NAMES_TEMP);
	}
	
	/** Creates the message To send.
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the message.
	 */
	private Message createSendMessage(String messageName,
			FMDB info,FileRecord file,
			AddressRecord address,DataOutputStream out){
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.BASICMESSAGE))
			message = new BasicMessage(messageName,
					MessageNames.getMessageEnd(),out);
		
		// The case of BEGIN,CONTAINNAMESERVER.
		if (messageType.equals(MessageNames.NETADDRESSMESSAGE))
			message = createNetAddressMessage(messageName,info,address,out);
		// The case of CONTAINFILE,DONTCONTAINFILE,WANTFILE.
		if (messageType.equals(MessageNames.FILENAMEMESSAGE))
			message = createFileNameMessage(messageName,info,file,out);
		// The case of FILE.
		if (messageType.equals(MessageNames.FILECONTENTSMESSAGE))
			message = createFileContentsMessage(messageName,info,out);
		return message;
	}

	/** Creates a NetAddress message (such as BEGIN,CONTAINNAMESERVER).
	 * 
	 * @param messageName
	 * @param info the FM' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the NetAddress message.
	 */
	private Message createNetAddressMessage(String messageName,
			FMDB info,AddressRecord address,DataOutputStream out){
		Message message = new NetAddressMessage(messageName,
				address.getIP(),address.getPort(),
				MessageNames.getMessageEnd(),out);
		return message;
	}
	
	/** Creates a FileName message (such as CONTAINFILE,DONTCONTAINFILE,WANTFILE).
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the FileName message.
	 */
	private Message createFileNameMessage(String messageName, FMDB info,
			FileRecord file, DataOutputStream out) {
		Message message = null;
		if (messageName.equals(MessageNames.CONTAINFILE)){
			if (file == null)
				// The case of CONTAINFILE not in the introduction.
				message = new FileNameMessage(messageName,
						info.getFileWanted(),
						MessageNames.getMessageEnd(),out);
			else
				// The case of CONTAINFILE in the introduction.
				message = new FileNameMessage(messageName,
						file.getFileName(),
						MessageNames.getMessageEnd(),out);
		}
		if (messageName.equals(MessageNames.DONTCONTAINFILE))
			message = new FileNameMessage(messageName,
					info.getLastFileNameDeleted(),
					MessageNames.getMessageEnd(),out);
		if (messageName.equals(MessageNames.WANTFILE))
			message = new FileNameMessage(messageName,
					info.getFileWanted(),
					MessageNames.getMessageEnd(),out);
		return message;
	}
	
	/** Creates a FileContents message (such as FILE).
	 * 
	 * @param messageName
	 * @param info the FM' DB.
	 * @param file the file to send.
	 * @param address the address to send.
	 * @param out the DataOutputStream.
	 * @return the FileContents message.
	 */
	private Message createFileContentsMessage(String messageName,
			FMDB info, DataOutputStream out) {
		return (new FileContentsMessage(messageName,
				info.getFileDirPath() + info.getFileWanted(),
				MessageNames.getMessageEnd(),out));
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
			
		 // The case of CONTAINNAMESERVER,FILEADDRESS
		 if (messageType.equals(MessageNames.NETADDRESSMESSAGE)){
			 if (chance.equals(MessageNames.FIRST_CHANCE))
				 message = new NetAddressMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance);
			 if (chance.equals(MessageNames.SECOND_CHANCE))
				 message = new NetAddressMessage(messageName,
						 MessageNames.getMessageEnd(),in,chance,messageNameGet);
		 }
		 
		// The case of NSCONTAINFILE.	
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
	 
	 /** Saves temporarily the data whic has been received during
	  * the FM-NS session.
	  * @param message the message which it gets the data.
	  * @param info the FM' DB.
	  */
	 private void saveTempContent(Message message, FMDB info) {
		 if (message.getMessageNameGet().equals(MessageNames.CONTAINNAMESERVER))
			 info.addNSOrFM( ((NetAddressMessage)(message)).getIP(),
					 ((NetAddressMessage)(message)).getPort(),
					 FMDB.NS_NAMES_TEMP);
		 if (message.getMessageNameGet().equals(MessageNames.FILEADDRESS))
			 info.addNSOrFM( ((NetAddressMessage)(message)).getIP(),
					 ((NetAddressMessage)(message)).getPort(),
					 FMDB.FM_NAMES_TEMP);
		 if (message.getMessageNameGet().equals(MessageNames.NSCONTAINFILE))
			 info.addFileName( ((FileNameMessage)(message)).getFileName(),
					 			FMDB.SYSTEM_FILES_TEMP);
	 }
	 
	 /** Saves constantly the data which has been received during
	  *  the FM-NS session.
	  * @param info the FM' DB.
	  */
	 private void saveContent(FMDB info) {
		 if (info.getNSOrFMSize(FMDB.FM_NAMES_TEMP) > 0 ){
			 info.copyAddresses(FMDB.FM_NAMES_TEMP,FMDB.FM_NAMES);
			 info.clear(FMDB.FM_NAMES_TEMP);
		 }
		 if (info.getFilesNamesSize(FMDB.SYSTEM_FILES_TEMP) > 0 ){
			 info.copySystemFiles();
			 info.clear(FMDB.SYSTEM_FILES_TEMP);
		 }
	 }
}