package ie.tcd;

import org.json.simple.JSONObject;
//import java.io.BufferedReader;
//import org.json.simple.JSONObject;
//import java.io.FileReader;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
import org.jsoup.*;
import java.io.File;
import java.nio.charset.Charset;
//import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
//import org.json.simple.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseFR94 {

	public static void deleteDir(File file) {

		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	public void fileAcces(File[] files) throws Exception {
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Directory: " + file.getName());
				fileAcces(file.listFiles()); // Calls same method again.

			} else {
				try {
					fileParser(file.getPath());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void fileParser(String path) throws Exception {
		List<String> frDocs = new ArrayList<String>();
		Map<String, String> frDoc = new HashMap<String, String>();
		File file = new File(path);
		Document docmnt = Jsoup.parse(file, "UTF-8", "");
		Path frPath = Paths.get("outputs/parsed_docs/fr94.json");
		Files.write(frPath, "[".getBytes(), StandardOpenOption.CREATE);

		// JSONArray a1 = new JSONArray();
		// JSONArray json = new JSONArray();

		docmnt.select("table").remove();
		docmnt.select("signer").remove();
		docmnt.select("signjob").remove();
		docmnt.select("action").remove();
		docmnt.select("footname").remove();
		docmnt.select("footnote").remove();
		docmnt.select("parent").remove();
		docmnt.select("rindock").remove();
		docmnt.select("further").remove();
		docmnt.select("address").remove();
		docmnt.select("footcite").remove();
		docmnt.select("import").remove();
		docmnt.select("cfrno").remove();
		docmnt.select("date").remove();
		docmnt.select("frfiling").remove();
		docmnt.select("billing").remove();

		Elements docs = docmnt.select("doc");
		Elements text = docmnt.select("text");
		int i = 0;

		// System.out.println("Doc Size: " + docs.size());

		String docNo[] = new String[docs.size()];
		String usDept[] = new String[docs.size()];
		String agency[] = new String[docs.size()];
		String usBureau[] = new String[docs.size()];
		String docTitle[] = new String[docs.size()];
		String summary[] = new String[docs.size()];
		String supplem[] = new String[docs.size()];
		String Other[] = new String[docs.size()];

		// IndexDocument id[] = new IndexDocument[docs.size()];

		for (Element e : docs) {

			// System.out.println("see here 1 !!!: "+e.getElementsByTag("docno").text());
			docNo[i] = e.getElementsByTag("docno").text();

			// id[i] = new IndexDocument();
			// id[i].setDocNo(docNo[i]);
			i++;
		}

		i = 0;

		for (Element e : text) {

			// System.out.println("see here 2 !!!: "+e.getElementsByTag("usdept").text());
			usDept[i] = e.getElementsByTag("usdept").text();

			// System.out.println("see here 3 !!!: "+e.getElementsByTag("agency").text());
			agency[i] = e.getElementsByTag("agency").text();

			// System.out.println("see here 4 !!!: "+e.getElementsByTag("usbureau").text());
			usBureau[i] = e.getElementsByTag("usbureau").text();

			// System.out.println("see here 5 !!!: "+e.getElementsByTag("doctitle").text());
			docTitle[i] = e.getElementsByTag("doctitle").text();

			// System.out.println("see here 6!!!: "+e.getElementsByTag("summary").text());
			summary[i] = e.getElementsByTag("summary").text();

			// System.out.println("see here 7 !!!: "+e.getElementsByTag("supplem").text());
			supplem[i] = e.getElementsByTag("supplem").text();

			// id[i].setOther(usDept[i], agency[i], usBureau[i], docTitle[i], summary[i],
			// supplem[i]);
			i++;
		}
		docmnt.select("usdept").remove();
		docmnt.select("agency").remove();
		docmnt.select("usbureau").remove();
		docmnt.select("doctitle").remove();
		docmnt.select("summary").remove();
		docmnt.select("supplem").remove();

		i = 0;

		for (Element e : text) {

			// System.out.println("see here 8!!!: "+e.getElementsByTag("text").text());

			Other[i] = e.getElementsByTag("text").text();
			frDoc.put("docno", docNo[i]);
			frDoc.put("usdept", usDept[i]);
			frDoc.put("agency", agency[i]);
			frDoc.put("usbureau", usBureau[i]);
			frDoc.put("doctitle", docTitle[i]);
			frDoc.put("summary", summary[i]);
			frDoc.put("supplem", supplem[i]);
			frDoc.put("other", Other[i]);
			frDocs.add(new JSONObject(frDoc).toString() + ",");

			// id[i].setOtherText(Other[i]);
			i++;

		}
		Files.write(frPath, frDocs, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

	}

	public static void main(String[] args) throws Exception {
		Utils utils = new Utils();

		ParseFR94 fr94 = new ParseFR94();
		File[] files = new File("./contents/AssignmentTwo/AssignmentTwo/fr94/").listFiles();
		System.out.println("Storing parsed FT doc...");
		utils.deleteDir(new File("outputs/parsed_docs/fr94.json"));
		fr94.fileAcces(files);
		Path frPath = Paths.get("outputs/parsed_docs/fr94.json");
		Files.write(frPath, "]".getBytes(), StandardOpenOption.APPEND);
		System.out.println("Storing done!\n");

	}

}