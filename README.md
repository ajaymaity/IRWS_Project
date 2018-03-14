IRWS Project - Group 5
==

Download data from web
--
* Go to this [url](https://drive.google.com/file/d/1MudJity9Ckh8jxapFx3OS-DLEkcvbYYx/view?nbsp) and click on Download at top right.
* (Optional) Keep the ZIP file inside newly created ```./content/``` folder in the project root.
* UNZIP the file.
* (Optional) You may UNZIP the file, such that the data folders: *dtds*, *fbis*, *fr94*, *ft*, and *latimes* are inside the directory: ```./content/Assignment Two/Assignment Two/```.

Configure the project in Eclipse
--
* Import the folder into Eclipse as a General project.
* Convert the project into Java project by changing its facet.

Parse the documents
--
* Run ParseDocs.java inside ```./src/ie/tcd/```. The code parses the FT documents, and stores the parsed content in ```/outputs/parsed_docs/ft.json```.
* You may specify an optional data directory using the following command line arguments:
```java ParseDocs --data <data_dir>```.