//###############
//FILE : BasicMessage.java
//WRITER : tamirtf77
//DESCRIPTION: A class of Basic message. It creates send & receive
// Basic messages such as ERROR,DONE,WELCOME,GOAWAY,GOODBYE,
// FILENOTFOUND,WANTSERVERS,ENDLIST,ENDSESSION,WANTALLFILES.
//###############
package oop.ex3.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;


public class BasicMessage extends Message {
	
	/** Constructs a Basic message to send.
	 * 
	 * @param messageName the message's name (Such as DONE).
	 * @param messageEnd the message's end (END).
	 * @param out the DataOutputStream.
	 */
	public BasicMessage(String messageName,String messageEnd,
			DataOutputStream out){
		super(messageName,messageEnd,out);
	}
	
	/** Constructs a Basic message To receive in the first chance.
	 * 
	 * @param messageName the message's name (Such as DONE).
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be FIRST_CHANCE).
	 * @throws NotSupposedToGetException
	 */
	public BasicMessage(String messageName,
			String messageEnd,DataInputStream in,String chance)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance);
	}
	
	/** Constructs a Basic message To receive not in the first chance.
	 * 
	 * @param messageName the message's name (Such as DONE).
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be SECOND_CHANCE).
	 * @param messageNameGet the message's name which was get
	 * previously, in the first chance to receive the message.
	 * @throws NotSupposedToGetException
	 */
	public BasicMessage(String messageName,
			String messageEnd,DataInputStream in,String chance,
			String messageNameGet)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance,messageNameGet);
	}
	
	@Override
	public void sendMessage(){
		sendUTF(getMessageNameSuppose(),getDataOutputStream());
		sendUTF(getMessageEnd(),getDataOutputStream());
	}

	@Override
	public void receiveMessage() 
		throws NotSupposedToGetException {
		super.receiveMessage();
		String messageEnd = receiveUTF(getDataInputStream());
		checkMessageWord(getMessageEnd(),messageEnd);
	}
}