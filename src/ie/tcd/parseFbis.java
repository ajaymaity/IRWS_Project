package ie.tcd;

import java.io.File;
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
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class parseFbis {

	public void parse() throws URISyntaxException, IOException {
		List<String> fbisDocs = new ArrayList<String>();
		Map<String, String> fbisDoc = new HashMap<String, String>();
		// String test="";
		File[] files = new File("./contents/AssignmentTwo/AssignmentTwo/fbis/").listFiles();
		for (File input : files) {
			if (input.isFile()) {

				System.out.println(input.getAbsolutePath());
				Document document = Jsoup.parse(input, "UTF-8");

				// System.out.println(document.getElementsByTag("DATE1"));
				// Elements paragraphs = document.getElementsByTag("HT");

				Elements elements = document.body().select("doc");

				for (Element element : elements) {

					/*
					 * System.out.println(element.getElementsByTag("DOCNO").text());
					 * System.out.println(element.getElementsByTag("HT").first().text());
					 * System.out.println(element.getElementsByTag("AU").text());
					 * System.out.println(element.getElementsByTag("DATE1").text());
					 * System.out.println(element.getElementsByTag("f").select("*").not("phrase").
					 * eachText()+"\n");
					 * System.out.println(element.getElementsByTag("Text").text());
					 */

					String docno = element.getElementsByTag("DOCNO").text();
					String HT = element.getElementsByTag("HT").first().text();
					String AU = element.getElementsByTag("AU").text();
					String Date = element.getElementsByTag("DATE1").text();
					String f = element.getElementsByTag("f").select("*").not("phrase").eachText() + "\n";
					String text = element.getElementsByTag("Text").text();
					fbisDoc.put("Docno", docno);
					fbisDoc.put("HT", HT);
					fbisDoc.put("AU", AU);
					fbisDoc.put("DATE", Date);
					fbisDoc.put("F", f);
					fbisDoc.put("Text", text);
					fbisDocs.add(new JSONObject(fbisDoc).toString() + ",");

				}

				File outputDir = new File("outputs");
				if (!outputDir.exists())
					outputDir.mkdir();

				// Create a directory to store parsed documents
				File parsedDocsDir = new File("outputs/parsed_docs/");
				if (!parsedDocsDir.exists())
					parsedDocsDir.mkdir();

				System.out.println("Storing parsed Fbis doc...");
				Path ftPath = Paths.get("outputs/parsed_docs/fbis.json");
				// deleteDir(new
				// File("/Users/playsafe/Desktop/Java/Parsing/src/outputs/parsed_docs/fbis.json"));
				Files.write(ftPath, "[".getBytes(), StandardOpenOption.CREATE);

				Files.write(ftPath, fbisDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

				Files.write(ftPath, "]".getBytes(), StandardOpenOption.APPEND);
				System.out.println("Storing done!\n");
			}
		}
	}
}
