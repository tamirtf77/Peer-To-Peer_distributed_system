//###############
//FILE : FileContentsMessage.java
//WRITER : tamirtf77
//DESCRIPTION: A class of FileContents message. 
// It creates send & receive FileContents messages - the FILE.
//###############
package oop.ex3.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileContentsMessage extends Message {

	/** The file's name to send or receive. */
	private String _fileName;
	
	/** Constructs a FileContents message to send.
	 * 
	 * @param messageName the message's name (FILE).
	 * @param fileName the file's name to send.
	 * @param messageEnd the message's end (END).
	 * @param out the DataOutputStream.
	 */
	public FileContentsMessage(String messageName,String fileName,
			String messageEnd, DataOutputStream out){
		super(messageName, messageEnd, out);
		_fileName = fileName;
	}
	
	/** Constructs a FileContents message 
	 * 	 To receive in the first chance.
	 * 
	 * @param messageName the message's name (FILE).
	 * @param fileName the file's name to receive.
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be FIRST_CHANCE).
	 * @throws NotSupposedToGetException
	 */
	public FileContentsMessage(String messageName,String fileName,
			String messageEnd,DataInputStream in,String chance)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance);
		_fileName = fileName;
	}
	
	/** Constructs a FileContents message To receive
	 *  not in the first chance.
	 * 
	 * @param messageName the message's name (FILE).
	 * @param fileName the file's name to receive.
	 * @param messageEnd the message's end (END).
	 * @param in the DataInputStream.
	 * @param chance the chance to get (Should be SECOND_CHANCE).
	 * @param messageNameGet the message's name which was get
	 * previously, in the first chance to receive the message.
	 * @throws NotSupposedToGetException
	 */
	public FileContentsMessage(String messageName,String fileName,
			String messageEnd,DataInputStream in,String chance,
			String messageNameGet)
					throws NotSupposedToGetException{
		super(messageName,messageEnd,in,chance,messageNameGet);
		_fileName = fileName;
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
		File file = new File(getFileName());
		sendUTF(getMessageNameSuppose(),getDataOutputStream());
		sendLong(file.length(),getDataOutputStream());
		sendFile(file,getDataOutputStream());
		sendUTF(getMessageEnd(),getDataOutputStream());
	}

	/** Sends long.
	 * 
	 * @param word the long to send.
	 * @param out the DataOutputStream.
	 */
	protected void sendLong(long word,
				DataOutputStream out){
		 try {
			 out.writeLong(word);
		 } 
		 catch (IOException e) {
			//We assume it won't happen.
		 }
	 }
	 
	/** Receives long.
	 * 
	 * @param in the DataInputStream.
	 * @return a long.
	 */
	 protected long receiveLong(DataInputStream in) {
		 try {
			 return in.readLong();
		 } 
		 catch (IOException e) {
			 return -1;
		 }
	 }
	 
	 /** Sends a file.
	  * 
	  * @param fileName the name of the file to send.
	  * @param out the DataOutputStream.
	  */
	 private void sendFile(File file, DataOutputStream out){
		 InputStream inFile = null;
		 try{
			 inFile = new FileInputStream(file);
			 for (long i=0;i<file.length();i++)
				 out.write(inFile.read());
		 }
		 catch(FileNotFoundException ex){
			 // Should not happen because we assume there
			 // won't be external modifications of the
			 // content of FM's DB.
		 }
			
		 catch(IOException e){
			//We assume it won't happen.
		 }
		 finally {
			 try{
				 inFile.close();
			 }
			 catch(Exception e){
				//We assume it won't happen.
			 }
		 }
	}

	@Override
	public void receiveMessage() 
		throws NotSupposedToGetException {
		super.receiveMessage();
		long fileSize = receiveLong(getDataInputStream());
		receiveFile(getDataInputStream(),fileSize);
		String messageEnd = receiveUTF(getDataInputStream());
		checkMessageWord(getMessageEnd(),messageEnd);
		
	}

	/** Receives a file.
	 * 
	 * @param in the DataInputStream.
	 * @param fileSize the number of bytes to read from
	 *  the DataInputStream and to write the, to the file.
	 * @return
	 */
	private void receiveFile(DataInputStream in,long fileSize) {
		 File file = new File(_fileName);
		 try {
			 file.createNewFile();
		 } 
		 catch (IOException e) {
			//We assume it won't happen.
		 }
		 OutputStream outFile = null;
		 try{
			 outFile = new FileOutputStream(file);
			 for (long i=0;i<fileSize;i++){
				 int len = in.read();
				 outFile.write(len);
			 }
		 }
		 catch(FileNotFoundException ex){
			 // Should not happen because we assume there
			 // won't be external modifications of the
			 // content of FM's DB.
		 }
		 catch(IOException e){
			//We assume it won't happen.
		 }
		 finally {
			 try{
				 outFile.close();
			 }
			 catch(Exception e){
				//We assume it won't happen.
			 }
		 }
	}
}