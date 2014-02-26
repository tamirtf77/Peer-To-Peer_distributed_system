//###############
//FILE : NSDB.java
//WRITER : tamirtf77
//DESCRIPTION: Saves all the data of a NS.
//The MAIN.
//###############
package oop.ex3.nameserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class NSDB {
	
	/** The string which represents the FM names. */
	static final String FM_NAMES = "FM_NAMES";
	
	/** The string which represents the FM temp names. */
	static final String FM_NAMES_TEMP = "FM_NAMES_TEMP";
	
	/** The string which represents the NS names. */
	static final String NS_NAMES = "NS_NAMES";
	
	/** The string which represents the NS temp names. */
	static final String NS_NAMES_TEMP = "NS_NAMES_TEMP";
	
	/** Saves the FMs that are known to the NSs.
	 * the key is AddressRecord and the value is FilesRecord.
	 */
	private ConcurrentHashMap <AddressRecord,FilesRecord> _FMNames;
	
	/** Saves temporarily the FMs that are known to the NSs.
	 * the key is AddressRecord and the value is FilesRecord.
	 */
	private ConcurrentHashMap <AddressRecord,FilesRecord> _FMNamesTemp;
	
	/** Saves the NSs that are known to the current NS. */
	private CopyOnWriteArrayList <AddressRecord> _NSNames;
	
	/** Saves temporarily the NSs that are known to the current NS. */
	private CopyOnWriteArrayList <AddressRecord> _NSNamesTemp;
	
	public NSDB(){
		_FMNames = new ConcurrentHashMap <AddressRecord,FilesRecord>();
		_FMNamesTemp = new ConcurrentHashMap <AddressRecord,FilesRecord>();
		_NSNames = new CopyOnWriteArrayList <AddressRecord>();
		_NSNamesTemp = new CopyOnWriteArrayList <AddressRecord>();
	}
		
	/** Adds a FM to the known FM's by the NS. 
	 * 
	 * @param address the FM's address.
	 * @param which the string representation of which hash maps
	 *  to add this FM.
	 */
	public synchronized void addFM(AddressRecord address,String which){ 
		if (which.equals(FM_NAMES))
				_FMNames.putIfAbsent(address,new FilesRecord());
		if (which.equals(FM_NAMES_TEMP))
				_FMNamesTemp.putIfAbsent(address,new FilesRecord());
	}
	
	/** Deletes a FM from the known FM's by the NS.
	 * 
	 * @param address the FM's address.
	 * @param which the string representation of which hash maps
	 *  to delete from.
	 */
	public synchronized void deleteFM(AddressRecord address,String which){
		if (which.equals(FM_NAMES))
				_FMNames.remove(address);
		if (which.equals(FM_NAMES_TEMP))
				_FMNamesTemp.remove(address);
	}
	
	/** Copies a FM from the _FMNamesTemp to the _FMNames.
	 * 
	 * @param address the FM's address.
	 */
	public synchronized void copyFM(AddressRecord address){
		_FMNames.putIfAbsent(address,_FMNamesTemp.get(address));
	}
	
	/** Adds a file to a FM which is known by the NS.
	 * 
	 * @param address the FM's address.
	 * @param fileName the file's name to add.
	 * @param which the string representation of which hash maps
	 *  to add to.
	 */
	public synchronized void addFileToFM(AddressRecord address,
			String fileName,String which){
		if (which.equals(FM_NAMES))
			if (_FMNames.containsKey(address))
				_FMNames.get(address).add(fileName);
		if (which.equals(FM_NAMES_TEMP))
			if (_FMNamesTemp.containsKey(address))
				_FMNamesTemp.get(address).add(fileName);
	}
	
	/** Deletes a file from a FM which is known by the NS.
	 * 
	 * @param address the FM's address.
	 * @param fileName the file's name to delete.
	 * @param which the string representation of which hash maps
	 *  to delete from.
	 */
	public synchronized void deleteFileFromFM(AddressRecord address,
			String fileName,String which){
		if (which.equals(FM_NAMES))
			if (_FMNames.containsKey(address))
				_FMNames.get(address).remove(fileName);
		if (which.equals(FM_NAMES_TEMP))
			if (_FMNamesTemp.containsKey(address))
				_FMNamesTemp.get(address).remove(fileName);
	}
	
	/** Checks if the FM is known or not to this NS.
	 * 
	 * @param AddressRecord the FM's address to check.
	 * @param which the string representation of which hash maps
	 *  to check in.
	 * @return true if it's known else false.
	 */
	public boolean containFM(AddressRecord FM,String which){
		if (which.equals(FM_NAMES))
			return (_FMNames.containsKey(FM));
		if (which.equals(FM_NAMES_TEMP))
			return (_FMNamesTemp.containsKey(FM));
		return false;
	}
		
	//FILEADDRESS
	/** Gets all the FMs who have the specific file.
	 * @param fileName the file's name.
	 * @return all the FMs who have the specific file.
	 */
	public synchronized ArrayList<AddressRecord> getFMContainsFile(String fileName){
		ArrayList<AddressRecord> addresses = new ArrayList<AddressRecord>();
		Set<AddressRecord> keys = _FMNames.keySet();
		Iterator<AddressRecord> iter = keys.iterator();
		AddressRecord key;
		while (iter.hasNext()){
			key = iter.next();
			if(_FMNames.get(key).containsFile(fileName))
				addresses.add(key);
		}
		return addresses;
	}
	
	/** Gets all the files which are known to the NS.
	 * 
	 * @return all the files which are known to the NS.
	 */
	public synchronized TreeSet<String> getAllFiles(){
		TreeSet<String> allFiles = new TreeSet<String>();
		Set<AddressRecord> keys = _FMNames.keySet();
		Iterator<AddressRecord> iterKeys = keys.iterator();
		ConcurrentSkipListSet<String> files;
		Iterator<String> iterFiles;
		AddressRecord key;
		while (iterKeys.hasNext()){
			key = iterKeys.next();
			files = _FMNames.get(key).getFiles();
			iterFiles = files.iterator();
			while (iterFiles.hasNext())
				allFiles.add(iterFiles.next());
		}
		return allFiles;
	}

	/** Adds NS to a list of NS.
	 * 
	 * @param IP the NS's IP.
	 * @param port the NS's port.
	 * @param to the string representation of which list
	 *  to add.
	 */
	public synchronized void addNS(String IP, int port,String to){
		if (to.equals(NS_NAMES))
			if (!containsNS(IP,port,to))
				_NSNames.add(new AddressRecord(IP,port));
		if (to.equals(NS_NAMES_TEMP))
			if (!containsNS(IP,port,to))
				_NSNamesTemp.add(new AddressRecord(IP,port));
	}
	
	/** Check if a NS is in a list.
	 * 
	 * @param IP the NS's IP.
	 * @param port the NS's port.
	 * @param which the string representation of in which list
	 *  to check.
	 * @return true if the NS is in the list else false.
	 */
	private boolean containsNS(String IP,int port,String which){
		if (which.equals(NS_NAMES))
			return containsLoop(_NSNames,IP,port);
		if (which.equals(NS_NAMES_TEMP))
			return containsLoop(_NSNamesTemp,IP,port);
		return true;
	}
	
	/** Checks if a NS is a list.
	 * 
	 * @param list the list to check.
	 * @param IP the NS's IP.
	 * @param port the NS's port.
	 * @return true if the NS is in the list else false.
	 */
	private boolean containsLoop(CopyOnWriteArrayList<AddressRecord> list,
			String IP,int port){
		for (int i=0;i<list.size();i++)
			if ((list.get(i).getIP().equals(IP)) && 
				(list.get(i).getPort() == port))
				return true;
		return false;
	}
	
	/** Get NS's address from a list according to its index.
	 * 
	 * @param index the index of the NS's address.
	 * @param which the string representation of which list
	 *  to get the NS's address.
	 * @return the NS's address.
	 */
	public synchronized AddressRecord getNS(int index,String which){
		if (which.equals(NS_NAMES))
			return _NSNames.get(index);
		if (which.equals(NS_NAMES_TEMP))
			return _NSNamesTemp.get(index);
		return null;
	}
	
	/** Gets the size of the NSs list.
	 * 
	 * @param which the string representation of which list
	 *  to get.
	 * @return the size of the NSs list.
	 */
	public synchronized int getNSSize(String which){
		if (which.equals(NS_NAMES))
			return _NSNames.size();
		if (which.equals(NS_NAMES_TEMP))
			return _NSNamesTemp.size();
		return -1;
	}
		
	/** Copy the new known NS from the temp list of NS to the
	 * current list of NS, and clears the temp list.
	 */
	public synchronized void copyToNSNames(){
		for (int i = 0;i<_NSNamesTemp.size();i++)
				addNS(_NSNamesTemp.get(i).getIP(),
						_NSNamesTemp.get(i).getPort(),NS_NAMES);
		_NSNamesTemp.clear();
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
		
		@Override 
		public int hashCode(){
		    int hash = 1;
		    hash = hash * 31 + _IP.hashCode();
		    hash = hash * 31 + _port;
		    return hash;
		}
		
		@Override
		public boolean equals(Object other){
			if (this == other)
				return true;
			if (!(other instanceof AddressRecord))
				return false;
			else 
				if ((this._IP.equals( ((AddressRecord)(other)).getIP())) &&
						(this._port == ( ((AddressRecord)(other)).getPort())))
					return true;
			return false;
		}
	}
	
	/** An inner class which represents files' record.
	 *  the files' record is the value in the 
	 *  ConcurrentHashMap of FMs - each FM have files
	 *  that a NS should hold their names.
	 * 
	 * @author Tamir Faibish, tamirtf77, 301755344.
	 *
	 */
	public class FilesRecord{
		
		/** the files set. */
		private ConcurrentSkipListSet <String> _files;
		
		/** Constructs a new FilesRecord. */
		public FilesRecord(){
			_files = new ConcurrentSkipListSet <String>();
		}
		
		/** Removes a file's name.
		 * 
		 * @param fileName the file's name to remove.
		 */
		public void remove(String fileName) {
			_files.remove(fileName);
		}

		/** Adds a file's name.
		 * 
		 * @param fileName the file's name to add.
		 */
		public void add(String fileName) {
			_files.add(fileName);
		}
		
		/** Check if a file's name is already in.
		 * 
		 * @param fileName the file's name to check.
		 * @return true if it is in, else false.
		 */
		public boolean containsFile(String fileName){
			return (_files.contains(fileName));
		}
		
		/** Gets all the files' names.
		 * 
		 * @return a set of all the files' names.
		 */
		public ConcurrentSkipListSet <String> getFiles(){
			return _files;
		}
	} 
}