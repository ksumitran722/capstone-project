package demo1;


/**
 * 
 *  The class you store in the database must fit 3 simple constraints:

	1) The class must have a default constructor that takes no arguments.
	
	2) The class must have fields that are either:
	 	- Public. Public fields will be loaded/stored.
	 	- Have getter/setter methods defined. Getter/setters must be named to match the fields (a field called "name" needs a method "getName).
	
	3) The class can only have fields that are:
		- primitive data types
		- ArrayLists (not arrays)
		- Objects of classes that follow these same rules.

	Classes from the Java library will often not fit these requirements, so you may need to make simpler classes
	yourself.

	If you want to store more complex data in your class that doesn't fit these rules, then you may want to
	make a separate class that is used only for database reads/writes. 
 * 
 * 
 * @author john_shelby
 *
 */
public class Post {

	private String author;
	private String message;
	
	public Post() {
		
	}

	public Post(String author, String message) {
		this.author = author;
		this.message = message;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getMessage() {
		return message;
	}

	public String toString() {
		return "NAME: " + author + ", MESSAGE: " + message;
	}

}