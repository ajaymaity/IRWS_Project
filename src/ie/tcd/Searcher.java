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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Searches the index using the indexes created using Indexer.java
 * 
 * @author Ajay Maity Local
 *
 */
public class Searcher {

	private static final int HITS_PER_PAGE = 1000;
	private static final String[] indexesStr = { "ft", "fr94", "fbis", "latimes" };
	private static final String[][] allElements = { { "date", "dateline", "in", "profile", "cn", "docno", "co", "pe",
			"text", "page", "tp", "pub", "headline", "byline" },
			{"docno", "usdept", "agency", "usbureau", "doctitle", "summary", "supplem", "other"},
			{"docno", "ht", "au", "date", "f", "text"},
			{"docno", "headline", "paragraph", "date", "text"}};

	/**
	 * Main Method
	 * 
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {

		// Parse command line arguments
		Map<String, String> values = new ParseCLA(args, "Searcher").parse();
		String indexDirStr = values.get("indexDir");
		String parsedTopicFileStr = values.get("parsedTopicFile");
		Utils utils = new Utils();
		indexDirStr = utils.refineDirectoryString(indexDirStr);

		// Create output directory if it does not exist
		File outputDir = new File("outputs");
		if (!outputDir.exists())
			outputDir.mkdir();

		// Create a directory to store parsed documents
		File parsedResDir = new File("outputs/results");
		if (!parsedResDir.exists())
			parsedResDir.mkdir();
	
		// Create a directory to store final queries
		File finalQueriesDir = new File("outputs/final_queries");
		if (!finalQueriesDir.exists())
			finalQueriesDir.mkdir();

		// Delete previous results files
		System.out.println("Deleting previous results files, if they exist...");
		utils.deleteDir(new File("outputs/results/search_results.txt"));
		System.out.println("Done!\n");

		int index = 0;
		List<String> resFileContent = new ArrayList<String>();
		List<String> queryFileContent = new ArrayList<String>();
		for (String indexStr : indexesStr) {

			String[] elements = allElements[index++];

			// Check if all paths are valid and exist
			String ftIndexDirStr = indexDirStr + indexStr + ".index";
			utils.checkIfDirectory(indexDirStr);
			utils.checkIfDirectory(ftIndexDirStr);
			utils.checkIfFile(parsedTopicFileStr);

			// Parse into JSON
			JSONParser jsonParser = new JSONParser();
			JSONArray tops = null;
			try {

				System.out.println("Reading " + parsedTopicFileStr + "...");
				tops = (JSONArray) jsonParser.parse(new FileReader(parsedTopicFileStr));
			} catch (org.json.simple.parser.ParseException e) {

				System.out.println("Unable to parse " + parsedTopicFileStr + ". Please ensure the format is correct.");
				System.out.println("Exiting application.");
				System.exit(1);
			}
			System.out.println("Reading done.\n");

			// Create the same analyzer as the indexer
			Analyzer analyzer = new EnglishAnalyzer();

			// Get index from disk
			Directory directory = FSDirectory.open(Paths.get(ftIndexDirStr));
			DirectoryReader ireader = DirectoryReader.open(directory);

			// Create an index searcher
			IndexSearcher isearcher = new IndexSearcher(ireader);
			isearcher.setSimilarity(new BM25Similarity());

			System.out.println("Searching index using English analyzer and BM25 similarity, with "
					+ Integer.toString(HITS_PER_PAGE) + " hits per page.");
			// Loop through all queries and retrieve documents
			for (Object obj : tops) {

				JSONObject top = (JSONObject) obj;
				String queryStr = (String) top.get("num") + " " + (String) top.get("title") + " "
						+ (String) top.get("desc") + " " + (String) top.get("narr");
				queryFileContent.add(queryStr);
				
				queryStr = queryStr.replace("/", "\\/");
				MultiFieldQueryParser queryParser = new MultiFieldQueryParser(elements, analyzer);
				Query query = queryParser.parse(queryStr);

				// Search
				TopDocs topDocs = isearcher.search(query, HITS_PER_PAGE);
				ScoreDoc[] hits = topDocs.scoreDocs;

				// Retrieve results
				for (int j = 0; j < hits.length; j++) {

					int docId = hits[j].doc;
					Document doc = isearcher.doc(docId);
					resFileContent.add(
							(String) top.get("num") + " 0 " + doc.get("docno") + " 0 " + hits[j].score + " STANDARD");
				}
			}
			System.out.println("Searching done!\n");
		}
		
		System.out.println("Writing queries to file...");
		Files.write(Paths.get("outputs/final_queries/queries.txt"), queryFileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE_NEW);
		System.out.println("Results written to outputs/results/search_results.txt to be used in TREC Eval.");

		System.out.println("Writing results to file...");
		Files.write(Paths.get("outputs/results/search_results.txt"), resFileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE_NEW);
		System.out.println("Results written to outputs/results/search_results.txt to be used in TREC Eval.");
	}
}
