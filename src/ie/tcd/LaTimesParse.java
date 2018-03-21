package ie.tcd;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.util.ArrayList;


public class LaTimesParse {
    @SuppressWarnings({ "unchecked", "null" })
    public static void main(String[] args) throws IOException, JSONException {

        File file = new File("./contents/latimes/");

        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            StringBuilder content = new StringBuilder();
            System.out.println("File name : " + files[i]);
            BufferedReader br = new BufferedReader(new FileReader(files[i]));
            ArrayList <String> arr = new ArrayList<String>();
            String st;
            while ((st = br.readLine()) != null){
                content.append(st);
                if(st.startsWith("</DOC>")){
                    arr.add(content.toString());
                    content.setLength(0);
                }
            }

//            System.out.println(arr.size());
            String jsonString = null;
            FileOutputStream out = new FileOutputStream("./outputs/parsed_docs/latimes.json");
            PrintWriter pw = new PrintWriter(out);

            for (String s: arr){
                Document htmlObj = Jsoup.parse(s);
                jsonString = new JSONObject().put("headline", htmlObj.getElementsByTag("headline").text())
                        .put("paragraph", htmlObj.getElementsByTag("p").text())
                        .put("date", htmlObj.getElementsByTag("date").text())
                        .put("text", htmlObj.getElementsByTag("text").text()).toString();
                System.out.println(jsonString);
                try  {
                    pw.println(jsonString);
                    pw.flush();
                    System.out.println("Copying contents to the JSON file...");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }


        }
    }
}
