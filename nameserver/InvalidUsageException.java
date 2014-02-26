//###############
//FILE : InvalidUsageException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that should be thrown if the
// number of parameters when initialize the NS if less then 1,
// or if the port parameter is not an integer or the port is
// less than 0 or more than 65535.
// The 3 options is like the school solution.
//###############
package oop.ex3.nameserver;

public class InvalidUsageException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidUsageException(){
		System.out.println("Invalid Usage");
		System.exit(-1);
	}
}