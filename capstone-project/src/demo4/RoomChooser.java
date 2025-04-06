package demo4;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;

import com.google.firebase.database.FirebaseDatabase;

import processing.core.PApplet;

public class RoomChooser extends JPanel
{
	private JFrame theWindow;
	
	private JList<String> roomList;
	private DefaultListModel<String> model;
	
	private JTextArea infoArea;

	private JButton connectButton;
	private JButton newRoomButton;
	
	
	private DatabaseReference postsRef;
	private DatabaseChangeListener databaseListener;
	
	private static final int ROOM_TIMEOUT = 180;  // 3 minutes
	
	
	
	
	public RoomChooser() {
		
		model = new DefaultListModel<String>();
		
		ActionHandler actionEventHandler = new ActionHandler();
		
		setLayout(new BorderLayout());
		JPanel middle = new JPanel();
		middle.setLayout(new GridLayout(1, 2));
		
		JPanel roomPanel = new JPanel();
		roomPanel.setLayout(new BorderLayout());
		roomList = new JList<String>();
		roomList.setModel(model);
		roomList.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		roomList.addListSelectionListener(new ListSelectionHandler());
		roomPanel.add(roomList,BorderLayout.CENTER);
		JLabel ah = new JLabel("Available Rooms");
		ah.setHorizontalAlignment(JLabel.CENTER);
		roomPanel.add(ah,BorderLayout.NORTH);
		middle.add(roomPanel);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoArea = new JTextArea();
		infoArea.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		infoArea.setEditable(false);
		infoPanel.add(infoArea,BorderLayout.CENTER);
		JLabel ri = new JLabel("Room Info");
		ri.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(ri,BorderLayout.NORTH);
		middle.add(infoPanel);
		
		add(middle, BorderLayout.CENTER);
		
		JPanel ePanel = new JPanel();
		ePanel.setLayout(new GridLayout(1,5,15,15));
		newRoomButton = new JButton("<html><center>Create<br>A Room</center></html>");
		newRoomButton.addActionListener(actionEventHandler);
		connectButton = new JButton("<html><center>Join<br>Room</center></html>");
		connectButton.addActionListener(actionEventHandler);

		
		ePanel.add(newRoomButton);
		ePanel.add(connectButton);
		
		add(ePanel,BorderLayout.SOUTH);
		
		databaseListener = new DatabaseChangeListener();

		// DATABASE SETUP
		FileInputStream refreshToken;
		try {

			refreshToken = new FileInputStream("APCSDemoKey.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(refreshToken))
					.setDatabaseUrl("https://apcsdemo-ea59b.firebaseio.com/")
					.build();

			FirebaseApp.initializeApp(options);
			DatabaseReference database = FirebaseDatabase.getInstance().getReference();
			postsRef = database.child("battle");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public void show() {
		theWindow = new JFrame();
		theWindow.add(this);
		theWindow.setBounds(0, 0, 800, 600);
		theWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theWindow.setVisible(true);
		postsRef.addChildEventListener(databaseListener);
	}
	
	public void hide() {
		theWindow.setVisible(false);
		theWindow.dispose();
		theWindow = null;
		postsRef.removeEventListener(databaseListener);
	}
	
	
	
	/**
	 * This is a weird thing for a client to do, but it prevents us from having to have server-side
	 * code.
	 * 
	 * This method removes users and rooms if they haven't been updated with new data in a while. Sometimes
	 * this happens if a user crashes or force-kills, causing the database to still have stale entries.
	 * 
	 * 
	 */
	public static boolean cleanRoom(DataSnapshot room) {
		Timestamp now = Timestamp.now();
		
		long lastModifiedRoom = room.child("lastModified").getValue(Long.class);
		
		if (now.getSeconds() - lastModifiedRoom > ROOM_TIMEOUT) {
			room.getRef().removeValueAsync();
			return true;
		}
		return false;
		
	}
	
	
	public void selectRoom(String name, boolean owner) {

		postsRef.orderByChild("name").equalTo(name).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snap) {
				
				if (!snap.hasChildren())
					return;

				DataSnapshot room = snap.getChildren().iterator().next();
				
				int max = room.child("maxPlayers").getValue(Integer.class);
				int players = (int)room.child("users").getChildrenCount();
				
				if (max > 0 && players >= max) {
					System.out.println(players + " / " + max);
					return;
				}
				
				
				
				DrawingSurface drawing = new DrawingSurface(room.getRef(), owner);
				PApplet.runSketch(new String[]{""}, drawing);
				drawing.windowResizable(true);
				
				hide();
			}

			@Override
			public void onCancelled(DatabaseError arg0) {
			}
		});
		
		
		
	}

	

	/**
	 * 
	 * Handles all changes to the database reference. Because Firebase uses a separate thread than most other processes we're using (both Swing and Processing),
	 * we need to have a strategy for ensuring that code is executed somewhere besides these methods.
	 * 
	 * @author john_shelby
	 *
	 */
	class DatabaseChangeListener implements ChildEventListener {


		@Override
		public void onCancelled(DatabaseError arg0) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String arg1) {
			
			SwingUtilities.invokeLater(new Runnable() {  // This threading strategy will work with Swing programs. Just put whatever code you want inside of one of these "runnable" wrappers.

				public void run() {
					if (!cleanRoom(dataSnapshot)) {
						String name = dataSnapshot.child("name").getValue(String.class);
						model.add(0,name);
					}
					
				}
				
			});
			
			
		}


		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String arg1) {
			
		}


		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String arg1) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {
			
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	String name = dataSnapshot.child("name").getValue(String.class);
			    	model.removeElement(name);
			    }
			});

		}

	}


	
	

	private class ActionHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == newRoomButton) {
				
				String roomName = JOptionPane.showInputDialog("Please choose a name for your room:");
				if (roomName == null || roomName.isEmpty()) {
					JOptionPane.showMessageDialog(RoomChooser.this, "Room creation fail - The room needs a name.");
					return;
				}
				
				if (model.contains(roomName)) {
					JOptionPane.showMessageDialog(RoomChooser.this, "Room creation fail - Room name already exists.");
					return;
				}
				
				String maxPlayers = JOptionPane.showInputDialog("What is the max number of players? (Leave blank for no max)");
				int max = -1;
				try {
					max = Integer.parseInt(maxPlayers);
					max = Math.max(1, max);
				} catch (NumberFormatException ex) {}
				
				Map<String,Object> roomData = new HashMap<String,Object>();
				roomData.put("name", roomName);
				roomData.put("maxPlayers", max);
				roomData.put("lastModified", Timestamp.now().getSeconds());
				postsRef.push().updateChildren(roomData, new CompletionListener() {
				
					@Override
					public void onComplete(DatabaseError arg0, DatabaseReference arg1) { // This makes it so we enter the room once it's added to the database.
						
						selectRoom(roomName, true);
					}
					
				});
				
			} else if (source == connectButton) {
				String sel = roomList.getSelectedValue();
				
				if (sel != null)
					selectRoom(sel, false);
				
			}

		}
	}
	
	
	private class ListSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			if (e.getValueIsAdjusting())
				return;
			
			String name = roomList.getSelectedValue();
			
			postsRef.orderByChild("name").equalTo(name).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot snap) {
					
					if (!snap.hasChildren())
						return;
					
					DataSnapshot room = snap.getChildren().iterator().next();

					String info = "Game room: " + name;
					
					int max = room.child("maxPlayers").getValue(Integer.class);
					
					DataSnapshot users = room.child("users");
					
					int players = (int)users.getChildrenCount();
					info += "\n" + players + " players / " + max + " max";
					if (players >= max)
						info += " (FULL)";
					
					info += "\n\nPlayers:\n";
					for (DataSnapshot s : users.getChildren()) {
						info += s.child("username").getValue(String.class) + "\n";
					}
					
					infoArea.setText(info);
				}

				@Override
				public void onCancelled(DatabaseError arg0) {
				}
			});
		}
		
	}

	
	
}
