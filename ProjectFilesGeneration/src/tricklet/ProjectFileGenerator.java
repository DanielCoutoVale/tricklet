package tricklet;

import java.io.File;
import java.io.IOException;
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
import org.w3c.dom.Node;
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

	public final void generate() throws IOException {
		List<String> participantIdList = new ArrayList<String>(participantMap.keySet());
		Collections.sort(participantIdList);
		File participantsDirectory = new File(directory, "Participants");
		makeDirectory(participantsDirectory);
		for (String participantId : participantIdList) {
			File participantDirectory = new File(participantsDirectory, participantId);
			makeDirectory(participantDirectory);
			fillDirectory(participantDirectory, participantId);
		}
	}

	private final void makeDirectory(File directory) {
		if (directory.isFile()) {
			directory.delete();
		}
		if (!directory.isDirectory()) {
			directory.mkdir();
		}
	}

	private final void fillDirectory(File directory, String participantId) throws IOException {
		List<Element> textList = getTextList(participantId);
		int i = 1;
		for (Element textElm : textList) {
			String textId = textElm.getAttribute("id");
			String projectName = participantId + "-" + (i++) + "-" + textId + ".project";  
			File projectFile = new File(directory, projectName);
			if (projectFile.exists()) {
				projectFile.delete();
			}
			projectFile.createNewFile();
		}
	}

	private final List<Element> getTextList(String participantId) {
		Element participantElm = participantMap.get(participantId);
		String setupId = participantElm.getAttribute("setup");
		Element setupElm = setupMap.get(setupId);
		List<Element> textList = new ArrayList<Element>();
		for (Element textListElm : getChildValues(setupElm, "text-list", textListMap)) {
			textList.addAll(getChildValues(textListElm, "text", textMap));
		}
		return textList;
	}

	private final List<Element> getChildValues(Element elm, String nodeName, Map<String, Element> map) {
		List<String> indexList = getChildIndices(elm, nodeName);
		List<Element> elmList = new ArrayList<Element>();
		for (String index : indexList) {
			elmList.add(map.get(index));
		}
		return elmList;
	}

	private final List<String> getChildIndices(Element elm, String nodeName) {
		List<Element> childElmList = getChildren(elm, nodeName);
		List<String> childIndexList = new ArrayList<String>();
		for (Element childElm : childElmList) {
			childIndexList.add(childElm.getAttribute("index"));
		}
		return childIndexList;
	}

	private final List<Element> getChildren(Element elm, String nodeName) {
		List<Element> childElmList = new ArrayList<Element>();
		NodeList childNodeList = elm.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode instanceof Element && childNode.getNodeName().equals(nodeName)) {
				childElmList.add((Element)childNode);
			}
		}
		return childElmList;
	}

}
