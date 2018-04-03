package ie.tcd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class ParseFBIS2 {

	/**
	 * Parse the line which has opening and closing element on same line
	 * 
	 * @param fbisDoc
	 *            the document where the contents are stored in JSON
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
	private String parseSameLineElement(Map<String, String> fbisDoc, String[] partitions, String elementName,
			String line, int lineNumber, File file) {

		if (partitions.length > 2) {

			// Special cases
			if (elementName.contentEquals("HT") && partitions.length == 6)
				return partitions[3].split("</")[0].trim();
			else if (elementName.contentEquals("F P=105") && partitions.length == 4) {

				if (partitions[0].contentEquals(partitions[2]) && partitions[1].contentEquals(partitions[3]))
					return partitions[1].split("</F")[0].trim();
			} else if (elementName.contentEquals("F P=106") && partitions.length == 3) {

				Utils utils = new Utils();
				utils.storeContentInMap(fbisDoc, partitions[2], "text");
				return partitions[1].split("</F")[0].trim();
			} else
				Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
		} else {

			String endElementName = elementName;
			if (elementName.contains("F P=10"))
				endElementName = "F";
			if (partitions[1].contains("</" + endElementName) && partitions[1].endsWith("</" + endElementName)) {

				if (partitions[1].split("</" + endElementName).length == 0)
					return "";
				else
					return partitions[1].split("</" + endElementName)[0].trim();
			} else
				Errors.printMalformedErrorAndExit(line, lineNumber, file);
		}
		return null;
	}

	/**
	 * Parse Foreign Broadcast Information Service data
	 * 
	 * @param fbisDirectoryStr
	 *            list of file locations to parse
	 * @param hasSubdirectory
	 *            true, if FBIS data has files in a subdirectory
	 * @return a list of parsed map FBIS data
	 * @throws IOException
	 *             when file is not present
	 */
	public List<String> parse(String fbisDirectoryStr, boolean hasSubdirectory) throws IOException {

		List<String> fbisDocs = new ArrayList<String>();
		List<File> filesList = (new Utils()).getFiles(fbisDirectoryStr, hasSubdirectory);

		Utils utils = new Utils();
		Map<String, String> fbisDoc = null;
		int docCount = 0;
		int fileCount = 0;
		// Loop through files
		System.out.println("Number of files: " + Integer.toString(filesList.size()));
		for (File file : filesList) {

			System.out.println("FBIS File: " + Integer.toString(++fileCount) + " / " + Integer.toString(filesList.size()));
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			int lineNumber = 0;

			boolean docInProgress = false;
			boolean headerInProgress = false;
			boolean textInProgress = false;
			boolean h3InProgress = false;
			boolean h4InProgress = false;
			boolean fp102InProgress = false;
			boolean fp104InProgress = false;

			String[] headerNext = { "H2", "AU" };
			String[] elements = { "docno", "ht", "header", "date1", "h1", "h2", "h3", "h4", "h5", "h6", "text", "au",
					"fp100", "fp101", "fp102", "fp103", "fp104", "fp105", "fp106", "abs" };

			// Loop through each line
			while ((line = bufferedReader.readLine()) != null) {

				lineNumber++;
				line = line.trim();

				// Special case
				line = line.replaceAll("<3>", "<H3>");
				line = line.replaceAll("</3>", "</H3>");

				// Ignoring <TI> elements
				line = line.replaceAll("<TI>", "");
				line = line.replaceAll("</TI>", "");

				// TODO: Store FIG elements, and store elements who are children to TEXT.

				// Ignore <FIG> elements for now
				if (line.startsWith("<FIG"))
					continue;

				if (textInProgress)
					line = line.replaceAll("<H1>", "").replaceAll("</H1>", "").replaceAll("<H2>", "")
							.replaceAll("</H2>", "").replaceAll("<H3>", "").replaceAll("</H3>", "")
							.replaceAll("<H4>", "").replaceAll("</H4>", "").replaceAll("<H5>", "")
							.replaceAll("</H5>", "").replaceAll("<H6>", "").replaceAll("</H6>", "")
							.replaceAll("<H7>", "").replaceAll("</H7>", "").replaceAll("<H8>", "")
							.replaceAll("</H8>", "").replaceAll("<TR>", "").replaceAll("</TR>", "")
							.replaceAll("<TXT5>", "").replaceAll("</TXT5>", "");

				if (line.contentEquals(""))
					continue;
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

									Map<String, String> fbisDoc2 = new HashMap<String, String>();
									for (int i = 0; i < elements.length; i++) {

										String elementValue = fbisDoc.get(elements[i]);
										if (elementValue != null) {

											elementValue = elementValue.trim();
											elementValue = elementValue.replaceAll("&amp", "&");
											elementValue = elementValue.replaceAll("&gt", ">");
											elementValue = elementValue.replaceAll("&lt", "<");
											elementValue = elementValue.replaceAll(" +", " ");
											if (elementValue.contentEquals(""))
												fbisDoc2.put(elements[i], elementValue);

											// DEBUG
											// if ((elementValue.contains("<") || elementValue.contains("</")) &&
											// elementValue.contains(">")) {
											// System.out.println(elementValue);
											// System.out.println("************");
											// }
										}
									}

									fbisDocs.add(new JSONObject(fbisDoc2).toString() + ",");
									fbisDoc = null;
									docCount++;
								} else
									Errors.printUnopenedElementErrorAndExit(element, lineNumber, file);
								break;

							case "DOCNO": // line starts with </DOCNO>
							case "HT": // line starts with </HT>

								Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
								break;

							case "HEADER": // line starts with </HEADER>
							case "TEXT": // line starts with </TEXT>

								if (docInProgress) {

									if (element.contentEquals("HEADER") && headerInProgress)
										headerInProgress = false;
									else if (element.contentEquals("TEXT") && textInProgress)
										textInProgress = false;
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
									fbisDoc = new HashMap<String, String>();
								} else
									Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								break;

							case "DOCNO": // line starts with <DOCNO>
							case "HT": // line starts with <HT>
							case "F P=100": // line starts with <F P=100>
							case "F P=101": // line starts with <F P=101>
							case "F P=103": // line starts with <F P=103>
							case "F P=105": // line starts with <F P=105>
							case "F P=106": // line starts with F P=106>

								if (docInProgress)
									fbisDoc.put(element.replaceAll(" ", "").replaceAll("=", "").toLowerCase(),
											parseSameLineElement(fbisDoc, partitions, element, line, lineNumber, file));
								else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							case "H1": // line starts with <H1>
							case "H2": // line starts with <H2>
							case "H5": // line starts with <H5>
							case "H6": // line starts with <H6>
							case "DATE1": // line starts with <DATE1>
							case "AU": // line starts with <AU>
							case "ABS": // line starts with <ABS>

								if (docInProgress)

									if (element.contentEquals("H1") || element.contentEquals("H5")
											|| element.contentEquals("H6") || element.contentEquals("DATE1")
											|| element.contentEquals("ABS"))
										fbisDoc.put(element.toLowerCase(), parseSameLineElement(fbisDoc, partitions,
												element, line, lineNumber, file));
									else if (headerInProgress)
										if (Arrays.asList(headerNext).contains(element))
											fbisDoc.put(element.toLowerCase(), parseSameLineElement(fbisDoc, partitions,
													element, line, lineNumber, file));
										else
											Errors.printOneOfExpectedElements(headerNext, "header", element, lineNumber,
													file);
									else
										Errors.printUnopenedElementErrorAndExit("header", lineNumber, file);
								else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							case "H3": // line starts with <H3>
							case "H4": // line starts with <H4>
							case "F P=102": // line starts with <F P=102>
							case "F P=104": // line starts with <F P=104>

								if (docInProgress) {

									boolean changeH3 = element.contentEquals("H3") && !h3InProgress;
									boolean changeH4 = element.contentEquals("H4") && !h4InProgress;
									boolean changeFP102 = element.contentEquals("F P=102") && !fp102InProgress;
									boolean changeFP104 = element.contentEquals("F P=104") && !fp104InProgress;

									if (changeH3 || changeH4 || changeFP102 || changeFP104) {

										if (partitions.length > 2)
											Errors.printUnexpectedErrorAndExit(line, lineNumber, file);
										else {

											if (changeH3)
												h3InProgress = true;
											else if (changeH4)
												h4InProgress = true;
											else if (changeFP102)
												fp102InProgress = true;
											else if (changeFP104)
												fp104InProgress = true;

											if (line.endsWith(">")) {

												if (changeH3)
													h3InProgress = false;
												else if (changeH4)
													h4InProgress = false;
												else if (changeFP102)
													fp102InProgress = false;
												else if (changeFP104)
													fp104InProgress = false;
												else
													Errors.printUnexpectedErrorAndExit(line, lineNumber, file);

												fbisDoc.put(
														element.replaceAll(" ", "").replaceAll("=", "").toLowerCase(),
														parseSameLineElement(fbisDoc, partitions, element, line,
																lineNumber, file));
											} else
												utils.storeContentInMap(fbisDoc, partitions[1] + " ",
														element.replaceAll(" ", "").replaceAll("=", "").toLowerCase());
										}
									} else
										Errors.printUnclosedElementErrorAndExit(element, lineNumber, file);
								} else
									Errors.printUnopenedDocErrorAndExit(element, lineNumber, file);
								break;

							case "HEADER": // line starts with <HEADER>
							case "TEXT": // line starts with <TEXT>

								if (docInProgress) {

									if (element.contentEquals("HEADER") && !headerInProgress)
										headerInProgress = true;
									else if (element.contentEquals("TEXT") && !textInProgress)
										textInProgress = true;
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

						if (h3InProgress) {

							String value = line;
							if (line.endsWith("</H3>")) {

								h3InProgress = false;
								value = line.split("</H3>")[0];
							}
							utils.storeContentInMap(fbisDoc, value, "h3");
						} else if (h4InProgress) {

							String value = line;
							if (line.endsWith("</H4>")) {

								h4InProgress = false;
								value = line.split("</H4>")[0];
							}
							utils.storeContentInMap(fbisDoc, value, "h4");
						} else if (headerInProgress)
							utils.storeContentInMap(fbisDoc, line, "header");
						else if (fp102InProgress) {

							String value = line;
							if (line.endsWith("</F>")) {

								fp102InProgress = false;
								value = line.split("</F>")[0];
							}
							utils.storeContentInMap(fbisDoc, value, "fp102");
						} else if (fp104InProgress) {

							String value = line;
							if (line.endsWith("</F>")) {

								fp104InProgress = false;
								value = line.split("</F>")[0];
							}
							utils.storeContentInMap(fbisDoc, value, "fp104");
						} else if (textInProgress) {

							if (line.startsWith("Language: <F P=105>")) {

								line = line.split("Language: ")[1];
								String element = "F P=105";
								String[] partitions = line.split(">");
								fbisDoc.put(element.replaceAll(" ", "").replaceAll("=", "").toLowerCase(),
										parseSameLineElement(fbisDoc, partitions, element, line, lineNumber, file));

							} else
								utils.storeContentInMap(fbisDoc, line, "text");
						} else
							Errors.printCantParseErrorAndExit(line, lineNumber, file);
					} else
						Errors.printCantParseErrorAndExit(line, lineNumber, file);
				}
			}
			bufferedReader.close();
		}
		System.out.println("Number of documents: " + Integer.toString(docCount));
		return fbisDocs;
	}
}
