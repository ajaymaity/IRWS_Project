package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseLatimes  {

	public void parse() throws URISyntaxException, IOException {
		List<String> fbisDocs = new ArrayList<String>();
		Map<String, String> fbisDoc = new HashMap<String, String>();
		// String test="";
		File[] files = new File("./contents/Assignment Two/Assignment Two/latimes/").listFiles();
		Path filePath = Paths.get("./outputs/parsed_docs/latimes.json");
		//	try {

		Files.write (filePath, "[".getBytes(), StandardOpenOption.CREATE);

		for (int i = 0; i < files.length; i++)

		{
			if (files[i].isFile()) {
				// System.out.println(files[i]);
						System.out.println(files[i].getAbsolutePath());
				// File files = new
				// File("/Users/playsafe/Desktop/Java/Parsing/src/contents/fbis/fbis/fb396001");
				Document document = Jsoup.parse(files[i], "UTF-8");
				Elements elements = document.body().select("doc");
				// System.out.println(files);
				for (Element element : elements) {

					/*
					 * sonString = new JSONObject().put("headline", htmlObj.getElementsByTag("headline").text())
							.put("paragraph", htmlObj.getElementsByTag("p").text())
							.put("date", htmlObj.getElementsByTag("date").text())
							.put("docno", htmlObj.getElementsByTag("docno").text())
							.put("text", htmlObj.getElementsByTag("text").text()).toString();
					 * */

					// System.out.println(element.getElementsByTag("DOCNO").text());
					String docno = element.getElementsByTag("docno").text().toLowerCase().trim();
					String para = element.getElementsByTag("p").first().text().toLowerCase().trim();
					String text_ = element.getElementsByTag("text").text().toLowerCase().trim();
					String date = element.getElementsByTag("date").text().toLowerCase().trim();
//					String text = element.getElementsByTag("Text").text().toLowerCase().trim();
					fbisDoc.put("docno", docno);
					fbisDoc.put("text", text_);
					fbisDoc.put("paragraph", para);
					fbisDoc.put("date", date);
//					
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

				Files.write(filePath, fbisDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
				Files.write(filePath, "]".getBytes(), StandardOpenOption.APPEND);

				// System.out.println("Storing done!\n");
				//			System.out.println(i);

				//			if (i == 1) break;

			

		}
		
	}

