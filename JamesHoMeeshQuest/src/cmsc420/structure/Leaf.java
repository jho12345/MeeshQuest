package cmsc420.structure;

import java.awt.geom.Point2D;
import cmsc420.structure.City;

public class Leaf extends Node {
	
	City city;
	
	public Leaf() {
		super(Node.LEAF);
	}
	
	public City getCity() {
		return city;
	}
	
	public Node add(City toAdd, Point2D.Float centerPoint, int w, int h) {
		if (city == null) {
			city = toAdd;
			
			return this;
			
		}else {
			InternalNode iNode;
			iNode = new InternalNode(centerPoint, w, h);
			
			iNode.add(city, centerPoint, w, h);
			iNode.add(toAdd, centerPoint, w, h);
			
			return iNode;
		}
	}
	
	public Node remove(City toRemove, Point2D.Float centerPoint, int w, int h) {
		if (this.city != city) {
			throw new IllegalArgumentException();
			
		}else {
			this.city = null;
			return EmptyNode.emptyNode;
			
		}
	}

}
