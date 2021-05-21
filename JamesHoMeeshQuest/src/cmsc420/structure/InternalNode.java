package cmsc420.structure;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class InternalNode extends Node{
	
	public Rectangle2D.Float[] quadrants;
	
	public Point2D.Float centerPoint;
	public Point2D.Float[] childrenCPs;
	
	public int w;
	public int h;
	
	public Node[] children;

	public InternalNode(Point2D.Float centerPoint, int w, int h) {
		super(Node.INTERNAL);
		
		Rectangle2D.Float[] quadrants = new Rectangle2D.Float[4];
		
		this.centerPoint = centerPoint;
		
		this.w = w;
		this.h = h;

		childrenCPs = new Point2D.Float[4];
		childrenCPs[0] = new Point2D.Float(centerPoint.x, centerPoint.y + h/2);
		
		childrenCPs[1] = new Point2D.Float(centerPoint.x + w/2, centerPoint.y + h/2);
		
		childrenCPs[2] = new Point2D.Float(centerPoint.x, centerPoint.y);
		
		childrenCPs[3] = new Point2D.Float(centerPoint.x + w/2, centerPoint.y);

		//inputting children nodes (quadrants)
		
		for(int i = 0; i < 4; i++) {
			quadrants[i] = new Rectangle2D.Float(childrenCPs[i].x, childrenCPs[i].y, w/2, h/2);
		}
			
		
		Node[] children = new Node[4];

		for (int j = 0; j < 4; j++) {
			children[j] = EmptyNode.emptyNode;
		}

	}
	
	public Rectangle2D.Float getRegion() {
		return new Rectangle2D.Float(centerPoint.x, centerPoint.y, w, h);
	}

	public Node getChild(int quadrant) {
		return children[quadrant];
	}
	
	public int numEmptyNodes() {
		int ret = 0;
		
		for (int i = 0; i < children.length; i++) {
			if (children[i] == EmptyNode.emptyNode) {
				ret++;
			}
		}
		
		return ret;
	}
	
	public int numLeaves() {
		int ret = 0;
		
		for (int i = 0; i < children.length; i++) {
			if (children[i].getNodeType() == LEAF) {
				ret++;
			}
		}
		
		return ret;
	}
	
	public static boolean intersects(Point2D point, Rectangle2D rect) {
		return (point.getX() >= rect.getMinX() && point.getX() < rect.getMaxX()
				&& point.getY() >= rect.getMinY() && point.getY() < rect
				.getMaxY());
	}

	
	public Node add(City city, Point2D.Float centerPoint, int w, int h) {
		
		Point2D cityCoords = city.toPoint2D();
		
		for (int i = 0; i < 4; i++) {
			if (intersects(cityCoords, quadrants[i])) {
				children[i] = children[i].add(city, childrenCPs[i], w/2, h/2);
				break;
			}
		}
		return this;
	}

	public Node remove(City city, Point2D.Float centerPoint, int w,	int h) {
		
		Point2D cityCoords = city.toPoint2D();
		
		for (int i = 0; i < 4; i++) {
			if (intersects(cityCoords, quadrants[i])) {
				children[i] = children[i].remove(city, childrenCPs[i], w/2, h/2);
			}
		}

		/*** deciding to remove cross from display ***/
		if (numEmptyNodes() == 4) {
			
			return EmptyNode.emptyNode;

			
			
		}else if (numLeaves() == 1 && numEmptyNodes() == 3) {
					
			for (int j = 0; j < children.length; j++) {
				if (children[j].getNodeType() == Node.LEAF) {
					return children[j];
				}
			}			
			

		} else {
			return this;
		}
		return null;
	}
}

	

	