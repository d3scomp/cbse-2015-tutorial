package cz.cuni.mff.d3s.roadtrain.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

class Node {
	public String id;
	public double x;
	public double y;

	public Node(String id, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public Node(Attributes atts) {
		id = atts.getValue("id");
		x = Double.valueOf(atts.getValue("x"));
		y = Double.valueOf(atts.getValue("y"));
	}
	
	public void write(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("", "node", "");
		
		writer.writeAttribute("", "", "id", id);
		writer.writeAttribute("", "", "x", String.valueOf(x));
		writer.writeAttribute("", "", "y", String.valueOf(y));
				
		writer.writeEndElement();
	}
}

class Link {
	public String id;
	public String from;
	public String to;
	public double length;
	public double freespeed;
	public String capacity;
	public String permlanes;
	public String oneway;
	public String modes;

	public Link(String id, String from, String to, Double length, Double freespeed, String capacity, String permlanes,
			String oneway, String modes) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.length = length;
		this.freespeed = freespeed;
		this.capacity = capacity;
		this.permlanes = permlanes;
		this.oneway = oneway;
		this.modes = modes;
	}
	
	public Link(Attributes atts) {
		id = atts.getValue("id");
		from = atts.getValue("from");
		to = atts.getValue("to");
		length = Double.valueOf(atts.getValue("length"));
		freespeed = Double.valueOf(atts.getValue("freespeed"));
		capacity = atts.getValue("capacity");
		permlanes = atts.getValue("permlanes");
		oneway = atts.getValue("oneway");
		modes = atts.getValue("modes");
	}
	
	public void write(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("", "link", "");
		
		writer.writeAttribute("", "", "id", id);
		writer.writeAttribute("", "", "from", from);
		writer.writeAttribute("", "", "to", to);
		writer.writeAttribute("", "", "length", String.valueOf(length));
		writer.writeAttribute("", "", "freespeed", String.valueOf(freespeed));
		writer.writeAttribute("", "", "capacity", capacity);
		writer.writeAttribute("", "", "permlanes", permlanes);
		writer.writeAttribute("", "", "oneway", oneway);
		writer.writeAttribute("", "", "modes", modes);
		
		writer.writeEndElement();
	}
}

public class LinkDivider {
	public static void main(String[] args) throws Exception {
		LinkDivider divider = new LinkDivider("input\\prague.xml", "input\\prague-divided.xml", 50);
		divider.process();
	}

	protected Map<String, Node> nodes = new HashMap<String, Node>();
	protected Collection<Link> links = new HashSet<Link>();
	protected Collection<Link> outLinks = new HashSet<Link>();
	
	private final String in;
	private final String out;
	private final double maxLength;
	
	public LinkDivider(String in, String out, double maxLength) {
		this.in = in;
		this.out = out;
		this.maxLength = maxLength;
	}

	class MapProcessor implements ContentHandler {
		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void endDocument() throws SAXException {}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			switch (qName) {
			case "network":
				break;
			case "nodes":
				break;
			case "node":
				Node node = new Node(atts);
				nodes.put(node.id, node);
				break;
			case "link":
				links.add(new Link(atts));
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void skippedEntity(String name) throws SAXException {}
	}

	void process() throws Exception {
		// Setup processor
		MapProcessor processor = new MapProcessor();

		// Parse input
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setValidating(false);
		SAXParser saxParser = saxParserFactory.newSAXParser();
		XMLReader reader = saxParser.getXMLReader();

		reader.setContentHandler(processor);
		reader.parse(in);
		
		
		divide();
		
		write();
	}
	
	private void divide() {
		for(Link link: links) {
			if(link.length < maxLength) {
				outLinks.add(link);
			} else {
				outLinks.addAll(divideLink(link));
			}
		}
	}
	
	private Collection<Link> divideLink(Link link) {
		Set<Link> ret = new HashSet<Link>();
		
		final int parts = (int) (1 + link.length / maxLength);
		final double step = link.length / parts;
		final Node from = nodes.get(link.from);
		final Node to = nodes.get(link.to);
		final double stepX = (to.x - from.x) / parts;
		final double stepY = (to.y - from.y) / parts;
	
		Node last = from;
		
		for(int i = 1; i <= parts; ++i) {
			Node node = null;
			if(i == parts) {
				node = to;
			} else {
				node = new Node(
						String.format("%s_%d", from.id, i),
						from.x + stepX * i,
						from.y + stepY * i);
			}
			
			Link newLink = new Link(
					String.format("%s_%d", link.id, i),
					last.id,
					node.id,
					step,
					link.freespeed,
					link.capacity,
					link.permlanes,
					link.oneway,
					link.modes);
			
			nodes.put(node.id, node);
			outLinks.add(newLink);
			
			last = node;
		}
		
		return ret;
	}
	
	private void write() throws Exception {
		// Set output
		OutputStream outputStream = new FileOutputStream(new File(out));
		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(xmlFactory.createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8")));
	
		writer.writeStartDocument();
		writer.writeDTD("<!DOCTYPE network SYSTEM \"http://www.matsim.org/files/dtd/network_v1.dtd\">");
		
		writer.writeStartElement("network");
		writer.writeAttribute("name", "divided");
		
		writer.writeStartElement("nodes");
		for(Node node: nodes.values()) {
			node.write(writer);
		}
		writer.writeEndElement();
		
		writer.writeStartElement("links");
		for(Link link: outLinks) {
			link.write(writer);
		}
		writer.writeEndElement();
		
		writer.writeEndElement();
		
		writer.writeEndDocument();

		writer.close();
	}
}
