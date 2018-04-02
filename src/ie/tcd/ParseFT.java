package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class ParseFT {

	/**
	 * Parse the line which has opening and closing element on same line
	 * 
	 * @param partitions
	 *            array of sub-strings in line split by >
	 * @param elementName
	 *            name of element in the line
	 * @param line
	 *            contents of the line
	 * @param lineNumber
	 *            line number which has to be parsed
	 * @param file
	 *            file where the line is present
	 * @return value of element after parsing
	 */
	private String parseSameLineElement(String[] partitions, String elementName, String line, int lineNumber,
			File file) {

		if (partitions.length > 2)
			Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
		else {

			if (partitions[1].contains("</" + elementName) && partitions[1].endsWith("</" + elementName)) {

				return partitions[1].split("</" + elementName)[0].trim();
			} else
				Errors.printMalformedErrorAndExit(line, lineNumber, file);
		}
		return null;
	}

	/**
	 * Parse Financial Times data
	 * 
	 * @param ftDirectoryStr
	 *            list of file locations to parse
	 * @param hasSubdirectory
	 *            true, if FT data has files in a subdirectory
	 * @return a list of parsed map FT data
	 * @throws IOException
	 *             when file is not present
	 */
	public List<String> parse(String ftDirectoryStr, boolean hasSubdirectory) throws IOException {

		List<String> ftDocs = new ArrayList<String>();
		List<File> filesList = (new Utils()).getFiles(ftDirectoryStr, hasSubdirectory);

		Utils utils = new Utils();
		Map<String, String> ftDoc = null;
		int docCount = 0;
		int fileCount = 0;
		// Loop through files
		System.out.println("Number of files: " + Integer.toString(filesList.size()));
		for (File file : filesList) {
			
			System.out.println("FT File: " + Integer.toString(++fileCount) + " / " + Integer.toString(filesList.size()));
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			int lineNumber = 0;

			boolean docInProgress = false;
			boolean dateInProgress = false;
			boolean headlineInProgress = false;
			boolean textInProgress = false;
			boolean pubInProgress = false;
			boolean pageInProgress = false;
			boolean bylineInProgress = false;
			boolean datelineInProgress = false;
			boolean xxInProgress = false;
			boolean coInProgress = false;
			boolean cnInProgress = false;
			boolean inInProgress = false;
			boolean tpInProgress = false;
			boolean peInProgress = false;
			boolean typo = false;

			String xxNext = "";

			String[] elements = { "date", "dateline", "in", "profile", "cn", "docno", "co", "pe", "text", "page", "tp",
					"pub", "headline", "byline" };

			// Loop through each line
			while ((line = bufferedReader.readLine()) != null) {

				lineNumber++;
				line = line.trim();
				char firstChar = line.charAt(0);

				// Check if line starts with <
				if (firstChar == '<') {

					// Check if line start with </
					if (line.charAt(1) == '/') {

						boolean endsWithGt = line.endsWith(">");
						String[] partitions = line.split(">");
						if (partitions.length == 1 && !endsWithGt)
							Errors.printMalformedErrorAndExit(line, lineNumber, file);
						else {

							String firstPartition = partitions[0];
							String element = firstPartition.substring(2, firstPartition.length());

							// Identify element and execute corresponding logic
							switch (element) {

							case "DOC": // line starts with </DOC>

								if (docInProgress) {

									docInProgress = false;
									if (partitions.length > 1)
										Errors.printUnexpectedErrorAndExit(line, lineNumber, file);

									for (int i = 0; i < elements.length; i++) {
										
										String elementValue = ftDoc.get(elements[i]);
										if (elementValue != null) {
											
											elementValue = elementValue.trim();
											elementValue = elementValue.replaceAll("&amp", "&");
											elementValue = elementValue.replaceAll("&gt", ">");
											elementValue = elementValue.replaceAll("&lt", "<");
											elementValue = elementValue.replaceAll(" +", " ");
											ftDoc.put(elements[i], elementValue);
										}
									}
									
									ftDocs.add(new JSONObject(ftDoc).toString() + ",");
									ftDoc = null;
									docCount++;
									xxNext = "";
								} else
									Errors.printUnopenedElementErrorAndExit(element, lineNumber, file);
								break;

							case "DOCNO": // line starts with </DOCNO>
							case "PROFILE": // line starts with </PROFILE>

								Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
								break;

							case "DATE": // line starts with </DATE>
							case "HEADLINE": // line starts with </HEADLINE>
							case "TEXT": // line starts with </TEXT>
							case "PUB": // line starts with </PUB>
							case "PAGE": // line starts with </PAGE>
							case "BYLINE": // line starts with </BYLINE>
							case "DATELINE": // line starts with </DATELINE>
							case "XX": // line starts with </XX>
							case "CO": // line starts with </CO>
							case "CN": // line starts with </CN>
							case "IN": // line starts with </IN>
							case "TP": // line starts with </TP>
							case "PE": // line starts with </PE>

								if (docInProgress) {

									if (element.contentEquals("DATE") && dateInProgress) {

										dateInProgress = false;
										if (typo) {

											headlineInProgress = false;
											typo = false;
										}
									} else if (element.contentEquals("HEADLINE") && headlineInProgress)
										headlineInProgress = false;
									else if (element.contentEquals("TEXT") && textInProgress)
										textInProgress = false;
									else if (element.contentEquals("PUB") && pubInProgress)
										pubInProgress = false;
									else if (element.contentEquals("PAGE") && pageInProgress)
										pageInProgress = false;
									else if (element.contentEquals("BYLINE") && bylineInProgress)
										bylineInProgress = false;
									else if (element.contentEquals("DATELINE") && datelineInProgress)
										datelineInProgress = false;
									else if (element.contentEquals("XX") && xxInProgress)
										xxInProgress = false;
									else if (element.contentEquals("CO") && coInProgress)
										coInProgress = false;
									else if (element.contentEquals("CN") && cnInProgress)
										cnInProgress = false;
									else if (element.contentEquals("IN") && inInProgress)
										inInProgress = false;
									else if (element.contentEquals("TP") && tpInProgress)
										tpInProgress = false;
									else if (element.contentEquals("PE") && peInProgress)
										peInProgress = false;
									else
										Errors.printUnopenedElementErrorAndExit(element, lineNumber, file);

									if (partitions.length > 1)
										Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
									if (!partitions[0].contentEquals("</" + element))
										Errors.printMalformedErrorAndExit(line, lineNumber, file);
								} else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							default:
								Errors.printCantParseErrorAndExit(line, lineNumber, file);
							}
						}
					} else { // In this block, line starts with < and not </

						boolean endsWithGt = line.endsWith(">");
						String[] partitions = line.split(">");
						if (partitions.length == 1 && !endsWithGt)
							Errors.printMalformedErrorAndExit(line, lineNumber, file);
						else {

							String firstPartition = partitions[0];
							String element = firstPartition.substring(1, firstPartition.length());

							// Identify element and execute corresponding logic
							switch (element) {

							case "DOC": // line starts with <DOC>

								if (!docInProgress) {

									docInProgress = true;
									if (partitions.length > 1)
										Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
									ftDoc = new HashMap<String, String>();
								} else
									Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								break;

							case "DOCNO": // line starts with <DOCNO>
							case "PROFILE": // line starts with <PROFILE>

								if (docInProgress)
									ftDoc.put(element.toLowerCase(),
											parseSameLineElement(partitions, element, line, lineNumber, file));
								else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							case "DATE": // line starts with <DATE>
							case "PUB": // line starts with <PUB>
							case "CO": // line starts with <CO>
							case "CN": // line starts with <CN>
							case "IN": // line starts with <IN>
							case "TP": // line starts with <TP>
							case "PE": // line starts with <PE>

								if (docInProgress) {

									boolean changeDate = element.contentEquals("DATE") && !dateInProgress;
									boolean changePub = element.contentEquals("PUB") && !pubInProgress;
									boolean changeCo = element.contentEquals("CO") && !coInProgress;
									boolean changeCn = element.contentEquals("CN") && !cnInProgress;
									boolean changeIn = element.contentEquals("IN") && !inInProgress;
									boolean changeTp = element.contentEquals("TP") && !tpInProgress;
									boolean changePe = element.contentEquals("PE") && !peInProgress;

									if (changeDate || changePub || changeCo || changeCn || changeIn || changeTp
											|| changePe) {

										if (partitions.length > 2)
											Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
										else {

											if (changeDate) {

												try {

													Integer.parseInt(partitions[1]);
													dateInProgress = true;
												} catch (NumberFormatException e) {

													Errors.printMalformedErrorAndExit(line, lineNumber, file);
												}
											} else if (changePub)
												pubInProgress = true;
											else if (changeCo) {

												if (xxNext.contentEquals("Companies"))
													coInProgress = true;
												else
													Errors.printXXDoesNotMatchElement(xxNext, "Companies", lineNumber,
															file);
											} else if (changeCn) {

												if (xxNext.contentEquals("Countries"))
													cnInProgress = true;
												else
													Errors.printXXDoesNotMatchElement(xxNext, "Countries", lineNumber,
															file);
											} else if (changeIn) {

												if (xxNext.contentEquals("Industries"))
													inInProgress = true;
												else
													Errors.printXXDoesNotMatchElement(xxNext, "Industries", lineNumber,
															file);
											} else if (changeTp) {

												if (xxNext.contentEquals("Types"))
													tpInProgress = true;
												else
													Errors.printXXDoesNotMatchElement(xxNext, "Types", lineNumber,
															file);
											} else if (changePe) {

												if (xxNext.contentEquals("People"))
													peInProgress = true;
												else
													Errors.printXXDoesNotMatchElement(xxNext, "People", lineNumber,
															file);
											}

											String part = partitions[1];
											if (changeCo || changeCn || changeIn || changeTp || changePe)
												part += " ";
											ftDoc.put(element.toLowerCase(), part);
										}
									} else
										Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								} else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							case "HEADLINE": // line starts with <HEADLINE>
							case "TEXT": // line starts with <TEXT>
							case "PAGE": // line starts with <PAGE>
							case "BYLINE": // line starts with <BYLINE>
							case "DATELINE": // line starts with <DATELINE>
							case "XX": // line starts with <XX>

								if (docInProgress) {

									if (element.contentEquals("HEADLINE") && !headlineInProgress)
										headlineInProgress = true;
									else if (element.contentEquals("TEXT") && !textInProgress)
										textInProgress = true;
									else if (element.contentEquals("PAGE") && !pageInProgress)
										pageInProgress = true;
									else if (element.contentEquals("BYLINE") && !bylineInProgress)
										bylineInProgress = true;
									else if (element.contentEquals("DATELINE") && !datelineInProgress)
										datelineInProgress = true;
									else if (element.contentEquals("XX") && !xxInProgress)
										xxInProgress = true;
									else
										Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);

									if (partitions.length > 1)
										Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
								} else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							default:
								Errors.printCantParseErrorAndExit(line, lineNumber, file);
							}
						}
					}
				} else { // if line does not start with <

					if (docInProgress) { // Store contents inside DOC

						if (headlineInProgress)
							utils.storeContentInMap(ftDoc, line, "headline");
						else if (textInProgress)
							utils.storeContentInMap(ftDoc, line, "text");
						else if (pageInProgress)
							utils.storeContentInMap(ftDoc, line, "page");
						else if (bylineInProgress)
							utils.storeContentInMap(ftDoc, line, "byline");
						else if (datelineInProgress)
							utils.storeContentInMap(ftDoc, line, "dateline");
						else if (xxInProgress)
							xxNext = line.split(":-")[0];
						else if (coInProgress)
							utils.storeContentInMap(ftDoc, line, "co");
						else if (cnInProgress)
							utils.storeContentInMap(ftDoc, line, "cn");
						else if (inInProgress)
							utils.storeContentInMap(ftDoc, line, "in");
						else if (tpInProgress)
							utils.storeContentInMap(ftDoc, line, "tp");
						else if (peInProgress)
							utils.storeContentInMap(ftDoc, line, "pe");
						else {

							// Special case as file .\content\Assignment Two\Assignment
							// Two\ft\ft923\ft923_39 contains a typo
							if (line.contentEquals("-{EADLANE-") && dateInProgress) {

								typo = true;
								headlineInProgress = true;
							} else
								Errors.printCantParseErrorAndExit(line, lineNumber, file);
						}
					} else
						Errors.printCantParseErrorAndExit(line, lineNumber, file);
				}
			}
			bufferedReader.close();
//			if (docCount >= 1) break;
		}
		System.out.println("Number of documents: " + Integer.toString(docCount));
		return ftDocs;
	}
}
