package demo4;

import java.awt.Rectangle;
import java.util.ArrayList;

import processing.core.PApplet;


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
public class Token {

	public String uniqueID;
	public int x, y;
	public int width, height;
	public int teamOwner;
	public String heldBy;
	
	
	public Token() {
		
	}


	
	public boolean intersects(Token other) {
		return toRectangle().intersects(other.toRectangle());
	}
	
	public Rectangle toRectangle() {
		return new Rectangle(x,y,width,height);
	}

	
	public void draw(PApplet surface, ArrayList<Player> players) {
		if (teamOwner == -1)
			surface.fill(0);
		else if (teamOwner == 0)
			surface.fill(255,0,0);
		else 
			surface.fill(0,0,255);
		surface.stroke(0);
		surface.rect(x, y, width, height);
		
		if (heldBy != null) {
			for (Player p : players) {
				if (p.uniqueID.equals(heldBy)) {
					if (p.team == 0)
						surface.fill(255,0,0);
					else
						surface.fill(0,0,255);
					surface.textSize(20);
					surface.textAlign(PApplet.CENTER);
					surface.text(p.username, x+width/2, y+height/2);
				}
			}
		}
	}
	
	public boolean contains(int xP, int yP) {
		if (xP >= x && yP >= y && xP <= x+width && yP <= y+height)
			return true;
		return false;
	}
	
	
}
