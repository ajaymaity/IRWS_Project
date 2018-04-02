package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Parse documents, along with various File I/O operations.
 * 
 * @author Ajay Maity Local
 *
 */
public class ParseDocs {

	/**
	 * Main Method
	 * 
	 * @param args
	 *            Command line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Parse Command line arguments
		Map<String, String> values = new ParseCLA(args, "ParseDocs").parse();
		String dataDir = values.get("dataDir");
		String outputDirStr = values.get("outputDir");

		// Refine the directory string value
		Utils utils = new Utils();
		dataDir = utils.refineDirectoryString(dataDir);
		outputDirStr = utils.refineDirectoryString(outputDirStr);

		// Create output directories if it does not exist
		File outputDir = new File(outputDirStr);
		if (!outputDir.exists())
			outputDir.mkdirs();
		
		// Delete old JSON files
		utils.deleteDir(new File(outputDirStr + "ft.json"));
		utils.deleteDir(new File(outputDirStr + "fr94.json"));
		utils.deleteDir(new File(outputDirStr + "fbis.json"));
		utils.deleteDir(new File(outputDirStr + "latimes.json"));

		// Parse and Store FT documents
		ParseFT pft = new ParseFT();
		String ftDirectoryStr = dataDir + "ft/";
		System.out.println("Parsing FT documents...");
		List<String> ftDocs = pft.parse(ftDirectoryStr, true);
		System.out.println("Parsing done!");
		System.out.println("Storing parsed FT doc...");
		Path ftPath = Paths.get(outputDirStr + "ft.json");
		Files.write(ftPath, "[".getBytes(), StandardOpenOption.CREATE);
		Files.write(ftPath, ftDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		Files.write(ftPath, "]".getBytes(), StandardOpenOption.APPEND);
		System.out.println("Storing done!\n");

		// Parse and Store FR94 documents
		ParseFR94 fr94 = new ParseFR94();
		File[] files = new File(dataDir + "fr94/").listFiles();
		System.out.println("Storing parsed FR94 doc...");
		fr94.fileAcces(files);
		Path frPath = Paths.get("outputs/parsed_docs/fr94.json");
		Files.write(frPath, "]".getBytes(), StandardOpenOption.APPEND);
		System.out.println("Storing done!\n");

		// Parse and Store FBIS documents
		System.out.println("Parsing FBIS...");
		ParseFbis fbis = new ParseFbis();
		fbis.parse();
		System.out.println("Done!\n");
		
//		ParseFBIS2 pfbis = new ParseFBIS2();
//		String fbisDirectoryStr = dataDir + "fbis/";
//		System.out.println("Parsing FBIS documents...");
//		List<String> fbisDocs = pfbis.parse(fbisDirectoryStr, false);
//		System.out.println("Parsing done!");
//		System.out.println("Storing parsed FBIS doc...");
//		Path fbisPath = Paths.get(outputDirStr + "fbis.json");
//		Files.write(fbisPath, "[".getBytes(), StandardOpenOption.CREATE);
//		Files.write(fbisPath, fbisDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
//		Files.write(fbisPath, "]".getBytes(), StandardOpenOption.APPEND);
//		System.out.println("Storing done!\n");

		// Parse and Store LaTimes documents
		System.out.println("Parsing Latimes...");
		ParseLatimes lat = new ParseLatimes();
		lat.parse();
		System.out.println("Done!\n");
	}
}