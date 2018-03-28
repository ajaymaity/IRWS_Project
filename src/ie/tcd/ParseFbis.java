package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseFbis {

	public void parse() throws URISyntaxException, IOException {
		List<String> fbisDocs = new ArrayList<String>();
		Map<String, String> fbisDoc = new HashMap<String, String>();
		int total_docs_counter = 0;
		// String test="";
		File[] files = new File("./contents/Assignment Two/Assignment Two/fbis/").listFiles();
		
		Path ftPath = Paths.get("./outputs/parsed_docs/fbis.json");
		Files.write(ftPath, "[".getBytes(), StandardOpenOption.CREATE);
		
		for (int i = 0; i < files.length; i++)

		{
			if (files[i].isFile()) {
				// System.out.println(files[i]);
	
      			//System.out.println(files[i].getAbsolutePath());
      			
				
				Document document = Jsoup.parse(files[i], "UTF-8");
				//Elements elements = document.body().select("DOCNO,HT");
				Elements elements = document.select("doc");
			
				for (Element element : elements) {
					fbisDoc = new HashMap<String,String>();
                     String docNo= element.getElementsByTag("DOCNO").text().toLowerCase();
					String HT = element.getElementsByTag("HT").first().text().toLowerCase();
					String AU = element.getElementsByTag("AU").text().toLowerCase().trim();
					String Date = element.getElementsByTag("DATE1").text().toLowerCase().trim();
					String f = element.getElementsByTag("f").select("*").not("phrase").eachText() + "\n	";
					String text = element.getElementsByTag("Text").text().toLowerCase().trim();
					fbisDoc.put("docno",docNo);
				    fbisDoc.put("ht", HT);
					fbisDoc.put("au", AU);
					fbisDoc.put("date", Date);
					fbisDoc.put("f", f);
					fbisDoc.put("text", text);
					fbisDocs.add(new JSONObject(fbisDoc).toString() + ",");
					
					
				}
                 
      			

			}

		}
		File outputDir = new File("outputs");
		if (!outputDir.exists())
			outputDir.mkdir();

		// Create a directory to store parsed documents
		File parsedDocsDir = new File("./outputs/parsed_docs/");
		if (!parsedDocsDir.exists())
			parsedDocsDir.mkdir();

		// System.out.println("Storing parsed Fbis doc...");
		
		// deleteDir(new
		// File("/Users/playsafe/Desktop/Java/Parsing/src/outputs/parsed_docs/fbis.json"));

		Files.write(ftPath, fbisDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		System.out.println("Total Documents being saved: " + fbisDocs.size());
		Files.write(ftPath, "]".getBytes(), StandardOpenOption.APPEND);
	}
}
