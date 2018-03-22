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
 * Parse topics, along with various File I/O operations.
 * @author Ajay Maity Local
 *
 */
public class ParseTopics {

	/**
	 * Parse Topics
	 * @param topicDirStr directory where the topics are present
	 * @return a list of parsed map topics
	 * @throws IOException when file is not present
	 */
	private List<String> parseTopics(String topicDirStr) throws IOException {
		
		List<String> tops = new ArrayList<String>();
		List<File> filesList = (new Utils()).getFiles(topicDirStr, false);
		
		Utils utils = new Utils();
		Map<String, String> top = null;
		int docCount = 0;
		// Loop through files
		System.out.println("Number of files: " + Integer.toString(filesList.size()));
		for (File file: filesList) {
			
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String line;
			int lineNumber = 0;
			
			boolean topInProgress = false;
			boolean numInProgress = false;
			boolean titleInProgress = false;
			boolean descInProgress = false;
			boolean narrInProgress = false;
			
			String topNext = "top";
			
			// Loop through each line
			while ((line = bufferedReader.readLine()) != null) {
				
				lineNumber++;
				line = line.trim();
				if (line.contentEquals("")) continue;
				char firstChar = line.charAt(0);
				
				// Check if line starts with < 
				if (firstChar == '<') {
					
					// Check if line start with </
					if (line.charAt(1) == '/') {
						
						boolean endsWithGt = line.endsWith(">");
						String[] partitions = line.split(">");
						if (partitions.length == 1 && !endsWithGt) Errors.printMalformedErrorAndExit(line, lineNumber, file);
						else {
						
							String firstPartition = partitions[0];
							String element = firstPartition.substring(2, firstPartition.length());
							
							// Identify element and execute corresponding logic
							switch (element) {
							
							case "top": // line starts with </top>
								
								if (topInProgress) {
									
									if (topNext.contentEquals("/top")) {
									
										narrInProgress = false;
										topInProgress = false;
										if (partitions.length > 1) Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
										top.put("num", top.get("num").trim().substring("Number: ".length(), top.get("num").trim().length()));
										top.put("title", top.get("title").trim());
										top.put("desc", top.get("desc").trim().substring("Description: ".length(), top.get("desc").trim().length()));
										top.put("narr", top.get("narr").trim().substring("Narrative: ".length(), top.get("narr").trim().length()));
										
										tops.add(new JSONObject(top).toString() + ",");
										top = null;
										docCount++;
										topNext = "top";
									}
									else Errors.printUnexpectedNextElement("/top", topNext, lineNumber, file);
								}
								else Errors.printUnopenedElementErrorAndExit(element, lineNumber, file);
								break;
								
							default: Errors.printCantParseErrorAndExit(line, lineNumber, file);
							}
						}
					}
					else { // In this block, line starts with < and not </
						
						boolean endsWithGt = line.endsWith(">");						
						String[] partitions = line.split(">");
						if (partitions.length == 1 && !endsWithGt) Errors.printMalformedErrorAndExit(line, lineNumber, file);
						else {
							
							String firstPartition = partitions[0];
							String element = firstPartition.substring(1, firstPartition.length());
							
							// Identify element and execute corresponding logic
							switch (element) {
							
							case "top": // line starts with <top>
								
								if (!topInProgress) {
									
									if (topNext.contentEquals("top")) {
									
										topInProgress = true;
										if (partitions.length > 1) Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
										top = new HashMap<String, String>();
										topNext = "num";
									}
									else Errors.printUnexpectedNextElement("top", topNext, lineNumber, file);
								}
								else Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								break;
								
							case "num": // line starts with <num>
							case "title": // line starts with <title>
							case "desc": // line starts with <desc>
							case "narr": // line starts with <narr>
								
								if (topInProgress) {
									
									boolean changeNum = element.contentEquals("num") && !numInProgress && !titleInProgress && !descInProgress && !narrInProgress;
									boolean changeTitle = element.contentEquals("title") && numInProgress && !titleInProgress && !descInProgress && !narrInProgress;
									boolean changeDesc = element.contentEquals("desc") && !numInProgress && titleInProgress && !descInProgress && !narrInProgress;
									boolean changeNarr = element.contentEquals("narr") && !numInProgress && !titleInProgress && descInProgress && !narrInProgress;
									
									if (changeNum || changeTitle || changeDesc || changeNarr) {
										
										if (partitions.length > 2) Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
										else {
											
											if (changeNum) {
												
												if (topNext.contentEquals("num")) {
													
													numInProgress = true;
													topNext = "title";
												}
												else Errors.printUnexpectedNextElement(topNext, "num", lineNumber, file);
											}
											if (changeTitle) {
												
												if (topNext.contentEquals("title")) {
													
													numInProgress = false;
													titleInProgress = true;
													topNext = "desc";
												}
												else Errors.printUnexpectedNextElement(topNext, "num", lineNumber, file);
											}
											else if (changeDesc) {
												
												if (topNext.contentEquals("desc")) {
													
													titleInProgress = false;
													descInProgress = true;
													topNext = "narr";
												}
												else Errors.printUnexpectedNextElement(topNext, "desc", lineNumber, file);
											}
											else if (changeNarr) {
												
												if (topNext.contentEquals("narr")) {
													
													descInProgress = false;
													narrInProgress = true;
													topNext = "/top";
												}
												else Errors.printUnexpectedNextElement(topNext, "narr", lineNumber, file);
											}
											
											String part = partitions[1];
											if (changeNum || changeTitle || changeDesc || changeNarr) part += " ";
											top.put(element.toLowerCase(), part);
										}
									}
									else Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								}
								else Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;
							
							default: Errors.printCantParseErrorAndExit(line, lineNumber, file);
							}
						}
					}
				}
				else { // if line does not start with <
					
					if (topInProgress) { // Store contents inside top
						
						if (numInProgress) utils.storeContentInMap(top, line, "num");
						else if (titleInProgress) utils.storeContentInMap(top, line, "title");
						else if (descInProgress) utils.storeContentInMap(top, line, "desc");
						else if (narrInProgress) utils.storeContentInMap(top, line, "narr");
						else Errors.printCantParseErrorAndExit(line, lineNumber, file);
					}
					else Errors.printCantParseErrorAndExit(line, lineNumber, file);
				}
			}
			bufferedReader.close();
		}
		System.out.println("Number of documents: " + Integer.toString(docCount));
		return tops;
	}

	/**
	 * Main Method
	 * @param args Command line arguments
	 * @throws IOException when file to parse is not found
	 */
	public static void main(String[] args) throws IOException {
		
		// Parse command line arguments
		Map<String, String> values = new ParseCLA(args, "ParseTopics").parse();
		String topicDirStr = values.get("topicDir");
		Utils utils = new Utils();
		topicDirStr = utils.refineDirectoryString(topicDirStr);
		
		// Check if all paths are valid and exist
		utils.checkIfDirectory(topicDirStr);
		
		// Parse Topics
		ParseTopics pt = new ParseTopics();
		System.out.println("Parsing topics...");
		List<String> parsedTopics = pt.parseTopics(topicDirStr);
		System.out.println("Parsing done!\n");
		
		// Create output directory if it does not exist
		File outputDir = new File("outputs");
		if (!outputDir.exists()) outputDir.mkdir();
		
		// Create a directory to store parsed topics
		File parsedTopicsDir = new File("outputs/parsed_topics");
		if (!parsedTopicsDir.exists()) parsedTopicsDir.mkdir();
		
		System.out.println("Storing parsed topics...");
		Path topsPath = Paths.get("outputs/parsed_topics/tops.json");
		utils.deleteDir(new File("outputs/parsed_topics/tops.json"));
		Files.write(topsPath, "[".getBytes(), StandardOpenOption.CREATE);
		Files.write(topsPath, parsedTopics, Charset.forName("UTF-8"), StandardOpenOption.APPEND);;
		Files.write(topsPath, "]".getBytes() , StandardOpenOption.APPEND);
		System.out.println("Storing done!\n");
	}
}
