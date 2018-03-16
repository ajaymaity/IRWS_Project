IRWS Project - Group 5
==

Download data from web
--
* Go to this [url](https://drive.google.com/file/d/1MudJity9Ckh8jxapFx3OS-DLEkcvbYYx/view?nbsp) and click on Download at top right.
* (Optional) Keep the ZIP file inside newly created ```contents/``` folder in the project root.
* UNZIP the file.
* (Optional) You may UNZIP the file, such that the data folders: *dtds*, *fbis*, *fr94*, *ft*, and *latimes* are inside the directory: ```contents/Assignment Two/Assignment Two/```.

Configure the project in Eclipse
--
* Import the folder into Eclipse as a General project.
* Convert the project into Java project by changing its facet.

Configure JAR libraries for the project
--
* Add External JARS to Eclipse project, all of which are present in ```libs/``` directory.

Parse the documents
--
* Run ```ParseDocs.java``` inside ```src/ie/tcd/```. The code parses the FT documents, and stores the parsed content in ```outputs/parsed_docs/ft.json```.
* You may specify an optional data directory using the following command line arguments:<br/>
```java ParseDocs --data <data_dir>```.

Index the parsed documents
--
* Run ```Indexer.java``` inside ```src/ie/tcd/```. The code indexes the parsed FT documents, and stores the index in ```outputs/indexes/ft.index/```.
* You may specify an optional parsed document directory stored as JSON using the following command line arguments:<br/>
```java Indexer --docs <parsed_doc_file>```.

Parse the topics
--
* Run ```ParseTopics.java``` inside ```src/ie/tcd/```. The code parses the topics, and stores the parsed content in ```outputs/parsed_topics/tops.json```.
* You may specify an optional topic directory using the following command line arguments:<br/>
```java ParseDocs --topics <topic_dir>```.