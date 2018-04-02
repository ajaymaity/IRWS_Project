package ie.tcd;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Test {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		// Parse into JSON
		JSONParser jsonParser = new JSONParser();
		JSONArray docs = null;
		try {

			System.out.println("Reading ft.json...");
			docs = (JSONArray) jsonParser.parse(new FileReader("parsed_docs_working_22.18/ft.json"));
		} catch (org.json.simple.parser.ParseException e) {

			System.out.println("Unable to parse ft.json. Please ensure the format is correct.");
			System.out.println("Exiting application.");
			System.exit(1);
		}
		System.out.println("Reading done.\n");
		
		int docNumber = 0;
		for (Object obj: docs) {
			
			JSONObject doc = (JSONObject) obj;
			Document document = new Document();
			@SuppressWarnings("unchecked")
			Set<String> elements = doc.keySet();
			for (String element : elements) {
				
				System.out.println(element);
				System.out.println(doc.get(element));
				System.out.println("****");
			}
			
			if (++docNumber == 5) break;
		}
	}
}
