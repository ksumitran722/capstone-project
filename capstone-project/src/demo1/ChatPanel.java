package demo1;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.*;
import com.google.firebase.database.*;


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
public class ChatPanel extends JPanel implements ActionListener {
	
	
	private JTextField messageField;
	private JTextArea output;
	private String name;
	
	
	
	private DatabaseReference postsRef;
	
	
	
	public ChatPanel(String name) {
		super(new BorderLayout());
		
		this.name = name;
		
		// UI SETUP
		output = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(output);
		add(scrollPane, BorderLayout.CENTER);
		
		messageField = new JTextField(50);
		messageField.addActionListener(this);
		
		JButton goButton = new JButton("POST");
		goButton.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		
		JPanel messagePanel = new JPanel();
		messagePanel.add(new JLabel("Message: "));
		messagePanel.add(messageField);
		
		
		bottomPanel.add(messagePanel);
		bottomPanel.add(goButton);
		JPanel holder = new JPanel();
		holder.add(bottomPanel);
		add(holder, BorderLayout.SOUTH);
		
		
		
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
				postsRef = database.child("posts");

				postsRef.addChildEventListener(new DatabaseChangeListener());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
	
		if (!messageField.getText().trim().isEmpty())
			postsRef.push().setValueAsync(new Post(name, messageField.getText()));
		messageField.setText("");
		
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

				@Override
				public void run() {
					
					Post post = dataSnapshot.getValue(Post.class);

					String text = output.getText();
					text += post + "\n";

					output.setText(text);
					
				}
				
			});
			
			
		}


		@Override
		public void onChildChanged(DataSnapshot arg0, String arg1) {
			// TODO Auto-generated method stub

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




}
