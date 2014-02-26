//###############
//FILE : FileNameMessage.java
//WRITER : tamirtf77
//DESCRIPTION: A class of FileName message. 
// It creates send & receive FileName messages such as 
// WANTFILE,CONTAINFILE,DONTCONTAINFILE,NSCONTAINFILE.
//###############
package oop.ex3.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class FileNameMessage extends Message {

	/** The file's name to send or receive. */
	private String _fileName;
	
	/** Constructs a FileName message to send.
	 * 
	 * @param messageName the message's name (such as WANTFILE).
	 * @param fileName the file's name to send.
	 * @param messageEnd the message's end (END).
	 * @param out the DataOutputStream.
	 */
	public FileNameMessage(String messageName, String fileName,
			String messageEnd, DataOutputStream out){
		super(messageName, messageEnd, out);
		_fileName = fileName;
	}
	
	/** Constructs a FileName message 
	 * 	 To receive in the first chance.
	 * 
	 * @param messageName the message's name (such as CONTAINFILE).
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be FIRST_CHANCE).
	 * @throws NotSupposedToGetException
	 */
	public FileNameMessage(String messageName,
			String messageEnd,DataInputStream in,String chance)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance);
	}
	
	/** Constructs a FileName message To receive
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
	public FileNameMessage(String messageName,
			String messageEnd,DataInputStream in,String chance,
			String messageNameGet)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance,messageNameGet);
	}
	
	/** Gets the file's name.
	 * 
	 * @return the file's name.
	 */
	public String getFileName(){
		return _fileName;
	}

	@Override
	public void sendMessage() {
		sendUTF(getMessageNameSuppose(),getDataOutputStream());
		sendUTF(_fileName,getDataOutputStream());
		sendUTF(getMessageEnd(),getDataOutputStream());
	}

	@Override
	public void receiveMessage() 
						throws NotSupposedToGetException {
		super.receiveMessage();
		_fileName = receiveUTF(getDataInputStream());
		String messageEnd = receiveUTF(getDataInputStream());
		checkMessageWord(getMessageEnd(),messageEnd);
	}

}