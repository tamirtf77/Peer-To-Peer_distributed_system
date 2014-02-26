//###############
//FILE : NetAddressMessage.java
//WRITER : tamirtf77
//DESCRIPTION: A class of NetAddress message. 
// It creates send & receive NetAddress messages such as 
// BEGIN,FILEADDRESS,CONTAINNAMESERVER.
//###############
package oop.ex3.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class NetAddressMessage extends Message{

	/** The address's IP. */
	private String _IP;
	
	/** The address's port. */
	private int _port;
	
	/** Constructs a NetAddress message to send.
	 * 
	 * @param messageName the message's name (such as BEGIN).
	 * @param IP the address's IP to send.
	 * @param port the address's port to send.
	 * @param messageEnd the message' end (END).
	 * @param out the DataOutputStream.
	 */
	public NetAddressMessage(String messageName, String IP,
			int port, String messageEnd, DataOutputStream out) {
		super(messageName, messageEnd, out);
		_IP = IP;
		_port = port;
	}
	
	/** Constructs a NetAddress message 
	 * 	 To receive in the first chance.
	 * 
	 * @param messageName the message's name (such as FILEADDRESS).
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be FIRST_CHANCE).
	 * @throws NotSupposedToGetException
	 */
	public NetAddressMessage(String messageName,
			String messageEnd,DataInputStream in,String chance)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance);
	}
	
	/** Constructs a NetAddress message To receive
	 *  not in the first chance.
	 * 
	 * @param messageName the message's name (such as CONTAINFILE).
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be SECOND_CHANCE).
	 * @param messageNameGet the message's name which was get
	 * previously, in the first chance to receive the message.
	 * @throws NotSupposedToGetException
	 */
	public NetAddressMessage(String messageName,
			String messageEnd,DataInputStream in,String chance,
			String messageNameGet)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance,messageNameGet);
	}
	
	/** Gets the address's IP.
	 * 
	 * @return the address's IP.
	 */
	public String getIP(){
		return _IP;
	}
	
	/** Gets the address's port.
	 * 
	 * @return the address's port.
	 */
	public int getPort(){
		return _port;
	}

	@Override
	public void sendMessage() {
		sendUTF(getMessageNameSuppose(),getDataOutputStream());
		sendUTF(_IP,getDataOutputStream());
		sendInt(_port,getDataOutputStream());
		sendUTF(getMessageEnd(),getDataOutputStream());
	}
	
	/** Sends an int.
	 * 
	 * @param word the int to send.
	 * @param out the DataOutputStream.
	 */
	 protected void sendInt(int word,DataOutputStream out) {
		 try {
			 out.writeInt(word);
		 } 
		 catch (IOException e) {
			//We assume it won't happen.
		 }
	 }
	 
	 /** Receives an int.
	  * 
	  * @param in the DataInputStream
	  * @return an int.
	  */
	 protected int getInt(DataInputStream in) {
		 try {
			 return in.readInt();
		 } 
		 catch (IOException e) {
			 return -1;
		 }
	 }

	@Override
	public void receiveMessage() throws NotSupposedToGetException {
		super.receiveMessage();
		_IP = receiveUTF(getDataInputStream());
		_port = getInt(getDataInputStream());
		String messageEnd = receiveUTF(getDataInputStream());
		checkMessageWord(getMessageEnd(),messageEnd);
	}
}