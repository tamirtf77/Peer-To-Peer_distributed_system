//###############
//FILE : InitializeException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that should be thrown if the
// the port to listen for requests is > 0 and < 1024.
//###############
package oop.ex3.nameserver;

public class InitializeException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public InitializeException(){
		System.out.println("Cannot initialize server listening connection");
		System.exit(-1);
	}
}