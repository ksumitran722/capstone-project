import processing.core.PApplet;
import processing.core.PImage;


public class Game extends PApplet{

    PImage photo;

    Server game = new Server();

    public void settings()
    {
        size(800, 600);
    }

    public void setup()
    {
        photo = loadImage("ludwig.jpg");
        photo.resize(400,400);
        game.run();

    }

    public void draw()
    {
        image(photo, width/4, height/4);
    }

    public static void main (String[] args)
    {
        PApplet.main("Game");
    }
}