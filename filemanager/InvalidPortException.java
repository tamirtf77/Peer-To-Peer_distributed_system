//###############
//FILE : InvalidPortException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that should be thrown while the
// port parameter is not a legal integer.
//###############
package oop.ex3.filemanager;

public class InvalidPortException extends InputParamsException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidPortException(){
		System.out.println("Invalid port number");
		System.exit(-1);
	}
}