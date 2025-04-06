package demo4;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import com.google.cloud.Timestamp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import processing.core.PApplet;


/**
 * 
 * For this to work, you must first create a "realtime" database:
 * https://firebase.google.com/
 * 
 * And use the "service accounts" menu to generate a JSON key for access.
 * 
 * Check here for more code samples:
 * https://firebase.google.com/docs/database/admin/save-data
 * 
 * 
 * 
 * @author john_shelby
 *
 */
public class DrawingSurface extends PApplet {

	
	// Window stuff
	private static final int DRAWING_WIDTH = 800, DRAWING_HEIGHT = 600;
	private static double scaleX, scaleY;
	private static final Rectangle WINDOW_BOUNDS = new Rectangle(0,0,DRAWING_WIDTH,DRAWING_HEIGHT);
	
	
	
	// Gameplay stuff 
	private Player me;
	private boolean owner;
	private ArrayList<Player> players;
	private ArrayList<Token> tokens;
	private ArrayList<Integer> points;
	private int pointUpdateCounter;
	private Point draggingOffset;
	
	
	
	
	// Database stuff
	private DatabaseReference roomRef;  // This is the database entry for the whole room
	private DatabaseReference myUserRef;  // This is the database entry for just our user's data. This allows us to more easily update ourselves.
	private DatabaseReference tokensRef;
	private DatabaseReference pointsRef;
	
	
	private boolean currentlySending;  // These field allows us to limit database writes by only sending data once we've received confirmation the previous data went through.
	
	
	
	
	
	

	public DrawingSurface(DatabaseReference roomRef, boolean owner) {
	
		players = new ArrayList<Player>();
		tokens = new ArrayList<Token>();
		points = new ArrayList<Integer>();
		
		this.roomRef = roomRef;
		this.owner = owner;
		currentlySending = false;
	}
	
	public void settings() {
		setSize(DRAWING_WIDTH,DRAWING_HEIGHT);
	}

	public void setup() {
		// The Player uses the PApplet in its constructor, so we're initializing a bunch of stuff in setup() instead of the constructor
		// so the PApplet is definitely ready to go.
		
		String username = JOptionPane.showInputDialog("Give yourself a username:");
		
		DatabaseReference usersRef = roomRef.child("users");
		usersRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onCancelled(DatabaseError arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDataChange(DataSnapshot arg0) {
				
				int[] teams = new int[2]; // Only 2 teams for now. Maybe more later.
				for (DataSnapshot ds : arg0.getChildren()) {
					
					int thisTeam = ds.child("team").getValue(Integer.class);
					teams[thisTeam]++;
				}
				
				int minVal = teams[0];
				int minInd = 0;
				for (int i = 1; i < teams.length; i++) {  // Find smallest team
					if (teams[i] < minVal) {
						minVal = teams[i];
						minInd = i;
					}
				}
				
				myUserRef = usersRef.push();
				me = new Player(myUserRef.getKey(), username, minInd);
				myUserRef.setValueAsync(me);
				
				usersRef.addChildEventListener(new UserChangeListener());
			}
			
		});
		
		tokensRef = roomRef.child("tokens");
		if (owner) {
			tokens = generateTokens();
			for (Token t : tokens) {
				DatabaseReference thisTokenRef = tokensRef.push();
				t.uniqueID = thisTokenRef.getKey();
				thisTokenRef.setValueAsync(t);
			}
		}
		
		tokensRef.addChildEventListener(new TokenChangeListener());
		
		
		pointsRef = roomRef.child("points");
		if (owner) {
			points.add(0);
			points.add(0);
			pointsRef.setValueAsync(points);
		} else {
			pointsRef.addValueEventListener(new PointsChangeListener());
		}
		
		
		Runtime.getRuntime().addShutdownHook(new Thread()  // This code runs when the program exits.
	    {
	      public void run()
	      {
	    	  if (owner) {
					roomRef.removeValueAsync();
	    	  } else {
					myUserRef.removeValueAsync();
	    	  }

	      }
	    });
	}
	
	public ArrayList<Token> generateTokens() {
		ArrayList<Token> tokens = new ArrayList<Token>();
		for (int i = 0; i < 8; i++) {
			Token t = new Token();
			t.width = (int)(Math.random()*50+30);
			t.height = (int)(Math.random()*50+30);
			t.x = (int)(Math.random()*(DRAWING_WIDTH-200))+100;
			t.y = (int)(Math.random()*(DRAWING_HEIGHT-200))+100;
			t.teamOwner = -1;
			boolean hit = false;
			for (Token t2 : tokens) {
				if (t.intersects(t2))
					hit = true;
			}
			if (hit)
				i--;
			else
				tokens.add(t);
		}
		return tokens;
	}
	
	public void draw() {
		background(255);
		
		push();
		scaleX = (double)width/DRAWING_WIDTH;
		scaleY = (double)height/DRAWING_HEIGHT;
		scale((float)scaleX, (float)scaleY);
		
		for (int i = 0; i < tokens.size(); i++) {
			tokens.get(i).draw(this, players);
		}
		
		if (points.size() == 2) {  // Make sure the owner has set up the points ArrayList
		
			String redTeam = "RED = " + points.get(0);
			String blueTeam = "BLUE = " +  + points.get(1);
			for (Player p : players) {
				if (p.team == 0) {
					redTeam += "\n" + p.username;
				} else {
					blueTeam += "\n" + p.username;
				}
			}
			textSize(20);
			textAlign(PApplet.LEFT);
			fill(255,0,0);
			text(redTeam, 20, 100);
			
			fill(0,0,255);
			text(blueTeam, 700, 100);
			
		}
		
		pop();

		
		if (owner) {  // Only the room "owner" updates the points for the whole room. Using systems like an "owner" is good for stuff that must happen just once (not every user should do it).
			pointUpdateCounter++;
			
			if (pointUpdateCounter >= 60) {
				pointUpdateCounter = 0;
				for (Token t : tokens) {
					if (t.teamOwner != -1) {
						points.set(t.teamOwner, points.get(t.teamOwner)+1);
					}
				}
				pointsRef.setValueAsync(points);  // You can store stuff directly in database keys if it's really really simple.
				roomRef.child("lastModified").setValueAsync(Timestamp.now().getSeconds());  // Used to remove old rooms
			}
		}
		
	}
	
	public void mousePressed() {
		for (Token t : tokens) {
			Point unscaledMouse = actualToAssumedCoordinates(new Point(mouseX, mouseY));
			if (t.contains(unscaledMouse.x, unscaledMouse.y) && t.heldBy == null) {
				draggingOffset = new Point(unscaledMouse.x - t.x, unscaledMouse.y - t.y);
				t.heldBy = me.uniqueID;
				t.teamOwner = -1;
				//tokensRef.setValueAsync(tokens);
				tokensRef.child(t.uniqueID).setValueAsync(t);
				break;
			}
		}
	}
	
	public void mouseReleased() {
		for (Token t : tokens) {
			if (t.heldBy != null && t.heldBy.equals(me.uniqueID)) {
				t.heldBy = null;
				t.teamOwner = me.team;
				//tokensRef.setValueAsync(tokens);
				tokensRef.child(t.uniqueID).setValueAsync(t);
				break;
			}
		}
	}
	
	public void mouseDragged() {
		for (Token t : tokens) {
			if (t.heldBy != null && t.heldBy.equals(me.uniqueID)) {
				Point unscaledMouse = actualToAssumedCoordinates(new Point(mouseX, mouseY));
				int oldX = t.x;
				int oldY = t.y;
				t.x = unscaledMouse.x - draggingOffset.x;
				t.y = unscaledMouse.y - draggingOffset.y;
				
				boolean hit = false;
				if (!WINDOW_BOUNDS.contains(t.toRectangle())) {
					t.x = oldX;
					t.y = oldY;
					hit = true;
				} else {
					
					for (Token t2 : tokens) {
						if (t == t2)
							continue;
						if (t.intersects(t2)) {
							t.x = oldX;
							t.y = oldY;
							hit = true;
							break;
						}
					}
					
				}
				
				if (!hit && !currentlySending) {
					currentlySending = true;
					
					tokensRef.child(t.uniqueID).setValue(tokens, new CompletionListener() {

						@Override
						public void onComplete(DatabaseError arg0, DatabaseReference arg1) {
							currentlySending = false;
						}

					});
				}
				
				break;
			}
		}
	}
	
	
	public Point actualToAssumedCoordinates(Point p) {
		return new Point((int)(p.x / scaleX), (int)(p.y / scaleY));
	}
	
	
	
	
	
	/**
	 * 
	 * Handles all changes to the "users" database reference. This part of the database contains information about the players currently in this room.
	 * Because Firebase uses a separate thread than most other processes we're using (both Swing and Processing),
	 * we need to have a strategy for ensuring that code is executed somewhere besides these methods.
	 * 
	 * @author john_shelby
	 *
	 */
	public class UserChangeListener implements ChildEventListener {

		private ConcurrentLinkedQueue<Runnable> tasks;
		
		public UserChangeListener() {  // This threading strategy will work with Processing programs. Just use this code inside your PApplet.
			tasks = new ConcurrentLinkedQueue<Runnable>();
			
			DrawingSurface.this.registerMethod("post", this);
		}
		
		
		public void post() {
			while (!tasks.isEmpty()) {
				Runnable r = tasks.remove();
				r.run();
			}
		}
		
		@Override
		public void onCancelled(DatabaseError arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChildAdded(DataSnapshot arg0, String arg1) {
			tasks.add(new Runnable() {

				@Override
				public void run() {
					
					boolean found = false;
					for (Player p : players) {
						if (p.uniqueID.equals(arg0.getKey()))
							found = true;
					}
					if (!found) {
						Player p = arg0.getValue(Player.class);
						players.add(p);
					}
					
				}
				
			});
		}

		@Override
		public void onChildChanged(DataSnapshot arg0, String arg1) {
			
			
		}

		@Override
		public void onChildMoved(DataSnapshot arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChildRemoved(DataSnapshot arg0) {
			tasks.add(new Runnable() {

				@Override
				public void run() {
					if (me.uniqueID.equals(arg0.getKey())) {
						System.exit(0);   // Exit the program if our player gets removed from the database (owner is gone)
						return;
					}
						
					
					for (int i = 0; i < players.size(); i++) {
						if (players.get(i).uniqueID.equals(arg0.getKey())) {
							players.remove(i);
							break;
						}
					}
				}
				
			});
			
		}
		
	}
	
	
	
	
	
	
	/**
	 * 
	 * This listener handles all changes to the token rectangles. You can (and should) have different listeners for
	 * different types of data.
	 * 
	 * @author john_shelby
	 *
	 */
	public class TokenChangeListener implements ChildEventListener {

		private ConcurrentLinkedQueue<Runnable> tasks;
		
		public TokenChangeListener() {  // This threading strategy will work with Processing programs. Just use this code inside your PApplet.
			tasks = new ConcurrentLinkedQueue<Runnable>();
			
			DrawingSurface.this.registerMethod("post", this);
		}
		
		
		public void post() {
			while (!tasks.isEmpty()) {
				Runnable r = tasks.remove();
				r.run();
			}
		}
		
		@Override
		public void onCancelled(DatabaseError arg0) {
			// TODO Auto-generated method stub
			
		}

		public void updateToken(Token t) {
			boolean hit = false;
			for (int i = 0; i < tokens.size(); i++) {
				if (tokens.get(i).uniqueID.equals(t.uniqueID)) {
					tokens.set(i, t);
					hit = true;
					break;
				}
			}
			if (!hit)
				tokens.add(t);
		}

		@Override
		public void onChildAdded(DataSnapshot arg0, String arg1) {
			tasks.add(new Runnable() {

				@Override
				public void run() {
					
					Token t = arg0.getValue(Token.class);
					updateToken(t);
					
				}
				
			});
		}


		@Override
		public void onChildChanged(DataSnapshot arg0, String arg1) {
			tasks.add(new Runnable() {

				@Override
				public void run() {
					
					Token t = arg0.getValue(Token.class);
					updateToken(t);
					
				}
				
			});
			
		}


		@Override
		public void onChildMoved(DataSnapshot arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onChildRemoved(DataSnapshot arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	
	
	
	
	/**
	 * 
	 * This listener handles updates to the points count. Unlike other pieces of data, only the "owner"
	 * of the room updates the points.
	 * 
	 * @author john_shelby
	 *
	 */
	public class PointsChangeListener implements ValueEventListener {

		private ConcurrentLinkedQueue<Runnable> tasks;
		
		public PointsChangeListener() {  // This threading strategy will work with Processing programs. Just use this code inside your PApplet.
			tasks = new ConcurrentLinkedQueue<Runnable>();
			
			DrawingSurface.this.registerMethod("post", this);
		}
		
		
		public void post() {
			while (!tasks.isEmpty()) {
				Runnable r = tasks.remove();
				r.run();
			}
		}
		
		@Override
		public void onCancelled(DatabaseError arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDataChange(DataSnapshot arg0) {
			tasks.add(new Runnable() {

				@Override
				public void run() {
					
					GenericTypeIndicator<ArrayList<Integer>> type = new GenericTypeIndicator<ArrayList<Integer>>() {};
					points = arg0.getValue(type);
					
				}
				
			});
		}
		
	}
	
	
	
	


}
