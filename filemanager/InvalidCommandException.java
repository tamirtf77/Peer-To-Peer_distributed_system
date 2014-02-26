//###############
//FILE : InvalidCommandException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that is thrown when the user typed
// an invalid command.
//###############
package oop.ex3.filemanager;

public class InvalidCommandException extends InputParamsException {

	private static final long serialVersionUID = 1L;
	
	public InvalidCommandException(){
		System.out.println("Invalid Command");
	}

}
