package ie.tcd;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ParseLatimes {
	@SuppressWarnings({ "unchecked", "null" })
	public void parse() throws IOException, JSONException {

		File file = new File("./contents/Assignment Two/Assignment Two/latimes/");

		File[] files = file.listFiles();

		String jsonString = null;
		List<String> pw = new ArrayList<String>();
//		FileWriter pw = new FileWriter(new File("./outputs/parsed_docs/latimes.json"));
		Path ftPath = Paths.get("./outputs/parsed_docs/latimes.json");
		Files.write(ftPath, "[".getBytes(), StandardOpenOption.CREATE);
		
		// files.length
//		pw.write("[");
		for (int i = 0; i < files.length; i++) {
			StringBuilder content = new StringBuilder();
			// System.out.println("File name : " + files[i]);
			BufferedReader br = new BufferedReader(new FileReader(files[i]));
			ArrayList<String> arr = new ArrayList<String>();
			String st;
			while ((st = br.readLine()) != null) {
				content.append(st);
				if (st.startsWith("</DOC>")) {
					arr.add(content.toString());
					Document htmlObj = Jsoup.parse(content.toString());
					jsonString = new JSONObject().put("headline", htmlObj.getElementsByTag("headline").text())
							.put("paragraph", htmlObj.getElementsByTag("p").text())
							.put("date", htmlObj.getElementsByTag("date").text())
							.put("docno", htmlObj.getElementsByTag("docno").text())
							.put("text", htmlObj.getElementsByTag("text").text()).toString();

					// System.out.println(content);
					try {
						pw.add(jsonString + ",");
						// System.out.println("Copying contents to the JSON file...");
					} catch (Exception e) {
						e.printStackTrace();
					}
					content.setLength(0);
				}
			}
			// pw.close();

		}
		Files.write(ftPath, "]".getBytes(), StandardOpenOption.APPEND);
	}
}