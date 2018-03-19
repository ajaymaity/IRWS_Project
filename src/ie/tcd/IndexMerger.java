package ie.tcd;

import java.io.File;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
/**
 * Merges the 4 different indexes that has been created for different sources
 * By taking input the directory with indexes and new directory for merged indexes
 */

public class IndexMerger {
	/** Index all text files under a directory. */
	public static void main(String[] args) {

		if(args.length != 2){
	
			System.out.println("Required: Indexes Directory & Final Merged directory");
			System.exit(1);
		}
		
		File INDEXES_DIR  = new File(args[0]);
		File INDEX_DIR    = new File(args[1]);

		INDEX_DIR.mkdir();
		
		Date start = new Date();

		try {
			Analyzer analyzer = new EnglishAnalyzer();
//			Analyzer analyzer = new StandardAnalyzer();
//			Analyzer analyzer = new CustomAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			IndexWriter iwriter = new IndexWriter(INDEX_DIR, config, 	true);
			iwriter.setMergeFactor(1000);
			iwriter.setRAMBufferSizeMB(50);
			
			Directory indexes[] = new Directory[INDEXES_DIR.list().length];

			for (int i = 0; i < INDEXES_DIR.list().length; i++) {
				System.out.println("Adding: " + INDEXES_DIR.list()[i]);
				indexes[i] = FSDirectory.getDirectory(INDEXES_DIR.getAbsolutePath() 
													+ "/" + INDEXES_DIR.list()[i]);
			}

			System.out.print("Merging added indexes...");
			iwriter.addIndexes(indexes);
			System.out.println("done");

			System.out.print("Optimizing index...");
			iwriter.optimize();
			iwriter.close();
			System.out.println("done");

			Date end = new Date();
			System.out.println("It took: "+((end.getTime() - start.getTime()) / 1000) 
											+ "\"");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
