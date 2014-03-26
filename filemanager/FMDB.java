//###############
//FILE : FMDB.java
//WRITER : tamirtf77
//DESCRIPTION: Saves all the data of a FM.
//###############
package oop.ex3.filemanager;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;


public class FMDB{
	
	/** The string which represents my files. */
	static final String MY_FILES = "MY_FILES";
	
	/** The string which represents the system's files. */
	static final String SYSTEM_FILES = "SYSTEM_FILES";
	
	/** The string which represents the system's temp files. */
	static final String SYSTEM_FILES_TEMP = "SYSTEM_FILES_TEMP";
	
	/** The string which represents the NS names. */
	static final String NS_NAMES = "NS_NAMES";
	
	/** The string which represents the NS temp names. */
	static final String NS_NAMES_TEMP = "NS_NAMES_TEMP";
	
	/** The string which represents the NS temp2 names.
	 * It's helpful in the commands ADD and DIRALLFILES. */
	static final String NS_NAMES_TEMP2 = "NS_NAMES_TEMP2";
	
	/** The string which represents the FM names. */
	static final String FM_NAMES = "FM_NAMES";
	
	/** The string which represents the FM  temp names. */
	static final String FM_NAMES_TEMP = "FM_NAMES_TEMP";
		
	private AddressRecord _myAddress;
	
	private File _filesDir;
	
	/** Saves the files' names of the file manager. */
	private ConcurrentSkipListSet <FileRecord> _filesNames;
	
	/** Saves the files' names of the file manager. */
	private ConcurrentSkipListSet <FileRecord> _systemFilesNames;
	
	/** Saves the files' names of the file manager. */
	private ConcurrentSkipListSet <FileRecord> _systemFilesNamesTemp;
	
	private String _fileWanted;
	
	private String _lastFileDeleted;
	
	/** Saves the servers that are known to the file manager. */
	private CopyOnWriteArrayList <AddressRecord> _NSNames;
	
	/** Saves the servers that are known to the file manager 
	 * temporarily. */
	private CopyOnWriteArrayList <AddressRecord> _NSNamesTemp;
	
	/** Saves the servers that are known to the file manager 
	 * temporarily. */
	private CopyOnWriteArrayList <AddressRecord> _NSNamesTemp2;
	
	/** Saves the file managers that are known to the file manager.
	 * the file manager saves them in case it wants a file
	 * (ADD - WANTFILE) that is not in its data base. */
	private CopyOnWriteArrayList <AddressRecord> _FMNames;
	
	/** Saves the file managers ,temporarily,
	 * that are known to the file manager.
	 * the file manager saves them in case it wants a file
	 * (ADD - WANTFILE) that is not in its data base. */
	private CopyOnWriteArrayList <AddressRecord> _FMNamesTemp;
	
	/** Constructs new file manager's data base.*/
	public FMDB(int port,File filesDir){
		try {
			_myAddress = new AddressRecord(InetAddress.getLocalHost().getHostAddress(),port);
		} 
		catch (UnknownHostException e) {
		}
		_filesDir = filesDir;
		_filesNames = new ConcurrentSkipListSet <FileRecord>(new FileComparator());
		_systemFilesNames = new ConcurrentSkipListSet <FileRecord>(new FileComparator());
		_systemFilesNamesTemp = new ConcurrentSkipListSet <FileRecord>(new FileComparator());
		_NSNames = new CopyOnWriteArrayList <AddressRecord>();
		_NSNamesTemp = new CopyOnWriteArrayList <AddressRecord>();
		_NSNamesTemp2 = new CopyOnWriteArrayList <AddressRecord>();
		_FMNames = new CopyOnWriteArrayList <AddressRecord>();
		_FMNamesTemp = new CopyOnWriteArrayList <AddressRecord>();
		_lastFileDeleted = null;
		_fileWanted = null;
	}
	
	/** Gets the FM' address.
	 * 
	 * @return the FM's address.
	 */
	public AddressRecord getMyAddress(){
		return _myAddress;
	}
	
	/** An inner class which implements comparator for FileRecord and
	 * override the compare. It useful for the FM' files and
	 * system's files.
	 * @author Tamir Faibish, tamirtf77, 301755344
	 *
	 */
	public class FileComparator implements Comparator<FileRecord>{

		@Override
		public int compare(FileRecord o1, FileRecord o2) {
			return o1.getFileName().compareTo(o2.getFileName());
		}
	}
	
	/** Gets the absolute path of the dir where the files of
	 * the FM are kept.
	 * @return the absolute path.
	 */
	public String getFileDirPath(){
		return _filesDir.getAbsolutePath();
	}
	
	/** Gets the files' set of FM's files or system files.
	 * 
	 * @param which the string which represents the name of
	 * FM's files or system files.
	 * @return the files' set of FM's files or system files. 
	 */
	public ConcurrentSkipListSet<FileRecord> getFilesNames(String which){
		if (which.equals(MY_FILES))
			return _filesNames;
		if (which.equals(SYSTEM_FILES))
			return _systemFilesNames;
		if (which.equals(SYSTEM_FILES_TEMP))
			return _systemFilesNamesTemp;
		return null;
	}
	
	/** Adds a file record to FM' files or system files.
	 * 
	 * @param name the file name to add.
	 * @param to the string which represents the name of
	 * FM's files or system files. 
	 */
	public void addFileName(String name,String to){
		if (to.equals(MY_FILES))
			_filesNames.add(new FileRecord(name,true));
		if (to.equals(SYSTEM_FILES))
			_systemFilesNames.add(new FileRecord(name,true));
		if (to.equals(SYSTEM_FILES_TEMP))
			_systemFilesNamesTemp.add(new FileRecord(name,true));
	}
	
	/** Gets the number files of the 
	 * FM' files or system files.
	 * @param which the string which represents the name of
	 * FM's files or system files.
	 * @return the number files of the 
	 * FM' files or system files.
	 */
	public int getFilesNamesSize(String which){
		if (which.equals(MY_FILES))
			return _filesNames.size();
		if (which.equals(SYSTEM_FILES))
			return _systemFilesNames.size();
		if (which.equals(SYSTEM_FILES_TEMP))
			return _systemFilesNamesTemp.size();
		return -1;
	}
	
	/** Checks whether the file is already found in the 
	 * FM's files or system files
	 * @param fileName the file's name to check.
	 * @param which the string which represents the name of
	 * FM's files or system files.
	 * @return true if the file is found else false.
	 */
	
	public boolean containFileName(String fileName,
			String which){
		FileRecord file = null;
		Iterator<FileRecord> i = null;
		if (which.equals(MY_FILES))
			i  = _filesNames.iterator();
		if (which.equals(SYSTEM_FILES))
			i  = _systemFilesNames.iterator();
		if (which.equals(SYSTEM_FILES_TEMP))
			i  = _systemFilesNamesTemp.iterator();
		while (i.hasNext()){
			file = i.next();
			if (file.getFileName().equals(fileName))
			return true;
		}
		return false;
	}
	
	/** Copies all the system files from the temp set to the
	 * constant set.
	 */
	public void copySystemFiles(){
		FileRecord file = null;
		Iterator<FileRecord> i = _systemFilesNamesTemp.iterator();
		while (i.hasNext()){
			file = i.next();
			addFileName(file.getFileName(),SYSTEM_FILES);
		}
		
	}
	/** Clears the data structure according to 
	 * 	the string which represents the name of.
	 * @param which the string which represents the name of.
	 */
	public void clear(String which){
		if (which.equals(SYSTEM_FILES)){
			_systemFilesNames.clear();
		}
		if (which.equals(SYSTEM_FILES_TEMP)){
			_systemFilesNamesTemp.clear();
		}
		if (which.equals(NS_NAMES_TEMP)){
			_NSNamesTemp.clear();
		}
		if (which.equals(NS_NAMES_TEMP2)){
			_NSNamesTemp2.clear();
		}
		if (which.equals(FM_NAMES)){
			_FMNames.clear();
		}
		if (which.equals(FM_NAMES_TEMP)){
			_FMNamesTemp.clear();
		}
	}
	
	/** Sets the file wanted.
	 * 
	 * @param fileName the file name.
	 */
	public void setFileWanted(String fileName){
		_fileWanted = fileName;
	}
	
	/** Gets the file wanted.
	 * 
	 * @return the file wanted.
	 */
	public String getFileWanted(){
		return _fileWanted;
	}
	
	/** Deletes physically the file.
	 * 
	 * @param fileName the name of the file to delete physically.
	 */
	public void deleteFile(String fileName){
		FileRecord temp = null;
		Iterator<FileRecord> i = _filesNames.iterator();
		while (i.hasNext()){
			temp = i.next();
			if (temp.getFileName().equals(fileName)){
				_filesNames.remove(temp);
				File file = new File(_filesDir.getAbsolutePath(),fileName);
				try{
					file.delete();
				}
				catch (SecurityException e){
				}
				setLastFileNameDeleted(fileName);
			}
		}
	}
	
	/** Sets the last file's name which was deleted.
	 * 
	 * @param fileName the file that was deleted.
	 */
	public void setLastFileNameDeleted(String fileName){
		_lastFileDeleted = fileName;
	}
	
	
	/** Gets the last file's name which was deleted.
	 * 
	 * @return the last file's name which was deleted.
	 */
	public String getLastFileNameDeleted(){
		return _lastFileDeleted;
	}
	
	/** Renames the file with the new name.
	 * 
	 * @param fileName the current name.
	 * @param newFileName the new name.
	 */
	public void renameFile(String fileName,String newFileName){
		FileRecord temp = null;
		Iterator<FileRecord> i = _filesNames.iterator();
		while (i.hasNext()){
			temp = i.next();
			if (temp.getFileName().equals(fileName)){
				File file = new File(_filesDir.getAbsolutePath(),fileName);
				File newFile = new File(_filesDir.getAbsolutePath(),newFileName);
				file.renameTo(newFile);
				temp.setFileName(newFileName);
				
				// removes the file's record and inserts it again
				// in order to resort the files' names.
				_filesNames.remove(temp);
				_filesNames.add(temp);
				setLastFileNameDeleted(fileName);
				setFileWanted(newFileName);
			}
		}
	}
	
	/** Checks the availability of a file.
	 * It is useful in order to know whether a file could be
	 * uploaded to another FM.
	 * @param fileName the file's name to check.
	 * @return true if the file is available else false.
	 */
	public synchronized boolean getAvailability(String fileName){
		FileRecord temp = null;
		Iterator<FileRecord> i = _filesNames.iterator();
		while (i.hasNext()){
			temp = i.next();
			if (temp.getFileName().equals(fileName))
				return temp.getAvailable();
		}
		return false;
	}
	
	/** Sets the availability of a file.
	 * 
	 * @param fileName the file's name to sets.
	 * @param available the status to set (true / false).
	 */
	public synchronized void setAvailability(String fileName,boolean available){
		FileRecord temp = null;
		Iterator<FileRecord> i = _filesNames.iterator();
		while (i.hasNext()){
			temp = i.next();
			if (temp.getFileName().equals(fileName))
				temp.setAvailable(available);
		}
	}
	
	
	
	
	/** Adds an address to a list.
	 * 
	 * @param IP the IP of the address.
	 * @param port the port of the address.
	 * @param to the string which represents the name of the list.
	 */
	public void addNSOrFM(String IP, int port,String to){
		if (to.equals(NS_NAMES))
			if (!containsAddress(IP,port,to))
				_NSNames.add(new AddressRecord(IP,port));
		if (to.equals(NS_NAMES_TEMP))
			if (!containsAddress(IP,port,to))
				_NSNamesTemp.add(new AddressRecord(IP,port));
		if (to.equals(NS_NAMES_TEMP2))
			if (!containsAddress(IP,port,to))
				_NSNamesTemp2.add(new AddressRecord(IP,port));
		if (to.equals(FM_NAMES))
			if (!containsAddress(IP,port,to))
				_FMNames.add(new AddressRecord(IP,port));
		if (to.equals(FM_NAMES_TEMP))
			if (!containsAddress(IP,port,to))
				_FMNamesTemp.add(new AddressRecord(IP,port));
	}
	
	/** Checks whether the address(IP + port) is 
	 * found in a list.
	 * @param IP the IP of the address.
	 * @param port the port of the address.
	 * @param which the string which represents
	 * 				 the name of the list.
	 * @return true if the address is found else false.
	 */
	private boolean containsAddress(String IP,int port,String which){
		if (which.equals(NS_NAMES))
			return containsLoop(_NSNames,IP,port);
		if (which.equals(NS_NAMES_TEMP))
			return containsLoop(_NSNamesTemp,IP,port);
		if (which.equals(NS_NAMES_TEMP2))
			return containsLoop(_NSNamesTemp2,IP,port);
		if (which.equals(FM_NAMES))
			return containsLoop(_FMNames,IP,port);
		if (which.equals(FM_NAMES_TEMP))
			return containsLoop(_FMNamesTemp,IP,port);
		return true;
	}
	
	/** Traverse the list in order to check whether the address
	 * if found in the list.
	 * @param list the list to traverse on.
	 * @param IP the IP of the address.
	 * @param port the port of the address.
	 * @return true if the address is found else false.
	 */
	private boolean containsLoop(CopyOnWriteArrayList<AddressRecord> list,
			String IP,int port){
		for (int i=0;i<list.size();i++)
			if ((list.get(i).getIP().equals(IP)) && 
				(list.get(i).getPort() == port))
				return true;
		return false;
	}
	
	/** Gets an address according to the index and the name of
	 * the list.
	 * 
	 * @param index the index.
	 * @param which the string which represents
	 * 				 the name of the list.
	 * @return the address.
	 */
	public AddressRecord getNSOrFMName(int index,String which){
		if (which.equals(NS_NAMES))
			return _NSNames.get(index);
		if (which.equals(NS_NAMES_TEMP))
			return _NSNamesTemp.get(index);
		if (which.equals(NS_NAMES_TEMP2))
			return _NSNamesTemp2.get(index);
		if (which.equals(FM_NAMES))
			return _FMNames.get(index);
		if (which.equals(FM_NAMES_TEMP))
			return _FMNamesTemp.get(index);
		return null;
	}
	
	/** Gets the number of elements in the list.
	 * 
	 * @param which the string which represents
	 * 				 the name of the list. 
	 * @return the number of elements in the list. 
	 */
	public int getNSOrFMSize(String which){
		if (which.equals(NS_NAMES))
			return _NSNames.size();
		if (which.equals(NS_NAMES_TEMP))
			return _NSNamesTemp.size();
		if (which.equals(NS_NAMES_TEMP2))
			return _NSNamesTemp2.size();
		if (which.equals(FM_NAMES))
			return _FMNames.size();
		if (which.equals(FM_NAMES_TEMP))
			return _FMNamesTemp.size();
		return -1;
	}
	
	/** Copies all the addresses from one list to the other list.
	 * 
	 * @param from the string which represents
	 * 				 the name of the list to copy from.
	 * @param to the string which represents
	 * 				 the name of the list to copy to.
	 */
	public void copyAddresses(String from,String to){
		if (from.equals(NS_NAMES))
			copyLoop(_NSNames,to);
		if (from.equals(NS_NAMES_TEMP))
			copyLoop(_NSNamesTemp,to);
		if (from.equals(NS_NAMES_TEMP2))
			copyLoop(_NSNamesTemp2,to);
		if (from.equals(FM_NAMES))
			copyLoop(_FMNames,to);
		if (from.equals(FM_NAMES_TEMP))
			copyLoop(_FMNamesTemp,to);
	}
	
	/** Traverse the list in order to copy.
	 * 
	 * @param list the list to traverse. 
	 * @param to the string which represents
	 * 				 the name of the list to copy to.
	 */
	private void copyLoop(CopyOnWriteArrayList<AddressRecord> list, String to) {
		for (int i = 0;i<list.size();i++){
			addNSOrFM(list.get(i).getIP(),
					list.get(i).getPort(),to);
		}
	}
	
	/** Checks for each NS in _NSNamesTemp if it is in the 
	 * list _NSNames. if not - left it in _NSNamesTemp else
	 * remove it from _NSNamesTemp.
	 */
	public void checkIfWereNS(){
		for (AddressRecord address:_NSNamesTemp){
			if (containsAddress(address.getIP(),
					address.getPort(),NS_NAMES))
				_NSNamesTemp.remove(address);
		}
	}
	
	/** An inner class which represents a file's record.
	 * 
	 * @author tamirtf77
	 *
	 */
	public class FileRecord{
		
		/** The file's name. */
		private String _fileName;
		
		/** The availability of the file. */
		private boolean _available;
		
		/** Constructs a new FileRecord. */
		private FileRecord(String fileName, boolean available){
			setFileName(fileName);
			setAvailable(available);
		}

		/** Sets the file's name.
		 * 
		 * @param fileName the name to set.
		 */
		private void setFileName(String fileName) {
			_fileName = fileName;
		}

		/** Gets the file's name.
		 * 
		 * @return the file's name.
		 */
		protected String getFileName() {
			return _fileName;
		}

		/** Sets the availability of a file.
		 * 
		 * @param available true if the file is available else
		 * false.
		 */
		private void setAvailable(boolean available) {
			_available = available;
		}

		/** Gets the availability of a the file.
		 * 
		 * @return the availability of a the file.
		 */
		protected boolean getAvailable() {
			return _available;
		}
	}
	
	/** An inner class which represents an address's record.
	 * 
	 * @author Tamir Faibish, tamirtf77, 301755344.
	 *
	 */
	public class AddressRecord{
		
		/** The IP of the address. */
		private String _IP;
	
		/** The port of the address. */
		private int _port;
		
		/** Constructs a new AddressRecord. */
		protected AddressRecord(String IP, int port){
			setIP(IP);
			setPort(port);
		}
		
		/** Sets the IP of the address
		 * 
		 * @param IP IP to set.
		 */
		protected void setIP(String IP) {
			_IP = IP;
		}

		/** Gets the IP of the address.
		 * 
		 * @return the IP of the address.
		 */
		protected String getIP() {
			return _IP;
		}

		/** Sets the port of the address
		 * 
		 * @param port number to set.
		 */
		protected void setPort(int port) {
			_port = port;
		}

		/** Gets the port of the address.
		 * 
		 * @return the port of the address.
		 */
		protected int getPort() {
			return _port;
		}
	}
}