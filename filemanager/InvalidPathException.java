//###############
//FILE : InvalidPathException.java
//WRITER : tamirtf77
//DESCRIPTION: An exception that should be thrown when the
// directory parameter is not a directory or the port is
// > 0 and < 1024 (like the school soultion does).
//###############
package oop.ex3.filemanager;

public class InvalidPathException extends InputParamsException {
	
	private static final long serialVersionUID = 1L;
	
	public InvalidPathException(){
		System.out.println("Invalid path!");
		System.exit(-1);
	}
}