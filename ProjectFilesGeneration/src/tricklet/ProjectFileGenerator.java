package tricklet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A generator of project files
 * 
 * @author Daniel Couto-Vale <daniel.couto-vale@rwth-aachen.de>
 */
public class ProjectFileGenerator {

	/**
	 * The directory of the experiment
	 */
	private final File directory;

	/**
	 * The document describing the experiment
	 */
	private final Document document;

	/**
	 * The text map
	 */
	private Map<String, Element> textMap;

	/**
	 * The text set map
	 */
	private Map<String, Element> textListMap;

	/**
	 * The setup map
	 */
	private Map<String, Element> setupMap;

	/**
	 * The participant
	 */
	private Map<String, Element> participantMap;

	/**
	 * Constructor
	 * 
	 * @param directory the directory of the experiment
	 * @param document the document describing the experiment
	 * @throws XPathExpressionException 
	 */
	public ProjectFileGenerator(File directory, Document document) throws XPathExpressionException {
		this.directory = directory;
		this.document = document;
		build();
	}

	private final void build() throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		textMap = buildMap(xpath, "/experiment/texts/text");
		textListMap = buildMap(xpath, "/experiment/text-lists/text-list");
		setupMap = buildMap(xpath, "/experiment/setups/setup");
		participantMap = buildMap(xpath, "/experiment/participants/participant");
	}

	private final Map<String, Element> buildMap(XPath xpath, String expression) throws XPathExpressionException {
		XPathExpression xpathExpression = xpath.compile(expression);
		NodeList nodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
		Map<String, Element> map = new HashMap<String, Element>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);
			map.put(element.getAttribute("id"), element);
		}
		return map;
	}

	public final void generate() {
		List<String> participantIdList = new ArrayList<String>(participantMap.keySet());
		Collections.sort(participantIdList);
		for (String participantId : participantIdList) {
			System.out.println(participantId);
		}
	}

}
