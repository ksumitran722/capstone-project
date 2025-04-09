import processing.core.PApplet;

import java.awt.*;

public class Button extends Clickable{





    public Button(float rectX, float rectY, float width, float height, float contour)
    {
        super(rectX,rectY,width,height,contour);
    }

    public void draw(PApplet marker)
    {
        marker.pushMatrix();
        marker.rectMode(marker.CENTER);
        if(checkMouseLocation(marker) == 1)
        {
            marker.fill(Color.yellow.getRGB());
        }
        else{
            marker.fill(Color.white.getRGB());
        }
        marker.rect(getRectX(), getRectY(), getWidth(), getHeight(), getContour());
        marker.popMatrix();
    }

    public int checkMouseLocation(PApplet marker)
    {
        boolean isHighlighted = marker.mouseX > getRectX() - getWidth() / 2 && marker.mouseX < getRectX() + getWidth() / 2
                && marker.mouseY > getRectY() - getHeight() / 2 && marker.mouseY < getRectY() + getHeight() / 2;

        if(isHighlighted)
        {
            return 1;
        }else{
            return 0;
        }

    }




}
