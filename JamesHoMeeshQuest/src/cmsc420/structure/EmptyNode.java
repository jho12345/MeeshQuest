package cmsc420.structure;

import java.awt.geom.Point2D;
import cmsc420.structure.City;

import java.awt.geom.Point2D;
import cmsc420.structure.City;

public class EmptyNode extends Node {

	public static EmptyNode emptyNode = new EmptyNode();
	
	public EmptyNode() {
		super(Node.EMPTY);
	}
	
	public Node add(City city, Point2D.Float centerPoint, int w, int h) {
		Node leaf = new Leaf();
		leaf.add(city, centerPoint, w, h);
		return leaf;
	}
	
	public Node remove(City city, Point2D.Float centerPoint, int w, int h) {
		return null;
	}
}
