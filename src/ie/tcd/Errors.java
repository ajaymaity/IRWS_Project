package ie.tcd;

import java.io.File;

/**
 * Takes care of all the Errors that may occur while parsing the documents and queries.
 * @author Ajay Maity Local
 *
 */
public class Errors {

	/**
	 * This error is thrown when an element is being closed before it is opened.
	 * @param elementName name of element
	 * @param lineNumber line number where this error is thrown
	 * @param file file which has the error
	 */
	public static void printUnopenedElementErrorAndExit(String elementName, int lineNumber, File file) {
		
		System.out.println(elementName + " element is not opened yet and line " + Integer.toString(lineNumber) + " is trying to close it in file " + file.getPath() + ".");
		System.out.println("Exiting application.");
		System.exit(1);
	}
	
	/**
	 * This error is thrown when an element is being opened before the previous element is opened.
	 * @param elementName name of element
	 * @param lineNumber line number where this error is thrown
	 * @param file file which has the error
	 */
	public static void printUnclosedElementErrorAndExit(String elementName, int lineNumber, File file) {
		
		System.out.println("Previous " + elementName + " element is not closed before the start of new " + elementName + " element at line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Exiting application.");
		System.exit(1);
	}
	
	/**
	 * This error is thrown when the line is malformed and has syntax errors.
	 * @param line contents of the malformed line
	 * @param lineNumber line number where this error is thrown
	 * @param file file which has the error
	 */
	public static void printMalformedErrorAndExit(String line, int lineNumber, File file) {
		
		System.out.println("Malformed file. Error parsing line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Contents of the line:");
		System.out.println(line);
		System.out.println("Exiting application.");
		System.exit(1);
	}
	
	/**
	 * This error is thrown when an element is being opened before the document is opened.
	 * @param elementName name of element
	 * @param lineNumber line number where this error is thrown
	 * @param file file which has the error
	 */
	public static void printUnopenedDocErrorAndExit(String elementName, int lineNumber, File file) {
		
		System.out.println("DOC element is not opened yet and " + elementName + " element is being used in line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Exiting application.");
		System.exit(1);
	}

	/**
	 * This error is thrown due to an unexpected circumstance, such as,
	 * when the document fields have different structure than what is expected of this application.
	 * The document may not have any problem as such,
	 * but handling such document structure may be out of the scope of this application.
	 * @param line contents of the line
	 * @param lineNumber line number where this error is thrown
	 * @param file file which has the error
	 */
	public static void printUnexpectedErrorAndExit(String line, int lineNumber, File file) {
		
		System.out.println("Unexpected error occured at line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Contents of the line:");
		System.out.println(line);
		System.out.println("Exiting application.");
		System.exit(1);
	}
	
	/**
	 * This error is thrown whenever any line can't be parsed by the parser, and is outside the given structure.
	 * @param line contents of the line
	 * @param lineNumber line number of the line
	 * @param file file which has the error
	 */
	public static void printCantParseErrorAndExit(String line, int lineNumber, File file) {
		
		System.out.println("*****Can't parse the following line (line number: " + Integer.toString(lineNumber) + ", file: " + file.getPath() + ")*****");
		System.out.println(line + "\n");
	}
	
	/**
	 * This error is thrown when XX element expects a particular element next, but finds something else.
	 * @param xxNext the element XX expects next
	 * @param found the element that was actually found
	 * @param lineNumber line number where this error occured
	 * @param file file which has the error
	 */
	public static void printXXDoesNotMatchElement(String xxNext, String found, int lineNumber, File file) {
		
		System.out.println("XX Element expects " + xxNext + " next, but found " + found + " at line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Exiting application.");
		System.exit(1);
	}
	
	/**
	 * This error is thrown when a different element is found than what was expected while parsing topic.
	 * @param expected the element that was expected
	 * @param found the element that was actually found
	 * @param lineNumber line number where this error occured
	 * @param file file which has the error
	 */
	public static void printUnexpectedNextElement(String expected, String found, int lineNumber, File file) {
		
		System.out.println("Expected " + expected + " element next, but found " + found + " at line " + Integer.toString(lineNumber) + " of file " + file.getPath() + ".");
		System.out.println("Exiting application.");
		System.exit(1);
	}
}
