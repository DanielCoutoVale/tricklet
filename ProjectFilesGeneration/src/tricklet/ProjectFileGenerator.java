package tricklet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tricklet.utils.DOMUtils;

/**
 * A generator of project files
 * 
 * @author Daniel Couto-Vale <daniel.couto-vale@rwth-aachen.de>
 */
public class ProjectFileGenerator {

	/**
	 * RTF Prefix
	 */
	private final static String rtfPrefix = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1031{\\fonttbl{\\f0\\fnil\\fcharset0 Courier New;}}\n\\viewkind4\\uc1\\pard\\lang1033\\f0\\fs30 ";

	/**
	 * RTF Suffix
	 */
	private final static String rtfSuffix = "}";

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

	public final void generate() throws IOException, SAXException, ParserConfigurationException, TransformerException {
		List<String> participantIdList = new ArrayList<String>(participantMap.keySet());
		Collections.sort(participantIdList);
		File participantsDirectory = new File(directory, "Participants");
		makeDirectory(participantsDirectory);
		for (String participantId : participantIdList) {
			File participantDirectory = new File(participantsDirectory, participantId);
			makeDirectory(participantDirectory);
			fillDirectory(directory, participantDirectory, participantId);
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

	private final void fillDirectory(File directory, File participantDirectory, String participantId) throws IOException, SAXException, ParserConfigurationException, TransformerException {
		List<Element> textList = getTextList(participantId);
		int i = 1;
		for (Element textElm : textList) {
			String textId = textElm.getAttribute("id");
			String projectName = participantId + "-" + (i++) + "-" + textId + ".project";  
			File projectFile = new File(participantDirectory, projectName);
			if (projectFile.exists()) {
				projectFile.delete();
			}
			projectFile.createNewFile();
			String textPath = textElm.getAttribute("path");
			File textFile = new File(directory, textPath);
			if (!textFile.exists()) {
				continue;
			}
			fillProjectFile(projectFile, textFile);
		}
	}

	private class IElement {
		private final Element element;
		public IElement(Element element) {
			this.element = element;
		}
		private IElement getChild(String elementName) {
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode instanceof Element) {
					Element childElement = (Element) childNode;
					if (childNode.getNodeName().equals(elementName)) {
						return new IElement(childElement);
					}
				}
			}
			return null;
		}
	}

	private final void fillProjectFile(File projectFile, File textFile) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		String textUtf8 = loadTextUtf8(textFile);
		String text = makeText(textUtf8);
		textUtf8 = textUtf8.replace("~", " ");
		InputStream resource = ProjectFileGenerator.class.getResourceAsStream("ProjectFileExample.xml");
		Document document = DOMUtils.loadDocument(resource);
		IElement elm = new IElement(document.getDocumentElement());
		IElement settingsElm = elm.getChild("Interface").getChild("Standard").getChild("Settings");
		IElement textElm = settingsElm.getChild("SourceText");
		IElement textUtf8Elm = settingsElm.getChild("SourceTextUTF8");
		textElm.element.setTextContent(text);
		textUtf8Elm.element.setTextContent(textUtf8);
		DOMUtils.save(document, projectFile);
	}

	private final String makeText(String textUtf8) {
		StringBuffer buffer = new StringBuffer();
		while (textUtf8.length() > 0) {
			int index = 106;
			if (index + 1 > textUtf8.length()) {
				buffer.append(textUtf8);
				break;
			}
			while (index > 0 && textUtf8.charAt(index) != ' ') {
				index --;
			}
			if (index == 0) index = 105;
			buffer.append(textUtf8.substring(0, index + 1));
			textUtf8 = textUtf8.substring(index + 1);
			buffer.append("\\par\\par\n");
		}
		textUtf8 = buffer.toString();
		String text = rtfPrefix + textUtf8.replace("‘", "\\'91").replace("’", "\\'92").replace("“", "\\'93").replace("”", "\\'94").replace("~", " ") + rtfSuffix;
		return text;
	}

	private final String loadTextUtf8(File textFile) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(textFile.toURI()));
		return new String(encoded, "UTF-8");
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
