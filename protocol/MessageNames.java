//###############
//FILE : MessageNames.java
//WRITER : tamirtf77
//DESCRIPTION: A class which holds all the names and group names
// of messages (group name is for example : NETADDRESSMESSAGE).
//###############
package oop.ex3.protocol;

import java.util.concurrent.ConcurrentHashMap;

public class MessageNames {

	/** The String representations of the Basic messages.*/
	public static final String BASICMESSAGE = "BASICMESSAGE";
	public static final String ERROR = "ERROR";
	public static final String DONE = "DONE";
	public static final String WELCOME = "WELCOME";
	public static final String GOAWAY = "GOAWAY";
	public static final String GOODBYE = "GOODBYE";
	public static final String FILENOTFOUND = "FILENOTFOUND";
	public static final String WANTSERVERS = "WANTSERVERS";
	public static final String ENDLIST = "ENDLIST";
	public static final String ENDSESSION = "ENDSESSION";
	public static final String WANTALLFILES = "WANTALLFILES";
	
	/** The String representations of the NetAddress messages.*/
	public static final String NETADDRESSMESSAGE = "NETADDRESSMESSAGE";
	public static final String BEGIN = "BEGIN";
	public static final String FILEADDRESS = "FILEADDRESS";
	public static final String CONTAINNAMESERVER = "CONTAINNAMESERVER";
	
	/** The String representations of the FileName messages.*/
	public static final String FILENAMEMESSAGE = "FILENAMEMESSAGE";
	public static final String CONTAINFILE = "CONTAINFILE";
	public static final String DONTCONTAINFILE = "DONTCONTAINFILE";
	public static final String WANTFILE = "WANTFILE";
	public static final String NSCONTAINFILE = "NSCONTAINFILE";
	
	/** The String representations of the FileContents message.*/
	public static final String FILECONTENTSMESSAGE = "FILECONTENTSMESSAGE";
	public static final String FILE = "FILE";
	
	/** The String representation of the message's end.*/
	public static final String MESSAGEEND = "END";
	
	/** The String representation of the first chance to receive.*/
	public static final String FIRST_CHANCE = "FIRST_CHANCE";
	
	/** The String representation of the second chance to receive.
	 * (i.e. second chance to receive means second,third,fourth
	 * ... to receive. 
	 */
	public static final String SECOND_CHANCE = "SECOND_CHANCE";
	
	/** The String representation of an Empty message. */
	public static final String EMPTY = "";
	
	/** The hash map to save a message's name as a key
	 * and a message's group name as a value.
	 */
    private static final ConcurrentHashMap<String, String> 
    							_messagesMap = createMap();

    /** Initialize the hash map of messages' names and 
     * messages' group names.
     * @return the hash map.
     */
    private static ConcurrentHashMap<String, String> createMap() {
    	ConcurrentHashMap<String, String> result = 
    		new ConcurrentHashMap<String, String>();
    	
        result.putIfAbsent(ERROR,BASICMESSAGE);
        result.putIfAbsent(DONE,BASICMESSAGE);
        result.putIfAbsent(WELCOME,BASICMESSAGE);
        result.putIfAbsent(GOAWAY,BASICMESSAGE);
        result.putIfAbsent(GOODBYE,BASICMESSAGE);
        result.putIfAbsent(FILENOTFOUND,BASICMESSAGE);
        result.putIfAbsent(WANTSERVERS,BASICMESSAGE);
        result.putIfAbsent(ENDLIST,BASICMESSAGE);
        result.putIfAbsent(ENDSESSION,BASICMESSAGE);
        result.putIfAbsent(WANTALLFILES,BASICMESSAGE);
        
        result.putIfAbsent(BEGIN,NETADDRESSMESSAGE);
        result.putIfAbsent(FILEADDRESS,NETADDRESSMESSAGE);
        result.putIfAbsent(CONTAINNAMESERVER,NETADDRESSMESSAGE);
        
        result.putIfAbsent(CONTAINFILE,FILENAMEMESSAGE);
        result.putIfAbsent(DONTCONTAINFILE,FILENAMEMESSAGE);
        result.putIfAbsent(WANTFILE,FILENAMEMESSAGE);
        result.putIfAbsent(NSCONTAINFILE,FILENAMEMESSAGE);
    	
        result.putIfAbsent(FILE,FILECONTENTSMESSAGE);

        return result;
    }
    
    /** Gets the message's group name.
     * 
     * @param word the message's name (such as FILEADDRESS).
     * @return the message's group name (such as NETADDRESSMESSAGE).
     */
    public static String getMessageType(String word){
    	if (_messagesMap.get(word) != null)
    		return (_messagesMap.get(word));
    	return EMPTY;
    }
    
    /** Gets the message's end (END).
     * 
     * @return the message's end (END).
     */
    public static String getMessageEnd(){
    	return (MESSAGEEND);
    }
}