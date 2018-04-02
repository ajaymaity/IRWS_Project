package ie.tcd;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Indexes the documents parsed using ParseDocs.java and saves in disk!
 * 
 * @author Ajay Maity Local
 *
 */
public class Indexer {

	private static final String[] docsStr = { "ft", "fr94", "fbis", "latimes" };

	/**
	 * Main Method
	 * 
	 * @param args
	 *            Command line arguments
	 * @throws IOException
	 *             when files/directories are not present
	 */
	public static void main(String[] args) throws IOException {

		// Parse command line arguments
		Map<String, String> values = new ParseCLA(args, "Indexer").parse();
		String docsDirStr = values.get("docsDir");
		Utils utils = new Utils();
		docsDirStr = utils.refineDirectoryString(docsDirStr);

		// Create analyzer
		Analyzer analyzer = new EnglishAnalyzer();
//		Analyzer analyzer = new CustomAnalyzer();

		// Delete previous index files
		System.out.println("Deleting previous index files, if they exist...");
		utils.deleteDir(new File("outputs/indexes/merged.index"));
		System.out.println("Done!\n");

		// Store index on disk
		Directory directory = FSDirectory.open(Paths.get("./outputs/indexes/merged.index"));

		// Create index writer
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		config.setSimilarity(new BM25Similarity());
		config.setRAMBufferSizeMB(50);
		IndexWriter iwriter = new IndexWriter(directory, config);

		for (String docStr : docsStr) {

			// Check if all paths are valid and exist
			utils.checkIfDirectory(docsDirStr);
			utils.checkIfFile(docsDirStr + docStr + ".json");

			// Parse into JSON
			JSONParser jsonParser = new JSONParser();
			JSONArray docs = null;
			try {

				System.out.println("Reading " + docStr + ".json...");
				docs = (JSONArray) jsonParser.parse(new FileReader(docsDirStr + docStr + ".json"));
			} catch (org.json.simple.parser.ParseException e) {

				System.out.println("Unable to parse " + docStr + ".json. Please ensure the format is correct.");
				System.out.println("Exiting application.");
				System.exit(1);
			}
			System.out.println("Reading done.\n");

			// Create index
			System.out.println("Creating index of " + docStr + " using English analyzer and BM25 similarity...");
			for (Object obj : docs) {

				JSONObject doc = (JSONObject) obj;

				Document document = new Document();
				@SuppressWarnings("unchecked")
				Set<String> elements = doc.keySet();
				for (String element : elements) {
					
					document.add(new TextField(element, (String) doc.get(element), Field.Store.YES));
				}
				iwriter.addDocument(document);
			}
			System.out.println("Indexing done of " + docStr + ", and saved on disk.");
		}
		iwriter.close();
		directory.close();
	}
}
