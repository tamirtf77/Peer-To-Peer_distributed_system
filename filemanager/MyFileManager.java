//###############
//FILE : MyFileManager.java
//WRITER : tamirtf77
//DESCRIPTION: Initialize a FM. The FM uses 2 threads :
// 1. thread (the main) which listens to the user's commands
// and perform them. If the command needs to connect NSs, the
// FM try to connect each NS it knows and requests/ inform
// the NSs.
// 2. thread which listens for requests from FMs for files.
//###############
package oop.ex3.filemanager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oop.ex3.filemanager.FMDB.AddressRecord;
import oop.ex3.filemanager.FMDB.FileRecord;
import oop.ex3.protocol.FileContentsMessage;
import oop.ex3.protocol.FileNameMessage;
import oop.ex3.protocol.Message;
import oop.ex3.protocol.MessageNames;
import oop.ex3.protocol.NotSupposedToGetException;
import oop.ex3.protocol.Session;

public class MyFileManager {
	
	public static void main(String[] args) throws InputParamsException{
		
		/*
		MyFileManager fileManager = new MyFileManager("ns","dir1","3021");
		try{
			fileManager.doAction();
		}
		catch(Exception e){}
		*/
		
		if (args.length < 3)
			throw new InvalidUsageException();
		MyFileManager fileManager = new MyFileManager(args[0],args[1],args[2]);
		try{
			fileManager.doAction();
		}
		catch(Exception e){
			
		}
	}
	
	/** The String representations of the user's commands.*/
	private static final String DIR = "DIR";
	private static final String DIRSERVERS = "DIRSERVERS";
	public static final String ADD = "ADD";
	// ADDAGAIN and ADDFINAL are not user's commands but
	// I used them as a "commands" in order to know which 
	// message to send.
	// They are both in the context of the ADD command.
	// ADDAGAIN is used for requests more NS - WANTSERVERS.
	// ADDAGAIN is used also in the DIRALLFILES command.
	// ADDFINAL is used for inform each NS about CONTAINFILE.
	public static final String ADDAGAIN = "ADDAGAIN";
	public static final String ADDFINAL = "ADDFINAL";
	public static final String REMOVE = "REMOVE";
	public static final String DIRALLFILES = "DIRALLFILES";
	public static final String RENAME = "RENAME";
	public static final String FIRESERVERS = "FIRESERVERS";
	public static final String QUIT = "QUIT";
	
	/** The regex representation of the user's commands. */
	private static final String COMMANDS = "\\A(" + DIR + "|" + DIRSERVERS +
	"|" + ADD + "|" + REMOVE + "|" +DIRALLFILES + "|" +
	RENAME + "|" + FIRESERVERS + "|" + QUIT + ")";
	
	/** The regex representation of the IP. */
	private static final String IP_STRING = "\\A.*%";
	
	/** The regex representation of the port. */
	private static final String PORT_STRING = "\\A[\\d]{1,}";
	
	/** The string representation of the local host.*/
	private static final String LOCAL_HOST_NAME = "localhost";
	
	/** The string representation,as IP, of the local host.*/
	private static final String LOCAL_HOST_IP = "127.0.0.1";
	
	/** The string representation of file exists.*/
	private static final String FILE_EXISTS = "File already exists";
	
	/** The string representation of file download.*/
	private static final String FILE_DOWNLOAD = "File Downloaded " +
			"Successfully from ";
	
	/** The string representation of colon.*/
	private static final String COLON = ":";
	
	/** The string representation of slash.*/
	static final String SLASH = "/";

	/** The string representation of cant download.*/
	private static final String CANT_DOWNLOAD_FILE = "Downloading failed";
	
	/** The string representation of delete impossible.*/
	private static final String DELETE_IMPOSSIBLE = "It is impossible"
		+ " to delete an absent file";
	
	/** The string representation of removing done.*/
	private static final String REMOVING_DONE = "Removing Done";
	
	/** The string representation of rename impossible.*/
	private static final String RENAME_IMPOSSIBLE = "It is impossible" 
		+ " to rename an absent file";
	
	/** The string representation of existing illegal.*/
	private static final String EXISTING_ILLEGAL = "It is illegal to" 
		+ " use an existing file name as a new name";
	
	/** The string representation of renaming done.*/
	private static final String RENAMING_DONE = "Renaming Done";
	
	/** The string representation of renaming done.*/
	private static final String BYE_BYE = "Bye-bye!";
	
	/** The string representation of first.*/
	private static final String FIRST = "FIRST";
	
	/** The string representation of not first.*/
	private static final String NOT_FIRST = "NOT_FIRST";
	
	/** The string representation of second.*/
	private static final String SECOND = "SECOND";
	
	/** The string representation of empty.*/
	private static final String EMPTY = "";
	
	/** The string representation of a space.*/
	private static final String SPACE = " ";
	
	/** The char representation of a space.*/
	private static final char WHITE_SPACE = ' ';
	
	/** The default timeout of the communicating with a NS. */
	private static final int DEFAULT_TIMEOUT = 5000;
	
	/** The FM's port. */
	private int _port;
	
	/** The directory of the files . */
	private File _filesDir;
	
	/** The FM's DB. */
	private FMDB _info;
	
	/** The upload listener of the FM. */
	private UploadListenerThread _myUploadListener;
	
	/** Constructs a new MyFileManager .*/
	public MyFileManager(String serversList,String files,String port)
						throws InputParamsException{
		File serversListFile = checkServersListFile(serversList);
		_filesDir = checkFilesDir(files);
		_port = convertPort(port,true);
		_info = new FMDB(_port,_filesDir);
		readInputFile(serversListFile,_info);
		readFilesNames(_filesDir,_info);
	}
	
	/** Checks if the string ,which represents the name of
	 * the servers' list file ,is a file.
	 *
	 * @param serversList the string which represents the name of
	 * the servers' list file.
	 * @return the File of the servers' list.
	 * @throws InvalidUsageException
	 */
	////////////////////
	private File checkServersListFile(String serversList) 
							throws InvalidPathException {
		File serversListFile = new File(serversList);
		if (!serversListFile.isFile()){
			throw new InvalidPathException();
		}
		return serversListFile;
	}
	
	/** Checks if the string, which represents the name of
	 * the FM's files , is a directory.
	 * 
	 * @param files the string which represents the name of
	 * the FM's files.
	 * @return the directory of the FM's files.
	 * @throws InvalidPathException
	 */
	private File checkFilesDir(String files)
				throws InvalidPathException {
		File filesDir = new File (files);
		if (!filesDir.isDirectory())
			throw new InvalidPathException();
		return filesDir;
	}
	
	/** Converts port from string to integer.
	 * 
	 * @param port the port to convert.
	 * @param FMIntialize true if it is the port of the FM,
	 * else it is a port in the NS's file. (I implemented
	 * it with the exceptions like the school solution does).
	 * @return
	 * @throws InputParamsException
	 */
	private int convertPort(String port,boolean FMIntialize) 
		throws InputParamsException{
		int portInt = -1;
		try{
			portInt = Integer.parseInt(port);
		}
		catch (Exception E){
			// like the school soultion does.
			throw new InvalidPortException();
		}
		// like the school soultion does.
		if ((portInt > 0) && (portInt < 1024) && FMIntialize)
			throw new InvalidPathException(); 
		
		// like the school soultion does.
		if ((portInt < 0) || (portInt > 65535))
			throw new InvalidUsageException();
		return portInt;
	}
	
	/** Reads the servers' list file.
	 * 
	 * @param lines the lines of the commands file 
	 * (in the beginning it's empty).
	 * @throws InvalidPathException 
	 */
	private void readInputFile(File file ,FMDB info) throws InvalidPathException{
		FileInputStream fileInput = null;
		DataInputStream in = null;
		BufferedReader buff_in = null;
		try{
			fileInput = new FileInputStream(file);
			in = new DataInputStream(fileInput);
			buff_in = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = buff_in.readLine()) != null){
				SeparatesIPAndPort(line,info);
	    	 }
		}
		catch (Exception e){
			throw new InvalidPathException();
		}
		finally {
			try{
		    	 in.close();
			}
			catch(Exception e){
				
			}
			try{
				fileInput.close();
			}
			catch(Exception e){
				
			}
		}
	}

	/** Separates the IP and port of a line.
	 * 
	 * @param line the line to separate.
	 * @param info the FM's DB.
	 * @throws InputParamsException
	 */
	private void SeparatesIPAndPort(String line,
			FMDB info) throws InputParamsException  {
		String IP,port;
		IP = getPart(line,IP_STRING);
		IP = IP.substring(0,IP.length()-1);
		if (IP.equals(EMPTY))
			throw new InvalidUsageException();
		line = line.substring(IP.length()+1);
		port = getPart(line,PORT_STRING);
		if (port.equals(EMPTY))
			throw new InvalidPortException();
		line = line.substring(port.length());
		if (!line.equals(EMPTY))
			throw new InvalidPortException();
		info.addNSOrFM(convertIP(IP), convertPort(port,false),FMDB.NS_NAMES);
	}

	/** Gets a part of a line according the pattern's string.
	 * 
	 * @param line a line.
	 * @param pattString the pattern's string
	 * @return the part of a line according to the pattern's string.
	 */
	private String getPart(String line, String pattString){
		Pattern pattern = Pattern.compile(pattString);
		Matcher matcher = pattern.matcher(line);
		if (matcher.lookingAt())
			return (line.substring(matcher.start(), matcher.end()));
		else
			return EMPTY;
	}
	
	/** Converts the IP in case it is the localhost or 127.0.0.1
	 * to the real IP address.
	 * @param IP the IP to convert.
	 * @return the real IP address.
	 * @throws InvalidPathException
	 */
	private String convertIP(String IP) throws InvalidPathException {
		if ((IP.equals(LOCAL_HOST_NAME)) || 
				(IP.equals(LOCAL_HOST_IP)))
			try {
				return InetAddress.getLocalHost().getHostAddress();
			} 
			catch (UnknownHostException e) {
				// We assume it won't happen.
			}
		return IP;
	}
	
	/** Read the files' names of the FM's directory.
	 * 
	 * @param filesDir the FM's directory of the files.
	 * @param info the FM's DB.
	 */
	private void readFilesNames(File filesDir,
			FMDB info) {
		File[] files = filesDir.listFiles();
		for (File file:files)
			if (file.isFile())
				info.addFileName(file.getName(),FMDB.MY_FILES);
		// We assume that it doesn't have sub directories.
		// If someone wants to include sub directories
		// he/she should adds here the code for this.
	}
	
	/** Do the action for the FM - inits the upload thread and
	 * receives commands(from the main thread) from the user
	 * and performs them. Until the action of the current 
	 * user's command has not been finished, no other commands are executed.
	 */
	private void doAction(){
		doCommunicationWithAllNS(EMPTY,_info,FMDB.NS_NAMES);
		initUploadListener();
		Scanner reader = new Scanner(System.in);
		String line = reader.nextLine();
		Pattern patt = Pattern.compile(COMMANDS);
		Matcher matcher = patt.matcher(line);
		String command = EMPTY,firstName = EMPTY,
							   secondName = EMPTY;
		while (!line.equals(QUIT)){
			try{
				command = getCommand(line,patt,matcher);
				line = line.substring(command.length());
				firstName = getCommandParam(command,FIRST,line);
				if (line.length() > 0)
					line = line.substring(firstName.length()+1);
				secondName = getCommandParam(command,SECOND,line);
				if (line.length() > 0)
					line = line.substring(secondName.length()+1);
				doCommand(command,firstName,secondName,_info,_myUploadListener);
			}
			catch (InvalidCommandException e){
			}
			line = reader.nextLine();
		}
		doCommand(QUIT,firstName,secondName,_info,_myUploadListener);
	}
	
	/** Inits the upload listener (thread).
	 * 
	 */
	private void initUploadListener(){
		_myUploadListener = new UploadListenerThread(_port,_info);
		_myUploadListener.start();
	}
	
	/** Get the command (such as RENAME without the parameters)
	 *  from a line.
	 * 
	 * @param line the current line.
	 * @param patt the pattern.
	 * @param matcher the matcher.
	 * @return the command.
	 * @throws InvalidCommandException
	 */
	private String getCommand(String line, Pattern patt, Matcher matcher)
		throws InvalidCommandException {
		String command = separateLine(line,FIRST);
		matcher.reset();
		matcher = patt.matcher(command);
		if (matcher.matches())
			return command;
		else
			throw new InvalidCommandException();
	}
	
	/** Separates the line according to the white spaces.
	 * 
	 * @param line the current line.
	 * @param partLocation FIRST - if it is the first word in
	 * the line else NOT_FIRST.
	 * @return the word according to white spaces and FIRST/ NOT_FIRST.
	 */
	private String separateLine(String line,String partLocation) {
		String part = EMPTY;
		boolean firstSpace = false;
		for (int i=0;i<line.length();i++){
			if (line.charAt(i) != WHITE_SPACE)
					part += line.charAt(i);
			else{
				if (partLocation.equals(NOT_FIRST)){
					if (!firstSpace)
						firstSpace = true;
					else
						return part;
				}
				else
					return part;
			}
		}
		return part;
	}
		
	/** Gets a parameter of a command (if the command has
	 * to have parameter(s) ).
	 * @param command the user's command.
	 * @param locationName FIRST if it should be the first parameter
	 * SECOND if it should be the second parameter.
	 * @param line the current line. 
	 * @return a parameter of a command
	 * @throws InvalidCommandException
	 */
	private String getCommandParam(String command,
			String locationName,String line)
			throws InvalidCommandException {
		String param = separateLine(line,NOT_FIRST);
		boolean suppose = suposseToGet(command,locationName);
		if ((suppose) && (param.equals(EMPTY)))
			throw new InvalidCommandException();
		if ((suppose == false) && (!param.equals(EMPTY)))
			throw new InvalidCommandException();
		return param;
	}

	/** Checks if a command suppose to get Parameters (one or
	 * two parametrs).
	 * @param command the user's command.
	 * @param locationName FIRST if it should has one parameter
	 * SECOND if it should has two parameter. 
	 * @return
	 */
	private boolean suposseToGet(String command,String locationName) {
		if (locationName.equals(FIRST))
			if ((command.equals(ADD)) ||
					(command.equals(REMOVE)) ||
					(command.equals(RENAME)))
				return true;
		if (locationName.equals(SECOND))
			if (command.equals(RENAME))
				return true;
		return false;
	}

	/** Does the user's command.
	 * 
	 * @param command the user's command.
	 * @param firstName the first parameter of a command.
	 * @param secondName the second parameter of a command.
	 * @param info the FM's DB.
	 * @param myUploadListener the FM's upload listener.
	 */
	private void doCommand(String command,String firstName,
			String secondName,FMDB info,
			UploadListenerThread myUploadListener){
		if (command.equals(DIR))
			doDirCommand(FMDB.MY_FILES,info);
		if (command.equals(DIRSERVERS))
			doDirServersCommand(info);
		if (command.equals(ADD))
			doAddCommand(firstName,info);
		if (command.equals(REMOVE))
			doRemoveCommand(firstName,info,myUploadListener);
		if (command.equals(DIRALLFILES))
			doDirAllFilesCommand(info);
		if (command.equals(RENAME))
			doRenameCommand(firstName,secondName,info,myUploadListener);
		if (command.equals(FIRESERVERS))
			doFireServersCommand(info);
		if (command.equals(QUIT))
			doQuitCommand(info,myUploadListener);
	}

	/** Does the DIR command.
	 * 
	 * @param whichList the string which represents the name of
	 * FM's files or system files.
	 * @param info the FM's DB.
	 */
	private void doDirCommand(String whichList,FMDB info) {
		ConcurrentSkipListSet <FileRecord> filesNames = 
					info.getFilesNames(whichList);
		Iterator<FileRecord> iter = filesNames.iterator();
		FileRecord file;
		while (iter.hasNext()){
			file = iter.next();
			System.out.println(file.getFileName());
		}
		if (whichList.equals(FMDB.SYSTEM_FILES))
			info.clear(whichList);
	}
	
	/** Does the DIRSERVERS command.
	 * 
	 * @param info the FM's DB.
	 */
	private void doDirServersCommand(FMDB info) {
		AddressRecord ns;
		for (int i=0; i < info.getNSOrFMSize(FMDB.NS_NAMES); i++){
			ns = info.getNSOrFMName(i, FMDB.NS_NAMES);
			System.out.println(ns.getIP()+ SPACE + ns.getPort());
		}
	}
	
	/** Does the ADD command.
	 * 
	 * @param fileWanted the file wanted. 
	 * @param info the FM's DB.
	 */
	private void doAddCommand(String fileWanted,FMDB info) {
		AddressRecord NS,FM = null;
		boolean download = false;
		info.setFileWanted(fileWanted);
		if (info.containFileName(fileWanted,FMDB.MY_FILES))
			System.out.println(FILE_EXISTS);
		else{
		// copies all the NS names from the constant list
		// to the temp list.
			info.copyAddresses(FMDB.NS_NAMES,FMDB.NS_NAMES_TEMP);
			while ((info.getNSOrFMSize(FMDB.NS_NAMES_TEMP) > 0) &&
					(!download)){
				for (int i=0; ((i<info.getNSOrFMSize(FMDB.NS_NAMES_TEMP)) &&
												(!download));i++){
					NS = info.getNSOrFMName(i,FMDB.NS_NAMES_TEMP);
					doCommunication(NS,ADD,info);
					for (int j=0; ((j< info.getNSOrFMSize(FMDB.FM_NAMES)) &&
												(!download));j++){
						FM = info.getNSOrFMName(j,FMDB.FM_NAMES);
						try{
							download = doCommunicationWithFM(FM,info);
						}
						catch (Exception e){}
					}
					info.clear(FMDB.FM_NAMES);
				}
				
				if (!download){
					info.copyAddresses(FMDB.NS_NAMES_TEMP,FMDB.NS_NAMES_TEMP2);
					info.clear(FMDB.NS_NAMES_TEMP);
					for (int i=0; i<info.getNSOrFMSize(FMDB.NS_NAMES_TEMP2);i++){
						NS = info.getNSOrFMName(i, FMDB.NS_NAMES_TEMP2);
						doCommunication(NS,ADDAGAIN,info);
					}
					info.checkIfWereNS();
				}
			}
			if (download){
				info.addFileName(info.getFileWanted(),FMDB.MY_FILES);
				System.out.println(FILE_DOWNLOAD + FM.getIP()+ COLON + FM.getPort());
				updateNSAboutAdding(info);
			}
			else
				System.out.println(CANT_DOWNLOAD_FILE);
		}
	}

	/** Does communication with other FM in order to get
	 * a file.
	 * @param FM the other FM to connect to.
	 * @param info the FM's DB
	 * @return 
	 * @throws InvalidPathException
	 * @throws IOException
	 * @throws SecurityException
	 */
	private boolean doCommunicationWithFM(AddressRecord FM, FMDB info) 
			throws InvalidPathException,IOException,SecurityException {
		boolean download = false;
		Socket client = new Socket(FM.getIP(),FM.getPort());
		client.setSoTimeout(DEFAULT_TIMEOUT);
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(client.getOutputStream());
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		DataInputStream in = null;
		try {
			in = new DataInputStream(client.getInputStream());
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		//initiate conversation with FM
		try{
			sendReceiveFMToFM(in,out,info);
			download = true;
		}
		catch(NotSupposedToGetException e){
			sendError(out,info);
		}
		try {
			out.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		try {
			in.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}
		try {
			client.close();
		} 
		catch (IOException e) {
			//We assume it won't happen.
		}	
		return download;
	}
	
	/** Sends and receives messages (FM-FM).
	 * 
	 * @param in the DataInputStream.
	 * @param out the DataOutputStream.
	 * @param info the FM's DB.
	 * @throws NotSupposedToGetException
	 */
	private void sendReceiveFMToFM(DataInputStream in,DataOutputStream out,
			FMDB info) throws NotSupposedToGetException{
		Session session = new Session();
		String sendName = session.FMRequestSendUploadSession();
		send(sendName,session,info,out);
		ArrayList<String> receiveNames = session.FMRequestReceiveUploadSession();
		receive(receiveNames,0,MessageNames.FIRST_CHANCE,info,session,in,MessageNames.EMPTY);
	}
	
	/** Sends a message.
	 * 
	 * @param sendName the message's name to send.
	 * @param session the current session(FM-FM).
	 * @param info the FM's DB.
	 * @param out the DataOutputStream.
	 */
	private void send(String sendName, Session session,
			FMDB info,DataOutputStream out){
		Message message = createSendMessage(sendName,info,out);
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
	 * @param session the current session (FM-FM).
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
	
	/** Creates the message To send.
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param out the DataOutputStream.
	 * @return the message.
	 */
	private Message createSendMessage(String messageName,FMDB info,
			DataOutputStream out){
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.FILENAMEMESSAGE))
			message = createFileNameMessage(messageName,info,out);
		return message;
	}

	/** Creates a FileName message to send (WANTFILE).
	 * 
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param out the DataOutputStream.
	 * @return the FileName message.
	 */
	private Message createFileNameMessage(String messageName, FMDB info,
			DataOutputStream out){
		Message message = null;
		if (messageName.equals(MessageNames.WANTFILE))
			message = new FileNameMessage(messageName,
					info.getFileWanted(),MessageNames.getMessageEnd(),out);
		return message;
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
	private Message createReceiveMessage(String messageName,FMDB info, 
			DataInputStream in,String chance,String messageNameGet) 
			throws NotSupposedToGetException {
		Message message = null;
		String messageType = MessageNames.getMessageType(messageName);
		if (messageType.equals(MessageNames.FILECONTENTSMESSAGE))
				 message = createFileContentsMessage(messageName,info,in,chance,messageNameGet);
		
		if (message == null){
			 throw new NotSupposedToGetException();
		}
		return message;
	}

	/** Creates a file contents message to receive(FILE).
	 * @param messageName the message's name.
	 * @param info the FM' DB.
	 * @param in the DataInputStream.
	 * @param chance the String which represenets the chance (first or second)
	 * 				the message.
	 * @param messageNameGet the message's name which was received in 
	 * the first chance.
	 * @return the FileContents message.
	 * @throws NotSupposedToGetException
	 */
	private Message createFileContentsMessage(String messageName, FMDB info,
			DataInputStream in,String chance,String messageNameGet) 
			throws NotSupposedToGetException {
		Message message = null;
		if (messageName.equals(MessageNames.FILE)){
			 if (chance.equals(MessageNames.FIRST_CHANCE))
				 message = new FileContentsMessage(messageName,
						 info.getFileDirPath()+SLASH+info.getFileWanted(),
						 MessageNames.getMessageEnd(),in,chance);
			 if (chance.equals(MessageNames.SECOND_CHANCE)){
				 message = new FileContentsMessage(messageName,
						 info.getFileDirPath()+SLASH+info.getFileWanted(),
						 MessageNames.getMessageEnd(),in,chance,messageNameGet);
			 }
		}
		return message;
	}
	
	/** Sends ERROR in the FM-FM session.
	 * 
	 * @param out the DataOutputStream.
	 * @param info the FM's DB.
	 */
	private void sendError(DataOutputStream out,FMDB info) {
		Message messageToSend = createSendMessage(MessageNames.ERROR,info,out);
		messageToSend.sendMessage();
	}


	/** Communicates with all the known NS.
	 * 
	 * @param command the command to send to each NS such as WANNAFILE.
	 * @param info the FM DB.
	 * @param whichList the list of the constant/temp known NS.
	 */
	private void doCommunicationWithAllNS(String command,
			FMDB info,String whichList){
		AddressRecord NS;
		for (int i=0; i<info.getNSOrFMSize(whichList);i++){
			NS = info.getNSOrFMName(i, whichList);
			doCommunication(NS,command,info);
		}
	}
	
	/** Communicates with each NS.
	 * 
	 * @param NS the NS to communicate with.
	 * @param command the command to send to each NS such as WANNAFILE.
	 * @param info the FM DB.
	 */
	private void doCommunication(AddressRecord NS,String command,
			FMDB info){ 
		try {
			FMToNSSession s = new FMToNSSession(command,NS,info);
			s.doFMToNSSession();
		} 
		catch (IOException e) {
			
		} 
	}
	
	/** Updates that are known to the current FM
	 *  about adding a file.
	 * 
	 * @param info the FM's DB.
	 */
	private void updateNSAboutAdding(FMDB info) {
		doCommunicationWithAllNS(ADDFINAL,info,FMDB.NS_NAMES);
	}
	
	/** Does the REMOVE command.
	 * 
	 * @param fileName the file's name to remove.
	 * @param info the FM's DB.
	 * @param myUploadListener the FM's upload listener.
	 */
	private void doRemoveCommand(String fileName,FMDB info,
			UploadListenerThread myUploadListener){
		if (!info.containFileName(fileName,FMDB.MY_FILES))
			System.out.println(DELETE_IMPOSSIBLE);
		else{
			info.setAvailability(fileName, false);
			myUploadListener.waitTillSingleFileUploadFinish(fileName);
			info.deleteFile(fileName);
			updateNSAboutDeletion(info);
			System.out.println(REMOVING_DONE);
		}
	}

	/** Updates all NS that are known to the current FM about
	 *  a deletion of a file.
	 * @param firstName
	 * @throws InvalidPathException 
	 */
	private void updateNSAboutDeletion(FMDB info) {
		doCommunicationWithAllNS(REMOVE,info,FMDB.NS_NAMES);
	}
	
	/** Does the DIRALLFILES command.
	 * 
	 * @param info the FM's DB.
	 */
	private void doDirAllFilesCommand(FMDB info) {
		AddressRecord NS;
		// copies all the NS names from the constant list
		// to the temp list.
		info.copyAddresses(FMDB.NS_NAMES,FMDB.NS_NAMES_TEMP);
		while (info.getNSOrFMSize(FMDB.NS_NAMES_TEMP) > 0){
			doCommunicationWithAllNS(DIRALLFILES,
					info,FMDB.NS_NAMES_TEMP);
			info.copyAddresses(FMDB.NS_NAMES_TEMP,FMDB.NS_NAMES_TEMP2);
			info.clear(FMDB.NS_NAMES_TEMP);
			for (int i=0; i<info.getNSOrFMSize(FMDB.NS_NAMES_TEMP2);i++){
				NS = info.getNSOrFMName(i, FMDB.NS_NAMES_TEMP2);
				doCommunication(NS,ADDAGAIN,info);	
			}
			info.checkIfWereNS();
		}
		// Calls to DIR command with the SYSTEM_FILES parameter
		// in order to print the system files.
		doDirCommand(FMDB.SYSTEM_FILES,info); 
	}
	
	/** Does the RENAME command.
	 * 
	 * @param firstName the file's name to rename.
	 * @param secondName the new name.
	 * @param info the FM's DB.
	 * @param myUploadListener the FM's upload listener.
	 */
	private void doRenameCommand(String firstName,String secondName,
			FMDB info,UploadListenerThread myUploadListener) {
		if (!info.containFileName(firstName,FMDB.MY_FILES))
			System.out.println(RENAME_IMPOSSIBLE);
		else{
			if ((info.containFileName(firstName,FMDB.MY_FILES)) &&
					(info.containFileName(secondName,FMDB.MY_FILES)))
					System.out.println(EXISTING_ILLEGAL);
			else{
				info.setAvailability(firstName, false);
				myUploadListener.waitTillSingleFileUploadFinish(firstName);
				info.renameFile(firstName, secondName);
				updateNSAboutDeletion(info);
				updateNSAboutAdding(info);
				System.out.println(RENAMING_DONE);
			}
		}
	}
	
	/** Does the FIRESERVERS command.
	 * 
	 * @param info the FM's DB.
	 */
	private void doFireServersCommand(FMDB info){
		doCommunicationWithAllNS(FIRESERVERS,info,FMDB.NS_NAMES);
	}
	
	/** Does the QUIT command.
	 * 
	 * @param info the FM's DB.
	 * @param myUploadListener the FM's upload listener.
	 */
	private void doQuitCommand(FMDB info,
			UploadListenerThread myUploadListener) {
		myUploadListener.FMIsQuit();
		myUploadListener.waitTillAllUploadsFinish();
		try {
			myUploadListener.join();
		} 
		catch (InterruptedException e) {
		}
		doCommunicationWithAllNS(QUIT,info,FMDB.NS_NAMES);
		System.out.println(BYE_BYE);
	}
}