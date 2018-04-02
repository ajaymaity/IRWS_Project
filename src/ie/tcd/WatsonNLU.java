package ie.tcd;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;

public class WatsonNLU {

	private String username;
	private String password;
	private NaturalLanguageUnderstanding nluService;
	private Features features;

	public WatsonNLU() throws FileNotFoundException, IOException {

		String folderPath = "credentials/";
		String filePath = folderPath + "watson_nlu.json";
		String nluServiceName = "nlu-irws-project-natural-la-1522527875502";

		Utils utils = new Utils();
		utils.checkIfDirectory(folderPath);
		utils.checkIfFile(filePath);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = null;
		try {

			jsonObj = (JSONObject) jsonParser.parse(new FileReader(filePath));
		} catch (ParseException e) {

			System.out.println("Unable to parse " + filePath + ". Please ensure the format is correct.");
			System.out.println("Exiting application.");
			System.exit(1);
		}

		JSONArray nluJSON = (JSONArray) jsonObj.get("natural-language-understanding");
		JSONObject nluIrws = (JSONObject) nluJSON.get(0);
		if (((String) nluIrws.get("name")).contentEquals(nluServiceName)) {

			JSONObject creds = (JSONObject) nluIrws.get("credentials");
			username = (String) creds.get("username");
			password = (String) creds.get("password");
		} else {

			System.out.println("Unable to identify Watson NLU service. Are you sure the service is correct?");
			System.out.println("Exiting application");
			System.exit(1);
		}

		// Instantiate Watson NLU Service
		nluService = new NaturalLanguageUnderstanding("2018-03-16", username, password);
		EntitiesOptions entities = new EntitiesOptions.Builder().build();
		KeywordsOptions keywords = new KeywordsOptions.Builder().build();
		features = new Features.Builder()
				.entities(entities)
				.keywords(keywords).build();
	}

	public List<Map<String, Long>> analyze(String text) {

		List<Map<String, Long>> valuesList = new ArrayList<Map<String, Long>>();
		Map<String, Long> entitiesMap = new HashMap<String, Long>();
		Map<String, Long> keywordsMap = new HashMap<String, Long>();
		
		AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(text).features(features).build();
		AnalysisResults response = nluService.analyze(parameters).execute();
		
		List<EntitiesResult> entities = response.getEntities();
		List<KeywordsResult> keywords = response.getKeywords();
		for (EntitiesResult entity: entities) 
			entitiesMap.put(entity.getText(), entity.getCount());
		for (KeywordsResult keyword: keywords) 
			keywordsMap.put(keyword.getText(), (long) 0);
		
		valuesList.add(entitiesMap);
		valuesList.add(keywordsMap);
		
		return valuesList;
	}

	// DEBUG
	public static void main(String[] args) throws FileNotFoundException, IOException {

		WatsonNLU wnlu = new WatsonNLU();
//		String text = "401 foreign minorities, Germany What language and cultural differences impede the integration of foreign minorities in Germany? A relevant document will focus on the causes of the lack of integration in a significant way; that is, the mere mention of immigration difficulties is not relevant.  Documents that discuss immigration problems unrelated to Germany are also not relevant.";
		String text = "411. salvaging, shipwreck, treasure. Find information on shipwreck salvaging: the recovery or attempted recovery of treasure from sunken ships. A relevant document will provide information on the actual locating and recovery of treasure; on the technology which makes possible the discovery, location and investigation of wreckages which contain or are suspected of containing treasure; or on the disposition of the recovered treasure.";
		List<Map<String, Long>> valuesList = wnlu.analyze(text);
		System.out.println(valuesList);
	}
}
