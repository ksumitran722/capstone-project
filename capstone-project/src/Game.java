import processing.core.PApplet;
import processing.core.PImage;


public class Game extends PApplet{

    PImage photo;

    Button test;


    public void settings()
    {
        size(800, 600);
    }

    public void setup()
    {
        test = new Button(width/2, height/2+75, 200, 75, 28);

    }

    public void draw()
    {
        rectMode(CENTER);
        test.draw(this);
    }

    public static void main (String[] args)
    {
        PApplet.main("Game");
    }
}