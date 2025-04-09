import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import processing.core.PApplet;
import processing.core.PImage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;


public class Game extends PApplet{

    PImage photo;

    Button test;

    PuzzlePiece pz;

    public static void initFirebase()
    {
        try{
            FileInputStream serviceAccount = new FileInputStream("capstone-project/config/capstone-project-b64f0-firebase-adminsdk-fbsvc-6c19c47af5.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://capstone-project-b64f0-default-rtdb.firebaseio.com/")
                    .build();

            FirebaseApp.initializeApp(options);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void settings()
    {
        size(800, 600);
    }

    public void setup()
    {
        test = new Button(width/2, height/2+75, 200, 75, 28);

        photo = loadImage("data/ludwig.jpg");

        pz = new PuzzlePiece(100, 100, 150, 150 , 0 , photo, 0, 0, 100, 100);

        test.setOnClickListener(
                new ClickListener() {
                    @Override
                    public void OnClick(Clickable source) {
                        System.out.println("Hello");
                    }
                }
        );

        pz.setOnClickListener(new ClickListener() {
            @Override
            public void OnClick(Clickable source) {
                pz.setIsBeingDragged(!pz.getIsBeingDragged());
            }
        });

    }

    public void draw()
    {
        background(220); //clear frames
        rectMode(CENTER);
        test.draw(this);
        pz.draw(this);

    }

    public void mouseClicked()
    {
        test.handleMouseClick(this);
        pz.handleMouseClick(this);
    }

    public static void main (String[] args)
    {
        PApplet.main("Game");
    }
}