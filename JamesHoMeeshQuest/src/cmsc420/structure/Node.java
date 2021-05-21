package cmsc420.structure;

import java.awt.geom.Point2D;
import cmsc420.structure.City;

public abstract class Node {
	
	static final int EMPTY = 0;
	static final int LEAF = 1;
	static final int INTERNAL = 2;
	
	final int nodeType;
	
	public Node(final int nodeType) {
		this.nodeType = nodeType;
	}
	
	public int getNodeType() {
		return nodeType;
	}
	
	public abstract Node add(City city, Point2D.Float centerPoint, int w, int h);

	public abstract Node remove(City city, Point2D.Float centerPoint, int w, int h);
}


