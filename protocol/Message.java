//###############
//FILE : Message.java
//WRITER : tamirtf77
//DESCRIPTION: An abstract class of an message. 
// It is a super class to all the messages. It holds common
// members and methods to all the messages.
//###############
package oop.ex3.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public abstract class Message {
		 
	 /** The first word to suppose to receive of a message. */
	 protected String _messageNameSuppose;
	 
	 /** The first word that actually received. */
	 protected String _messageNameGet = MessageNames.EMPTY;
	 
	 /** The last word of a message. */
	 protected String _messageEnd;
	 
	 /** The DataInputStream. */
	 protected DataOutputStream _out;
	 
	 /** The DataOutputStream. */
	 protected DataInputStream _in;
	 
	 /** The chance to get the message. */
	 protected String _chance;
	 
	 /** Constructs a message to send (each specific message
	  * call it in its constructor).
	  * 
	  * @param messageName the message's name.
	  * @param messageEnd the message' end (END).
	  * @param out the DataOutputStream.
	  */
	 public Message(String messageName,String messageEnd,
			 DataOutputStream out){
		 _messageNameSuppose = messageName;
		 _messageEnd = messageEnd;
		 _out = out;
	 }
	 
	 /** Constructs a message to receive (each specific message
	  * call it in its constructor) in the first chance.
	  * 
	  * @param messageNameSuppose the message's name
	  * 						 suppose to receive.
	  * @param messageEnd the message's end (END).
	  * @param in the DataInputStream.
	  * @param chance the chance to get (Should be FIRST_CHANCE).
	  * @throws NotSupposedToGetException
	  */
	 public Message(String messageNameSuppose,String messageEnd,
			 DataInputStream in,String chance) throws NotSupposedToGetException{
		 _messageNameSuppose = messageNameSuppose;
		 _messageEnd = messageEnd;
		 _in = in;
		 _chance = chance;
	 }
	 
	 /** Constructs a message to receive (each specific message
	  * call it in its constructor) not in the first chance.
	  * 
	  * @param messageNameSuppose the message's name
	  * 						 suppose to receive.
	  * @param messageEnd the message's end (END).
	  * @param in the DataInputStream.
	  * @param chance the chance to get (Should be SECOND_CHANCE).
	  * @param messageNameGet the message's name which was get
	  *  previously, in the first chance to receive the message.
	  * @throws NotSupposedToGetException
	  */
	 public Message(String messageNameSuppose,String messageEnd,
			 DataInputStream in,String chance,String messageNameGet)
	 		throws NotSupposedToGetException{
		 _messageNameSuppose = messageNameSuppose;
		 _messageEnd = messageEnd;
		 _in = in;
		 _chance = chance;
		 _messageNameGet = messageNameGet;
	 }

	 /** Gets the message's name that suppose to receive.
	  * 
	  * @return the message's name that suppose to receive.
	  */
	public String getMessageNameSuppose(){
		 return _messageNameSuppose;
	 }
	
	/** Gets the message's name which is actually receives.
	 * 
	 * @return message's name which is actually receives.
	 */
	public String getMessageNameGet(){
		 return _messageNameGet;
	 }
	
	/** Sets the message's name which is actually receives.
	 * 
	 * @param messageNameGet the message's name which is actually receives.
	 */
	public void setMessageNameGet(String messageNameGet){
		_messageNameGet = messageNameGet;
	}
	 
	/** Gets the message's end.
	 * 
	 * @return the message's end.
	 */
	public String getMessageEnd(){	
		return _messageEnd;
	}
	 
	/** Gets the DataOutputStream.
	 * 
	 * @return the DataOutputStream.
	 */
	protected DataOutputStream getDataOutputStream(){	
		return _out;
	}
	 
	/** Gets the DataInputStream.
	 * 
	 * @return the DataInputStream.
	 */
	protected DataInputStream getDataInputStream(){
		return _in;
	}
	 
	/** Gets the chance to receive the message.
	 * 
	 * @return the chance to receive the message.
	 */
	protected String getChance(){
		return _chance;
	}
	
	/** Checks whether the message's name acutually receive
	 *  equals to the message's name supposed to receive.
	 * @param myMessageName the message's name acutually receive
	 * @param messageName the message's name supposed to receive.
	 * @throws NotSupposedToGetException
	 */
	protected void checkMessageWord(String myMessageName,
										String messageName) 
							throws NotSupposedToGetException {
		if (!myMessageName.equals(messageName))
			throw new NotSupposedToGetException();
	}
	 
	/** Sends the message.
	 * 
	 */
	public abstract void sendMessage();
	
	/** Receives the message.
	 * 
	 * @throws NotSupposedToGetException
	 */
	public void receiveMessage() 
	   throws NotSupposedToGetException{
		// If it's the first chance to receive than
		// it should read the message's name and saves it.
		// If the message's name receive isn't equal
		// to the message's name suppose to receive, than
		// we should uses the message's name receive
		// when we create other messages to receive 
		// (i.e. there can several options to the
		// message's name suppose to receive).
		if (getChance().equals(MessageNames.FIRST_CHANCE)){
			String messageNameGet = receiveUTF(getDataInputStream());
			setMessageNameGet(messageNameGet);
		}
		checkMessageWord(getMessageNameSuppose(),getMessageNameGet());
	}
	 
	/** Sends a string.
	 * 
	 * @param word the string to send.
	 * @param out the DataOutputStream
	 */
	protected void sendUTF(String word ,
			DataOutputStream out){
		try {
			out.writeUTF(word);
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
	}
	
	/** Receives a string.
	 * 
	 * @param in the DataInputStream.
	 * @return a string.
	 */
	protected String receiveUTF(DataInputStream in)  {
		try {
			return in.readUTF();
		} 
		catch (IOException e) {
			return MessageNames.EMPTY;
		}
	}
}