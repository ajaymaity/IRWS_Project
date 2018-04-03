package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Set;

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

//	private static final String[] allElements = { "date", "dateline", "in", "profile", "cn", "docno", "co", "pe",
//			"text", "page", "tp", "pub", "headline", "byline", "usdept", "agency", "usbureau", "doctitle", "summary",
//			"supplem", "other", "ht", "au", "f", "paragraph", "header", "date1", "h1", "h2", "h3", "h4", "h5", "h6",
//			"fp100", "fp101", "fp102", "fp103", "fp104", "fp105", "fp106", "abs" };
	private static final String[] allElements = {"date", // elements of FT 
			"dateline", // elements of FT
			"in", // elements of FT
			"profile", // elements of FT
			"cn", // elements of FT
			"docno", // elements of FT
			"co", // elements of FT
			"pe", // elements of FT
			"text", // elements of FT
			"page", // elements of FT
			"tp", // elements of FT
			"pub", // elements of FT
			"headline", // elements of FT
			"byline", // elements of FT
			"usdept", "agency", "usbureau", "doctitle", "summary", "supplem", "other",
			"ht", "au", "f",
			"paragraph"};
//	private static final String[] allElements = {"date", "docno",
//			"text", "headline",
//			"usdept", "agency", "usbureau", "doctitle", "summary", "supplem", "other",
//			"ht", "au", "f",
//			"paragraph"};
	
	private static String boostQueryString(String term, String boost, List<String> distinctWords) {
		
		String updatedQueryStr = "\"" + term + "\"^" +
				boost + " ";
		
		String[] words = term.split(" ");
		if (words.length > 1) {
			
			for (String word: words) {
				
				if (!distinctWords.contains(word)) {

					distinctWords.add(word);
					updatedQueryStr += "\"" + word + "\"^" +
							boost + " ";
				}
			}
		}
		
		return updatedQueryStr;
	}
	
	private static List<String> updateDistinctWords(List<String> distinctWords, String term) {
		
		String[] words = term.split(" ");
		for (String word: words) {
			
			if (!distinctWords.contains(word))
				distinctWords.add(word);
		}
		return distinctWords;
	}

	/**
	 * Create query from topic
	 * 
	 * @param top
	 *            the topic JSON to create query from
	 * @return the query string created from the topic JSON
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static String createQuery(JSONObject top) throws FileNotFoundException, IOException {

		// Concatenate the four elements
		String queryStr = (String) top.get("num") + ". " + (String) top.get("title") + ". " + (String) top.get("desc")
				+ ". " + (String) top.get("narr");

		// Consider desc and narr elements
//		 String queryStr = (String) top.get("desc") + " " + (String) top.get("narr");

		// Consider only desc
		// String queryStr = (String) top.get("desc");

		// Consider only narr
		// String queryStr = (String) top.get("narr");

		// Consider num, desc and narr elements
		// String queryStr = (String) top.get("num") + " "
		// + (String) top.get("desc") + " " + (String) top.get("narr");
		
		queryStr = queryStr.replaceAll("\\?", ".").replaceAll("\\*", " ").replaceAll("\\:", "").replaceAll("\\s", " ");
		
		// Term Boosting by detecting Entities
		WatsonNLU wnlu = new WatsonNLU();
		List<Map<String, Long>> valuesList = wnlu.analyze(queryStr);
		Set<String> entityKeys = valuesList.get(0).keySet();
//		Set<String> keywordKeys = valuesList.get(1).keySet();
		
		// Boost all entities by their count appearing in the query + 1
		for (String entityKey: entityKeys)
			queryStr = queryStr.replaceAll(" " + entityKey + " ", " \"" + entityKey + "\"^" + Long.toString(valuesList.get(0).get(entityKey) + 1) + " ");
		
		List<String> distinctWords = new ArrayList<String>();
		String[] words = queryStr.split(" ");
		for (String word: words) {
			
			if (!distinctWords.contains(word)) {
				
				distinctWords.add(word);
				if (word.length() <= 3) {
				
					queryStr = queryStr.replaceAll(" " + word + " ", " " + word + "^0 ");
				}
			}
		}
		
//		String updatedQueryStr = "";
//		List<String> distinctWords = new ArrayList<String>(); 
//		for (String entityKey: entityKeys) {
//
//			updatedQueryStr += boostQueryString(entityKey, 
//					Long.toString(valuesList.get(0).get(entityKey)), 
//					distinctWords);
//			distinctWords = updateDistinctWords(distinctWords, entityKey);
//		}
//		
//		for (String keywordKey: keywordKeys) {
//			
//			updatedQueryStr += boostQueryString(keywordKey, "1", distinctWords);
//			distinctWords = updateDistinctWords(distinctWords, keywordKey);
//		}
//		
//		queryStr = queryStr.replaceAll("\\p{P}", "");
//		String[] words = queryStr.split(" ");
//		for (String word: words) {
//
//			if (word.contentEquals("")) continue;
//			if (!distinctWords.contains(word)) {
//
//				distinctWords.add(word);
//				String boost = "0.5";
//				if (word.length() <= 3) boost = "0.1";
//				updatedQueryStr += "\"" + word + "\"^" + boost + " ";
//			}
//		}
		
		String updatedQueryStr = queryStr;
		System.out.println(updatedQueryStr);
		return updatedQueryStr;
	}
	
	public static Map<String, Float> getBoost() {
		
		Map<String, Float> boost = new HashMap<String, Float>();
		boost.put("date", (float) 0);
		boost.put("dateline", (float) 2);		
		boost.put("in", (float) 1);
		boost.put("profile", (float) 0);
		boost.put("cn", (float) 2);
		boost.put("docno", (float) 0);
		boost.put("co", (float) 1);
		boost.put("pe", (float) 1);
		boost.put("text", (float) 24);
		boost.put("page", (float) 0);
		boost.put("tp", (float) 0);
		boost.put("pub", (float) 0);
		boost.put("headline", (float) 4);
		boost.put("byline", (float) 1);
		
		boost.put("summary", (float) 4);
		boost.put("doctitle", (float) 4);
		boost.put("usdept", (float) 0.5);
		boost.put("agency", (float) 0.5);
		boost.put("usbureau", (float) 0.5);
		boost.put("paragraph", (float) 0.5);
		boost.put("supplem", (float) 0.25);
		boost.put("other", (float) 0.1);
		boost.put("ht", (float) 0.1);
		boost.put("au", (float) 0.1);
		boost.put("f", (float) 0.1);
		
		return boost;
	}

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

		// Delete previous final queries files
		System.out.println("Deleting previous final queries files, if they exist...");
		utils.deleteDir(new File("outputs/final_queries/queries.txt"));
		System.out.println("Done!\n");

		int index = 0;
		List<String> resFileContent = new ArrayList<String>();
		List<String> queryFileContent = new ArrayList<String>();

		String[] elements = allElements;

		// Check if all paths are valid and exist
		String ftIndexDirStr = indexDirStr + "merged.index";
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
		int queryNum = 0;
		Map<String, Float> boosts = getBoost();
		for (Object obj : tops) {

			queryNum++;
			JSONObject top = (JSONObject) obj;

			String queryStr = createQuery(top);
			queryFileContent.add(queryStr);

			queryStr = queryStr.replace("/", "\\/");
//			System.out.println(queryNum);
//			System.out.println(queryStr);
//			System.out.println();
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(elements, analyzer, boosts);
			Query query = queryParser.parse(queryStr);

			// Search
			TopDocs topDocs = isearcher.search(query, HITS_PER_PAGE);
			ScoreDoc[] hits = topDocs.scoreDocs;

			// Retrieve results
			for (int j = 0; j < hits.length; j++) {

				int docId = hits[j].doc;
				Document doc = isearcher.doc(docId);
				resFileContent.add((String) top.get("num") + " 0 " + doc.get("docno").toUpperCase() + " 0 "
						+ hits[j].score + " STANDARD");
			}
		}
		System.out.println("Searching done!\n");

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
