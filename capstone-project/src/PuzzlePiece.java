import processing.core.PApplet;
import processing.core.PImage;

public class PuzzlePiece extends Clickable{
    private PImage puzzleImage;
    private boolean isBeingDragged = false;

    public PuzzlePiece(float rectX, float rectY, float width, float height, float contour, PImage source, int portionX, int portionY, int portionWidth, int portionHeight)
    {
        super(rectX,rectY,width,height,contour);
        puzzleImage = source.get(portionX,portionY,portionWidth,portionHeight);
    }

    public void draw(PApplet marker)
    {
        marker.pushMatrix();
        marker.imageMode(marker.CENTER);
        if(isBeingDragged)
        {
            setRectX(marker.mouseX);
            setRectY(marker.mouseY);
        }
        marker.image(puzzleImage,getRectX(),getRectY(),getWidth(),getHeight());
        marker.popMatrix();
    }

    public void setIsBeingDragged(boolean isBeingDragged)
    {
        this.isBeingDragged = isBeingDragged;
    }
    public boolean getIsBeingDragged()
    {
        return isBeingDragged;
    }


}
