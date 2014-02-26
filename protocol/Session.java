//###############
//FILE : Session.java
//WRITER : tamirtf77
//DESCRIPTION: A class of session - it decides which message
// to send & receive according to the previous send & receive messages
// for the both NSs and FMs. It also decided for the FM-FM
// session (the Upload session).
//###############
package oop.ex3.protocol;

import java.util.ArrayList;

import oop.ex3.filemanager.MyFileManager;

public class Session {
	
	/** The previous receive message. */
	private String _previousReceiveMessage = MessageNames.EMPTY;
	
	/** The previous send message. */
	private String _previousSendMessage = MessageNames.EMPTY;
	
	/** An indicator if the introduction in FM-NS session
	 * is finished.
	 */
	private boolean _FMIntroductionOver = false;
	
	/** An indicator if was the CONTAINNAMESERVER 
	 * in the introduction in FM-NS session - it
	 * helps for sending  CONTAINNAMESERVER, not in
	 * the introduction, while FM sends WANTSERVERS to
	 * NS.
	 */
	private boolean _wasContainNameServer = false;
	
	/** Gets the previous receive message.
	 * 
	 * @return the previous receive message.
	 */
	public String getPreviousReceiveMessage(){
		return _previousReceiveMessage;
	}
	
	/** Sets the previous receive message.
	 * 
	 * @param message the previous receive message.
	 */
	public void setPreviousReceiveMessage(String message){
		_previousReceiveMessage = message;
	}
	
	/** Gets the previous send message.
	 * 
	 * @return the previous send message.
	 */
	public String getPreviousSendMessage(){
		return _previousSendMessage;
	}
	
	/** Sets the previous send message.
	 * 
	 * @param message the previous send message.
	 */
	public void setPreviousSendMessage(String message){
		_previousSendMessage = message;
	}
	
/*%%%%%%%%%%% The part of a FM in the session. %%%%%%%%%%%%%%%%%%%%%%*/
	
	/** Decides which message to send by the FM.
	 * @param command the user's command.
	 * @param has indicator if the FM has more files/servers
	 *   to send to a NS in the introduction.
	 * @return the message's name to send.
	 */
	public String FMMessageToSend(String command,boolean has) {
		String messageName = MessageNames.EMPTY;
		if (!getFMIntroductionOver())
			messageName = FMIntroduceSendDownloadSession(command,has);
		if (messageName.equals(MessageNames.EMPTY))
				messageName = FMSendMessageAccordingToCommand(command);
		return messageName;
	}
	
	/** Decides which message to send by the FM in the 
	 * introduction.
	 * 
	 * @param command the user's command.
	 * @param has indicator if the FM has more files/servers
	 *   to send to a NS in the introduction.
	 * @return the message's name to send.
	 */
	private String FMIntroduceSendDownloadSession(String command,boolean has) {
		if (previousSendReceive(MessageNames.EMPTY,MessageNames.EMPTY))
			return MessageNames.BEGIN;
		if (previousSendReceive(MessageNames.BEGIN,MessageNames.DONE))
			if (command.equals(MessageNames.EMPTY))
				return MessageNames.ENDSESSION;
		if (previousSendReceive(MessageNames.BEGIN,MessageNames.WELCOME)){
			if (has)
				return MessageNames.CONTAINFILE;
			else
				return MessageNames.ENDLIST;
		}
		if (previousSendReceive(MessageNames.CONTAINFILE,MessageNames.DONE)){
			if (has)
				return MessageNames.CONTAINFILE;
			else
				return MessageNames.ENDLIST;
		}
		if (previousSendReceive(MessageNames.ENDLIST,MessageNames.DONE))
			if (has)
				// Each FM has at least one NS in his list, otherwise
				// it can't speak with any NS.
				return MessageNames.CONTAINNAMESERVER;
		if (previousSendReceive(MessageNames.CONTAINNAMESERVER,MessageNames.DONE)){
			if (has)
				return MessageNames.CONTAINNAMESERVER;
			else
				return MessageNames.ENDLIST;
		}
		// It goes to here when the whole information has been sent
		// to the NS and there is no user's command (it accures
		// after FM is initialize and it sends its information
		// to all the NS it knows).
		if (previousSendReceive(MessageNames.ENDLIST,MessageNames.DONE)){
			if (command.equals(MessageNames.EMPTY))
				return MessageNames.ENDSESSION;
		}
		return MessageNames.EMPTY;
	}
	
	/** Sets that the introduction FM-NS is over. */
	private void setFMIntroductionOver(){
		_FMIntroductionOver = true;
	}
	
	/** Gets the indicator if the introduction in FM-NS session
	 * is over.
	 * 
	 * @return the indicator if the introduction in FM-NS session
	 *         is over.
	 */
	private boolean getFMIntroductionOver(){
		return _FMIntroductionOver;
	}
	
	/** Decides which message to send by the FM according to
	 * the user's command.
	 * 
	 * @param command the user's command.
	 * @return the message's name to send.
	 */
	private String FMSendMessageAccordingToCommand(String command) {
		setFMIntroductionOver();
		if (command.equals(MyFileManager.ADD)){
			if (previousFMSendDownloadSession())
				return MessageNames.WANTFILE;
			//FILEADRESS with finally ENDLIST  or FILENOTFOUND
			if ((previousSendReceive(MessageNames.WANTFILE,
					MessageNames.ENDLIST)) ||
					(previousSendReceive(MessageNames.WANTFILE,
							MessageNames.FILENOTFOUND)))
				return MessageNames.ENDSESSION;
		}
		if (command.equals(MyFileManager.ADDAGAIN)){
			if (previousFMSendDownloadSession())
				return MessageNames.WANTSERVERS;
			
			//CONTAINNAMESERVER with finally ENDLIST
			if (previousSendReceive(MessageNames.WANTSERVERS,
									MessageNames.ENDLIST))
				return MessageNames.ENDSESSION;
		}
		if(command.equals(MyFileManager.ADDFINAL)){
			if (previousFMSendDownloadSession()){
				return MessageNames.CONTAINFILE;
			}
			if (previousSendReceive(MessageNames.CONTAINFILE,
					MessageNames.DONE)){
				return MessageNames.ENDSESSION;
			}
		}
		if (command.equals(MyFileManager.REMOVE)){
			if (previousFMSendDownloadSession())
				return MessageNames.DONTCONTAINFILE;
			if (previousSendReceive(MessageNames.DONTCONTAINFILE,
					MessageNames.DONE))
				return MessageNames.ENDSESSION;
		}
		if (command.equals(MyFileManager.DIRALLFILES)){
			if (previousFMSendDownloadSession())
				return MessageNames.WANTALLFILES;
			//NSCONTAINFILE with finally ENDLIST
			if (previousSendReceive(MessageNames.WANTALLFILES,
					MessageNames.ENDLIST))
				return MessageNames.ENDSESSION;
		}
		if (command.equals(MyFileManager.FIRESERVERS)){
			if (previousFMSendDownloadSession())
				return MessageNames.GOAWAY;
			if (previousSendReceive(MessageNames.GOAWAY,
					MessageNames.DONE))
				return MessageNames.ENDSESSION;
		}
		if (command.equals(MyFileManager.QUIT)){
			if (previousFMSendDownloadSession())
				return MessageNames.GOODBYE;
			if (previousSendReceive(MessageNames.GOODBYE,
					MessageNames.DONE))
				return MessageNames.ENDSESSION;
		}
		// case of ERROR.
		return MessageNames.ENDSESSION;
	}
	
	/** Checks whether the previous Send/Receive was the final
	 * of the the beginning of each session between FM to NS.
	 * @return true if so else false.
	 */
	private boolean previousFMSendDownloadSession(){
		if ((previousSendReceive(MessageNames.BEGIN,
									MessageNames.DONE)) ||
				(previousSendReceive(MessageNames.ENDLIST,
									  MessageNames.DONE)))
			return true;
		return false;
	}
	
	/** Checks if the previous send & receive messages' names
	 * are equal to the parameters send and receive's names.
	 * @param send the send message's name.
	 * @param receive the receive message's name.
	 * @return true if they are both equal else false.
	 */
	public boolean previousSendReceive(String send,String receive){
		if ((_previousSendMessage.equals(send)) && 
				(_previousReceiveMessage.equals(receive)))
			return true;
		return false;
	}
	
	/** Decides which message to send by the FM who
	 * requests a file from other FM.
	 * 
	 * @return the message's name to send.
	 */
	public String FMRequestSendUploadSession() {
		if (previousSendReceive(MessageNames.EMPTY,MessageNames.EMPTY))
			return MessageNames.WANTFILE;
		return MessageNames.ERROR;
	}
	
	/** Decides which message to send by the FM who
	 * supplies a file.
	 * 
	 * @param has indicator if the FM has the file.
	 * @return the message's name to send.
	 */
	public String FMSupplySendUploadSession(boolean has) {
		if (previousSendReceive(MessageNames.EMPTY,MessageNames.WANTFILE)){
			if (has)
				return MessageNames.FILE;
			else
				return MessageNames.FILENOTFOUND;
		}
		return MessageNames.ERROR;
	}
	
	/** Decides which message to receive by the FM who
	 * requests a file from other FM.
	 * 
	 * @return the messages' names (options) to receive.
	 */
	public ArrayList<String> FMRequestReceiveUploadSession(){
		ArrayList<String> receive = new ArrayList<String>();
		if (previousReceiveSend(MessageNames.EMPTY,MessageNames.WANTFILE)){
			receive.add(MessageNames.FILE);
			receive.add(MessageNames.FILENOTFOUND);
		}
		return receive;
	}
	
	/** Decides which message to receive by the FM who
	 * supplies a file.
	 * 
	 * @return the message's name to receive.
	 */
	public ArrayList<String> FMSupplyReceiveUploadSession(){
		ArrayList<String> receive = new ArrayList<String>();
		if (previousReceiveSend(MessageNames.EMPTY,MessageNames.EMPTY)){
			receive.add(MessageNames.WANTFILE);
		}
		return receive;
	}
	
	
	/** Decides which message to receive by the FM in the FM-NS session
	 * according to the user command.
	 * 
	 * @param command the user command.
	 * @return the messages' names (options) to receive.
	 */
	public ArrayList<String> FMMessageToReceive(String command){
		ArrayList<String> messageNames = new ArrayList<String>();
		FMIntroduceReceiveDownloadSession(messageNames);
		if (messageNames.size() == 0)
			FMReceiveMessageAccordingToCommand(command,messageNames);
		return messageNames;
	}
	
	/** Decides which message to receive by the FM in the FM-NS introduction.
	 * 
	 * @param receive the receive's array of options to receive.
	 */
	private void FMIntroduceReceiveDownloadSession(ArrayList<String> receive) {
		if (previousReceiveSend(MessageNames.EMPTY,MessageNames.BEGIN)){
			receive.add(MessageNames.WELCOME);
			receive.add(MessageNames.DONE);
		}
		if ((previousReceiveSend(MessageNames.WELCOME,
							MessageNames.CONTAINFILE)) ||
			(previousReceiveSend(MessageNames.WELCOME,
									MessageNames.ENDLIST))){
			receive.add(MessageNames.DONE);
		}
		if (_previousReceiveMessage.equals(MessageNames.DONE))
			if ((_previousSendMessage.equals(MessageNames.CONTAINFILE)) ||
					(_previousSendMessage.equals(MessageNames.ENDLIST)) ||
					(_previousSendMessage.equals(MessageNames.CONTAINNAMESERVER)) ||
					(_previousSendMessage.equals(MessageNames.ENDSESSION))){
				receive.add(MessageNames.DONE);		
			}
	}
	
	/** Decides which message to receive by the FM in the FM-NS
	 *  according to the user's command.
	 * 
	 * @param command the user's command.
	 * @param receive the receive's array of options to receive.
	 */
	private void FMReceiveMessageAccordingToCommand(String command,
			ArrayList<String> receive) {
		if (command.equals(MyFileManager.ADD)){
			if (previousReceiveSend(MessageNames.DONE,
					MessageNames.WANTFILE)){
				receive.add(MessageNames.FILEADDRESS);
				receive.add(MessageNames.FILENOTFOUND);
			}
			if (previousReceiveSend(MessageNames.FILEADDRESS,
					MessageNames.WANTFILE)){
				receive.add(MessageNames.FILEADDRESS);
				receive.add(MessageNames.ENDLIST);
			}
			if ((previousReceiveSend(MessageNames.ENDLIST,
					MessageNames.ENDSESSION)) || 
					(previousReceiveSend(MessageNames.FILENOTFOUND,
							MessageNames.ENDSESSION))){
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.ADDAGAIN)){
			if (previousReceiveSend(MessageNames.DONE,
					MessageNames.WANTSERVERS)){
				receive.add(MessageNames.CONTAINNAMESERVER);
			}
			if (previousReceiveSend(MessageNames.CONTAINNAMESERVER,
					MessageNames.WANTSERVERS)){
				receive.add(MessageNames.CONTAINNAMESERVER);
				receive.add(MessageNames.ENDLIST);
			}
			if (previousReceiveSend(MessageNames.ENDLIST,
					MessageNames.ENDSESSION)){ 
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.ADDFINAL)){
			if ((previousReceiveSend(MessageNames.DONE,
					MessageNames.CONTAINFILE)) ||
					(previousReceiveSend(MessageNames.DONE,
							MessageNames.ENDSESSION))){
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.REMOVE)){
			if ((previousReceiveSend(MessageNames.DONE,
					MessageNames.DONTCONTAINFILE)) ||
					(previousReceiveSend(MessageNames.DONE,
							MessageNames.ENDSESSION))){
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.DIRALLFILES)){
			if (previousReceiveSend(MessageNames.DONE,
					MessageNames.WANTALLFILES)){ 
				receive.add(MessageNames.NSCONTAINFILE);
				receive.add(MessageNames.ENDLIST);
			}
			if (previousReceiveSend(MessageNames.NSCONTAINFILE,
					MessageNames.WANTALLFILES)){ 
				receive.add(MessageNames.NSCONTAINFILE);
				receive.add(MessageNames.ENDLIST);
			}
			if (previousReceiveSend(MessageNames.ENDLIST,
							MessageNames.ENDSESSION)){  
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.FIRESERVERS)){
			if ((previousReceiveSend(MessageNames.DONE,
					MessageNames.GOAWAY)) ||
					(previousReceiveSend(MessageNames.DONE,
							MessageNames.ENDSESSION))){  
				receive.add(MessageNames.DONE);
			}
		}
		if (command.equals(MyFileManager.QUIT)){
			if ((previousReceiveSend(MessageNames.DONE,
					MessageNames.GOODBYE)) ||
					(previousReceiveSend(MessageNames.DONE,
							MessageNames.ENDSESSION))){  
				receive.add(MessageNames.DONE);
			}
		}
	}
	
	/** Checks if the previous receive & send messages' names
	 * are equal to the parameters receive and send's names.
	 * @param receive the receive message's name.
	 * @param send the send message's name.
	 * @return true if they are both equal else false.
	 */
	public boolean previousReceiveSend(String receive,String send){
		if ((_previousReceiveMessage.equals(receive)) && 
				(_previousSendMessage.equals(send)))
			return true;
		return false;
	}
	
/*%%%%%%%%%%% The part of a NS in the session. %%%%%%%%%%%%%%%%%%%%%%*/
	
	/** Decides which message to send by the NS.
	 * @param metFM indicator if the NS has already met this FM.
	 * @param has indicator if the NS has more FILEADDRESS,
	 * NSCONTAINFILE or CONTAINAMESERVER to send to a FM
     * if it need to do so.
	 * @return the message's name to send.
	 */
	public String NSMessageToSend(boolean metFM,boolean has){ 
		String messageName = MessageNames.EMPTY;
			messageName = NSIntroduceSendDownloadSession(metFM);
			if (messageName.equals(MessageNames.EMPTY))
				messageName = NSSendMessageAfterIntroduction(has);
		return messageName;
	}
	
	/** Decides which message to send by the NS in the 
	 * introduction.
	 *  
	 * @param metFM indicator if the NS has already met this FM.
	 * @return the message's name to send.
	 */
	private String NSIntroduceSendDownloadSession(boolean metFM) {
		if (metFM){
			if (previousSendReceive(MessageNames.EMPTY,MessageNames.BEGIN))
				return MessageNames.DONE;
		}
		else{
			if (previousSendReceive(MessageNames.EMPTY,MessageNames.BEGIN))
				return MessageNames.WELCOME;
			if ((_previousSendMessage.equals(MessageNames.WELCOME)) ||
					(_previousSendMessage.equals(MessageNames.DONE))){
				if ((_previousReceiveMessage.equals(MessageNames.CONTAINFILE)) ||
						(_previousReceiveMessage.equals(MessageNames.ENDLIST)) ||
						(_previousReceiveMessage.equals(MessageNames.CONTAINNAMESERVER)))
					return MessageNames.DONE;
			}
			if (previousSendReceive(MessageNames.DONE,
									MessageNames.ENDSESSION)){
					return MessageNames.DONE;
			}
		}
		return MessageNames.EMPTY;
	}

	/** Decides which message to send by the NS in the FM-NS
	 *  after the introduction according to the previous
	 *  receive and sends messages and the indicator.
	 * @param has indicator if the NS has more FILEADDRESS,
	 * NSCONTAINFILE or CONTAINAMESERVER to send to a FM
     * if it need to do so.
	 * @return the message's name to send.
	 */
	private String NSSendMessageAfterIntroduction(boolean has) {
		if (previousSendReceive(MessageNames.DONE,
								MessageNames.WANTFILE)){
			if (has)
				return MessageNames.FILEADDRESS;
			else
				return MessageNames.FILENOTFOUND;
		}
		
		if (previousSendReceive(MessageNames.FILEADDRESS,
				MessageNames.WANTFILE)){
			if (has)
				return MessageNames.FILEADDRESS;
			else
				return MessageNames.ENDLIST;
		}
		
		if ((previousSendReceive(MessageNames.ENDLIST,
				MessageNames.ENDSESSION)) ||
				(previousSendReceive(MessageNames.FILENOTFOUND,
						MessageNames.ENDSESSION)))
			return MessageNames.DONE;
		
		if (previousSendReceive(MessageNames.DONE,
				MessageNames.WANTSERVERS))
			return MessageNames.CONTAINNAMESERVER;
		
		if (previousSendReceive(MessageNames.CONTAINNAMESERVER,
				MessageNames.WANTSERVERS)){
			if (has)
				return MessageNames.CONTAINNAMESERVER;
			else
				return MessageNames.ENDLIST;
		}
		
		if ((previousSendReceive(MessageNames.DONE,
				MessageNames.WANTALLFILES)) ||
				(previousSendReceive(MessageNames.NSCONTAINFILE,
						MessageNames.WANTALLFILES))){
			if (has)
				return MessageNames.NSCONTAINFILE;
			else
				return MessageNames.ENDLIST;
		}
		if (NSTypicalSession(MessageNames.CONTAINFILE))
			return MessageNames.DONE;
		if (NSTypicalSession(MessageNames.DONTCONTAINFILE))
			return MessageNames.DONE;
		if (NSTypicalSession(MessageNames.GOAWAY))
			return MessageNames.DONE;
		if (NSTypicalSession(MessageNames.GOODBYE))
			return MessageNames.DONE;
		
		// I get a wrong receive message.
		return MessageNames.ERROR;
	}
	
	/** Checks whether the previous Send/Receive was the final
	 * of the the beginning of each session between FM to NS.
	 * @return true if it is else false.
	 */
	private boolean NSTypicalSession(String message){
		if ((previousSendReceive(MessageNames.DONE,message)) || 
				(previousSendReceive(MessageNames.DONE,
									 MessageNames.ENDSESSION)))
				return true;
		return false;
	}
	
	/** Decides which message to receive by the NS in the FM-NS session.
	 * 
	 * @param metFM indicator if the NS has already met this FM.
	 * @return the messages' names (options) to receive.
	 */
	public ArrayList<String> NSMessageToReceive(boolean metFM){
		ArrayList<String> messageNames = new ArrayList<String>();
		NSIntroduceReceiveDownloadSession(metFM,messageNames);
		if (messageNames.size() == 0)
			NSReceiveMessageAfterIntroduction(messageNames);
		return messageNames;
	}
	
	/** Decides which message to receive by the NS in the FM-NS 
	 * 	 introduction.
	 * 
	 * @param metFM indicator if the NS has already met this FM.
	 * @param receive the messages' names (options) to receive
	 */
	private void NSIntroduceReceiveDownloadSession(boolean metFM,
							ArrayList<String> receive) {
		if (previousReceiveSend(MessageNames.EMPTY,MessageNames.EMPTY)){
			receive.add(MessageNames.BEGIN);
		}
		if (!metFM){
			if ((previousReceiveSend(MessageNames.BEGIN,
									 MessageNames.WELCOME)) ||
					(previousReceiveSend(MessageNames.CONTAINFILE,
							MessageNames.DONE))){
				receive.add(MessageNames.CONTAINFILE);
				receive.add(MessageNames.ENDLIST);
			}
			if ((previousReceiveSend(MessageNames.ENDLIST,
									MessageNames.DONE)) &&
									(!_wasContainNameServer)){
				receive.add(MessageNames.CONTAINNAMESERVER);	
			}
			if (previousReceiveSend(MessageNames.CONTAINNAMESERVER,
									MessageNames.DONE)){
				receive.add(MessageNames.CONTAINNAMESERVER);
				receive.add(MessageNames.ENDLIST);		
			}
		}
	}
	
	/** Checks whether the previous receive/send was the final
	 * of the the beginning of each session between FM to NS.
	 * @return true if so else false.
	 */
	private boolean previousNSReceiveDownloadSession(){
		if ((previousReceiveSend(MessageNames.BEGIN,
									MessageNames.DONE)) ||
				(previousReceiveSend(MessageNames.ENDLIST,
									  MessageNames.DONE)))
			return true;
		return false;
	}
	
	public void wasContainNameServer() {
		_wasContainNameServer = true;
	}
	
	/** Decides which message to receive by the NS in the FM-NS
	 *  according to the previous receive and send messages.
	 * 
	 * @param receive the receive's array of options to receive.
	 */
	private void NSReceiveMessageAfterIntroduction(ArrayList<String> receive) {
		if (previousNSReceiveDownloadSession()){
			receive.add(MessageNames.WANTFILE);
			receive.add(MessageNames.WANTSERVERS);
			receive.add(MessageNames.CONTAINFILE);
			receive.add(MessageNames.DONTCONTAINFILE);
			receive.add(MessageNames.WANTALLFILES);
			receive.add(MessageNames.GOAWAY);
			receive.add(MessageNames.GOODBYE);
			receive.add(MessageNames.ENDSESSION);
		}
		if ((previousReceiveSend(MessageNames.WANTFILE,
								 MessageNames.FILENOTFOUND)) ||
					(previousReceiveSend(MessageNames.WANTFILE,
							MessageNames.ENDLIST))){
			receive.add(MessageNames.ENDSESSION);
		}
		if ((previousReceiveSend(MessageNames.WANTSERVERS,
				MessageNames.ENDLIST)) || 
				(previousReceiveSend(MessageNames.WANTALLFILES,
						MessageNames.ENDLIST))){
			receive.add(MessageNames.ENDSESSION);
		}
		if ((previousReceiveSend(MessageNames.CONTAINFILE,
				MessageNames.DONE)) ||
		    (previousReceiveSend(MessageNames.DONTCONTAINFILE,
				MessageNames.DONE)) || 
			(previousReceiveSend(MessageNames.GOAWAY,
				MessageNames.DONE)) || 
			(previousReceiveSend(MessageNames.GOODBYE,
				MessageNames.DONE))) {
			receive.add(MessageNames.ENDSESSION);
		}
	}
}