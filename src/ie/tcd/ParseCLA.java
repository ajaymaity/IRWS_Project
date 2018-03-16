package ie.tcd;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Parses the command line arguments
 * Copied CLI code from http://www.thinkplexx.com/blog/simple-apache-commons-cli-example-java-command-line-arguments-parsing
 * @author Ajay Maity Local
 *
 */

@SuppressWarnings("deprecation")
public class ParseCLA {

	private String[] args = null;
	private String fileName = null;
	private Options options = new Options();
	
	/**
	 * Constructor
	 * @param args Command line arguments
	 */
	public ParseCLA(String[] args, String fileName) {
		
		this.args = args;
		this.fileName = fileName;

		options.addOption("h", "help", false, "Show help.");
		if (this.fileName.contentEquals("ParseDocs"))
			options.addOption("d", "data", true, "Directory where the data is present [Default: \"contents/Assignment Two/Assignment Two/\"");
		else if (this.fileName.contentEquals("Indexer"))
			options.addOption("d", "docs", true, "Directory where the parsed documents are present [Default: \"outputs/parsed_docs/\"");
		else if (this.fileName.contentEquals("ParseTopics")) 
			options.addOption("t", "topics", true, "Directory where the topics are present [Default: \"contents/CS7IS3-Assignment2-Topics/\"");
		else {
			
			System.out.println("Unexpected error while initializing command line arguments!");
			System.out.println("Exiting application.");
			System.exit(1);
		}
	}
	
	/**
	 * Parse the command line arguments
	 * @return a hash map of command line arguments in a key-value pair.
	 */
	public Map<String, String> parse() {
		
		Map<String, String> values = new HashMap<String, String>();
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			
			cmd = parser.parse(options, args);
			
			if (cmd.hasOption("h")) help();
			if (this.fileName.contentEquals("ParseDocs"))
				values.put("dataDir", cmd.getOptionValue("d", "contents/Assignment Two/Assignment Two/"));
			else if (this.fileName.contentEquals("Indexer"))
				values.put("docsDir", cmd.getOptionValue("d", "outputs/parsed_docs/"));
			else if (this.fileName.contentEquals("ParseTopics")) 
				values.put("topicDir", cmd.getOptionValue("t", "contents/CS7IS3-Assignment2-Topics/"));
			else {
				
				System.out.println("Unexpected error while parsing command line arguments!");
				System.out.println("Exiting application.");
				System.exit(1);
			}
		}
		catch (ParseException e) {
			
			System.out.println("Failed to parse command line arguments.");
			System.out.println("Showing help...");
			help();
		}
		
		return values;
	}

	/**
	 * Help method
	 */
	private void help() {
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Help", options);
		System.exit(0);
	}
}
