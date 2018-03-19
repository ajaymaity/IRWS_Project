package ie.tcd;

import org.apache.lucene.analysis.LowerCaseFilter;


import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Custom Analyzer using different filters available in Lucene
 *
 */

public class CustomAnalyzer {
	
	protected TokenStreamComponents createComponents(String fieldName) {
		// TODO Auto-generated method stub
		final Tokenizer source = new StandardTokenizer();
		TokenStream filter = new StandardFilter(source);
//		filter = new QuestionMarkerTokenFilter(source);
		filter = new LowerCaseFilter(source);
		filter = new EnglishPossessiveFilter(source);
		filter = new EnglishMinimalStemFilter(source);
		filter = new StandardFilter(source);
		filter = new ClassicFilter(source);
		filter = new KStemFilter(filter);
	    filter = new EnglishMinimalStemFilter(filter);
	    filter = new EnglishPossessiveFilter(filter);	  
	    filter = new PorterStemFilter(filter);
	    CharArraySet stopWord = CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	    filter = new StopFilter(filter, stopWord);
		
		
		
		return new TokenStreamComponents(source, filter);
	}

}
