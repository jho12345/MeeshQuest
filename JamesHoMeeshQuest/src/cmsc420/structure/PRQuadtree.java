package cmsc420.structure;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
//import java
import java.util.HashSet;

import cmsc420.structure.City;
import cmsc420.exception.*;
import cmsc420.geom.Circle2D;

public class PRQuadtree {

	protected Node root;	
	protected Point2D.Float centerPoint;
	public int spWidth;
	public int spHeight;
	
	public HashSet<String> cities;
	
	public PRQuadtree() {
		root = EmptyNode.emptyNode;
		centerPoint = new Point2D.Float(0, 0);
		cities = new HashSet<String>();
	}
	
	public Node getRoot() {
		return this.root;
	}
	
	public Point2D.Float getCenterPoint() {
		return centerPoint;
	}
	
	public int getSpWidth() {
		return spWidth;
	}
	
	public int getSpHeight() {
		return spHeight;
	}
	
	public boolean intersects(Circle2D circle, Rectangle2D.Double rectangle) {
		
		Rectangle2D.Double partition = new Rectangle2D.Double(rectangle.getX() - circle.getCenterX(), 
				rectangle.getY() - circle.getCenterY(), rectangle.getWidth(), rectangle.getHeight());
	
		double radius = circle.getRadius();
		double rSquared = circle.getRadius() * circle.getRadius();
		
		//Quadrants 2 & 3
		double maxXSquared = partition.getMaxX() * partition.getMaxX();
		
		//Quadrants 1 & 4
		double minXSquared = partition.getMinX() * partition.getMinX();
		
		//Quadrants 3 & 4
		double maxYSquared = partition.getMaxY() * partition.getMaxY();
		
		//Quadrants 1 & 2
		double minYSquared = partition.getMinY() * partition.getMinY();
		
		
		/*** In Quadrants 1-4 ***/
		//Quadrant 1
		if (partition.getMinX() > 0 && partition.getMinY() > 0) {
			return ((minXSquared + minYSquared) <= rSquared ? true : false);
		
		//Quadrant 2
		}else if (partition.getMaxX() < 0 && partition.getMinY() > 0) {
			return ((maxXSquared + minYSquared) < rSquared ? true : false);
			
		//Quadrant 3
		}else if(partition.getMaxX() < 0 && partition.getMaxY() < 0) {
			return ((maxXSquared + maxYSquared) < rSquared ? true : false);
			
		//Quadrant 4
		}else if (partition.getMinY() > 0 && partition.getMaxY() < 0) {
			return ((minXSquared + maxYSquared) < rSquared ? true : false);
		
		/*** Directly west, east, south, or north ***/
		//West
		}else if (partition.getMaxX() < 0 && partition.getMinY() == 0) {
			return (((Math.abs(partition.getMaxX()) < radius ? true : false)));
			
		//East
		}else if (partition.getMinX() > 0 && partition.getMinY() == 0) {
			return (partition.getMinX() <= radius ? true : false);
			
		//South
		}else if (partition.getMinX() <= 0 && partition.getMaxY() < 0) {
			return (((Math.abs(partition.getMaxY()) < radius ? true : false)));
			
		//North
		}else if (partition.getMinX() <= 0 && partition.getMinY() > 0) {
			return ((partition.getMinY() <= radius) ? true : false);
			
		/*** within the rectangle ***/
		}else {
			return true;
		}
			
	
	}
	
	public boolean contains(String node) {
		return cities.contains(node);

	}
	
	public void add(City city) throws CityAlreadyMappedException, CityOutOfBoundsException {
		if (cities.contains(city.getName())) {
			throw new CityAlreadyMappedException();
		}
		
		int x = (int)city.getX();
		int y = (int)city.getY();	
		if (x < centerPoint.x || y < centerPoint.y ||  x >= spWidth || y >= spHeight) {
			throw new CityOutOfBoundsException();
		}
		
		cities.add(city.getName());
		root = root.add(city, centerPoint, spWidth, spHeight);
	}
	
	public boolean remove(City city) {
		boolean success = cities.contains(city.getName());
		if (success) {
			cities.remove(city.getName());
			root = root.remove(city, centerPoint, spWidth, spHeight);
		}
		return success;
	}
	
	public void delete(City city) {
		if (cities.contains(city.getName())) {
			root = root.remove(city, centerPoint, spWidth, spHeight);
			cities.remove(city.getName());
		}
	}
	
	public void clear() {
		cities.clear();
		root = EmptyNode.emptyNode;
	}
}
