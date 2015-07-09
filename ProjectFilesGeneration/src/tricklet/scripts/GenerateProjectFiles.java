package tricklet.scripts;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Generates project files from a set of text files
 * 
 * @author Daniel Couto-Vale <daniel.couto-vale@rwth-aachen.de>
 *
 */
public class GenerateProjectFiles {

	/**
	 * Constructor
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		// Check arguments
		if (args.length != 1) {
			printUsage();
			System.exit(-1);
			return;
		}

		// Check file
		String filePath = args[0];
		File file = new File(filePath);
		if (!file.exists()) {
			printUsage();
			System.exit(-1);
			return;
		}

		// Create document
		Document document;
		try {
			document = loadDocument(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			printUsage();
			System.exit(-1);
			return;
		}
		document.getAttributes();
	}

	private final static Document loadDocument(File file) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(file);
		return document;
	}

	private final static void printUsage() {
		System.out.println("Usage: java -jar generate-project-files.jar [experiment-file-path]");
	}

}
