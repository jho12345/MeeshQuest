/**
 * @(#)Command.java        1.1
 *
 * 2014/09/09
 *
 * @author Ruofei Du, Ben Zoller (University of Maryland, College Park), 2014
 *
 * All rights reserved. Permission is granted for use and modification in CMSC420
 * at the University of Maryland.
 */
package cmsc420.command;

import java.io.IOException;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.exception.*;
import cmsc420.structure.City;
import cmsc420.structure.CityLocationComparator;
import cmsc420.structure.*;



/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 *
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>(new Comparator<String>() {

		
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/**
	 * stores created cities sorted by their locations (used with listCities command)
	 */
	
	protected final HashSet<String> citiesHash = new HashSet<String>();
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());
	protected CanvasPlus canvas;
	public void setCanvas(CanvasPlus canvas) {
		this.canvas = canvas;
	}
	protected final PRQuadtree prQuadtree = new PRQuadtree();
	
	protected final TreeMap<City, Integer> mappedCities = new TreeMap<City, Integer>(new Comparator<City>() {
		
		@Override
		public int compare(City c1, City c2) {
			return c2.getName().compareTo(c1.getName());
		}
		
	});


	/** spatial width and height of the MX Quadtree */
	protected int spatialWidth, spatialHeight;

	/**
	 * Set the DOM Document tree to send the of processed commands to.
	 *
	 * Creates the root results node.
	 *
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 *
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 *
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 *
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 *
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 *
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private void addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 *
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 *
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color);

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode,
					parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	public void processDeleteCity(final Element node) {
		final Element commNode = getCommandNode(node);
		final Element paramNode = results.createElement("parameters");
		
		final String name = processStringAttribute(node, "name", paramNode);
		
		//error checking
		if (citiesByName.containsKey(name)) {
			Element outputNode = results.createElement("output");
			City deleted = citiesByName.get(name);
			
			citiesByName.remove(name);
			citiesByLocation.remove(deleted);
			
			if (prQuadtree.cities.contains(name)) {
				citiesHash.remove(name);
				prQuadtree.remove(deleted);
		
				addCityNode(outputNode, "cityUnmapped", deleted);
			}	
			
			citiesByName.remove(name);
			citiesByLocation.remove(deleted);
			
			addSuccessNode(commNode, paramNode, outputNode);
		} else {
			addErrorNode("cityDoesNotExist", commNode, paramNode);
		}
	}

	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 *
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		citiesHash.clear();

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 *
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");

			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 *
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("radius", Integer
				.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 *
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}
	
	public void processMapCity (final Element node) {
		
		final Element commNode = getCommandNode(node);
		final Element paramNode =  results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		final String name = processStringAttribute(node, "name", paramNode);
		
		//process errors
		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commNode, paramNode);
		
		} else if (prQuadtree.contains(name)) {
			addErrorNode("cityAlreadyMapped", commNode, paramNode);
		
		} else {
			
		//no errors
			City city = citiesByName.get(name);
			
			try {
				
				prQuadtree.add(city);
				citiesHash.add(name);
				
				canvas.addPoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
				
				addSuccessNode(commNode, paramNode, outputNode);
				
			}catch (CityAlreadyMappedException e) {
				addErrorNode("cityAlreadyMapped", commNode, paramNode);
				
			}catch (CityOutOfBoundsException e) {
				addErrorNode("cityOutOfBounds", commNode, paramNode);
			}
			
		}
		
		
		
	}
	
	public void processUnmapCity(Element node) {
		
		final Element commNode = getCommandNode(node);
		final Element paramNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		final String name = processStringAttribute(node, "name", paramNode);
		
		//error checking
		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commNode, paramNode);
			
		} else if (!prQuadtree.cities.contains(name)) {
			addErrorNode("cityNotMapped", commNode, paramNode);
			
		} else {
			
			City city = citiesByName.get(name);
			
			prQuadtree.remove(citiesByName.get(name));
			
			canvas.removePoint(city.getName(),  city.getX(), city.getY(), Color.BLACK);
				
			addSuccessNode(commNode, paramNode, outputNode);
		}
	}
		
	public void processSaveMap(final Element node) {
	
		final Element commNode = getCommandNode(node);
		final Element paramNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		final String name = processStringAttribute(node, "name", paramNode);
		
		try {
			canvas.save(name);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		addSuccessNode(commNode, paramNode, outputNode);
	}
	
	public void processPrintPRQuadtree(final Element node) {
		
		final Element commNode = getCommandNode(node);
		final Element paramNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");
		
		if (prQuadtree.getRoot() == EmptyNode.emptyNode) {
			addErrorNode("emptyMap", commNode, paramNode);
			
		} else {
			Element qtNode = results.createElement("prquadtree");
			processPrintPRQuadtreeAux(prQuadtree.getRoot(), qtNode);
			outputNode.appendChild(qtNode);
			addSuccessNode(commNode, paramNode, outputNode);
		}
	}
	
	public void processPrintPRQuadtreeAux(final Node curr, final Element xml) {
		//empty tree
		if (curr.getNodeType() == 0) {
			Element whiteNode = results.createElement("white");
			xml.appendChild(whiteNode);
			
		//leaf
		} else if (curr.getNodeType() == 1) {
			Element blackNode = results.createElement("black");
			Leaf leaf = (Leaf) curr;
			
			blackNode.setAttribute("name", leaf.getCity().getName());
			blackNode.setAttribute("x", Integer.toString((int) leaf.getCity().getX()));
			blackNode.setAttribute("y", Integer.toString((int) leaf.getCity().getY()));
			
			xml.appendChild(blackNode);
			
		//internal node
		} else if (curr.getNodeType() == 2) {
			Element grayNode = results.createElement("gray");
			InternalNode internalNode = (InternalNode) curr;
			
			grayNode.setAttribute("x", Integer.toString((int) (internalNode.centerPoint.x + internalNode.w/2)));
			grayNode.setAttribute("y", Integer.toString((int) (internalNode.centerPoint.y + internalNode.h/2)));
			
			for (int i = 0; i < 4; i++) {
				processPrintPRQuadtreeAux(internalNode.getChild(i), grayNode);
			}
			xml.appendChild(grayNode);
		}
	}
	
	

}

