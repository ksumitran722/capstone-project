package demo2;
import java.awt.Color;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Drawing {

	private PGraphics drawing;
	
	public Drawing (int width, int height, PApplet surface) {
		drawing = surface.createGraphics(width,  height);
		clear();
	}
	
	
	public void addDotSet(ArrayList<Dot> dots, Color c) {
		drawing.beginDraw();
		drawing.ellipseMode(PConstants.CENTER);
		drawing.fill(c.getRGB());
		for (Dot d : dots) {
			drawing.ellipse(d.x, d.y, 5, 5);
		}
		drawing.endDraw();
	}
	
	
	public void clear() {
		drawing.beginDraw();
		drawing.background(255);
		drawing.endDraw();
	}
	
	
	public void draw(PApplet surface) {
		surface.image(drawing, 0, 0);
	}
	
	
}
