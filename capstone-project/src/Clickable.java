import processing.core.PApplet;

import java.awt.*;

public class Clickable {
    private float rectX, rectY, width, height, contour;

    private ClickListener listener;

    public Clickable(float rectX, float rectY, float width, float height, float contour)
    {
        this.rectX = rectX;
        this.rectY = rectY;
        this.width = width;
        this.height = height;
        this.contour = contour;
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
    public void handleMouseClick(PApplet marker)
    {
        if(checkMouseLocation(marker) == 1 && listener != null)
        {
            listener.OnClick(this);
        }
    }
    public void setOnClickListener(ClickListener listener)
    {
        this.listener = listener;
    }


    public float getRectX() { return rectX; }
    public float getRectY() { return rectY; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getContour() { return contour;}

    public void setRectX(float newRectX) {
        this.rectX = newRectX;
    }

    public void setRectY(float newRectY) {
        this.rectY = newRectY;
    }

    public void setWidth(float newWidth) {
        this.width = newWidth;
    }

    public void setHeight(float newHeight) {
        this.height = newHeight;
    }

    public void setContour(float newContour) {
        this.contour = newContour;
    }
}
