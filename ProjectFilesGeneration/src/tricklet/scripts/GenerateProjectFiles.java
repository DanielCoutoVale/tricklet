package tricklet.scripts;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import tricklet.ProjectFileGenerator;
import tricklet.utils.DOMUtils;

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
			document = DOMUtils.loadDocument(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			printUsage();
			System.exit(-1);
			return;
		}
		try {
			ProjectFileGenerator generator = new ProjectFileGenerator(file.getParentFile(), document);
			generator.generate();
		} catch (XPathExpressionException | IOException | SAXException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
			printUsage();
			System.exit(-1);
			return;
		}
	}

	

	private final static void printUsage() {
		System.out.println("Usage: java -jar generate-project-files.jar [experiment-file-path]");
	}

}
