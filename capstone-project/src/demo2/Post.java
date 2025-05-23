package demo2;
import java.util.ArrayList;

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

	public ArrayList<Dot> dots; // It is OK to use additional classes within your "database objects" as long as they all follow the rules above.
	public int r,g,b;  
	// One thing that is interesting is that the Firebase database cannot store arrays.
	// So, if you want to use a library class that uses arrays (the Color class is one such example), then
	// you need to store the data a different, simpler way yourself.
	// Note that ArrayLists *can* be stored.
	
	public Post() {
		
	}
	


}

class Dot {
	public int x, y;
	
	public Dot() {
		
	}
	
	public Dot(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
}