import processing.core.PApplet;

import java.awt.*;

public class Button {


    float rectX, rectY, width, height, contour;



    public Button(float rectX, float rectY, float width, float height, float contour)
    {
        this.rectX = rectX;
        this.rectY = rectY;
        this.width = width;
        this.height = height;
        this.contour = contour;
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
        marker.rect(rectX, rectY, width, height, contour);
        marker.popMatrix();
    }

    public int checkMouseLocation(PApplet marker)
    {
        boolean isHighlighted = false;

        if(marker.mouseX > rectX - width/2 && marker.mouseX < rectX + width/2
        && marker.mouseY > rectY - height/2 && marker.mouseY < rectY + height/2)
        {
            isHighlighted = true;
        }

        if(isHighlighted)
        {
            return 1;
        }else{
            return 0;
        }

    }





}
