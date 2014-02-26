//###############
//FILE : InvalidUsageException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that should be thrown while the
// there is 3 parameters while initialize a FM or
// the port is < 0 or > 65535.
//###############
package oop.ex3.filemanager;

public class InvalidUsageException extends InputParamsException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidUsageException(){
		System.out.println("Invalid Usage");
		System.exit(-1);
	}
}
