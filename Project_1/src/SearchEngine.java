import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class SearchEngine {

	/**
	 * 
	 */
	public static void main(String[] args) throws IOException {
		//final Path currentWorkingPath = Paths.get("F:\\CECS 529 Information Retrival\\Homeworks\\Homework 3\\Project_1").toAbsolutePath();
		//System.out.println("Starting");
		//final Path currentWorkingPath = Paths.get("F:\\CECS 529 Information Retrival\\Homeworks\\Homework 3\\Project_1\\New folder").toAbsolutePath();
		
		/*To-Do
		 * Currently it will find the current path and process any documents in it --Fix for finding documents with github.
		 * Will need to change it to asking the user for directoy.
		 */
		System.out.println("Starting");
		final Path initalPath = Paths.get("").toAbsolutePath();
		//Current Test folder
		final Path currentWorkingPath = Paths.get(initalPath.toString() + "//New folder").toAbsolutePath();  
		
		// the Positional index
		final NaiveInvertedIndex index = new NaiveInvertedIndex();

		// the list of file names that were processed
		final List<String> fileNames = new ArrayList<String>();

		// This is our standard "walk through all .txt files" code.
		Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
			int mDocumentID  = 0;

			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) {
				// make sure we only process the current working directory
				if (currentWorkingPath.equals(dir)) {
					return FileVisitResult.CONTINUE;
				}
				return FileVisitResult.SKIP_SUBTREE;
			}

			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) {
				// only process .txt files
				if (file.toString().endsWith(".txt")) {
					// we have found a .txt file; add its name to the fileName list,
					// then index the file and increase the document ID counter.
					System.out.println("Indexing file " + file.getFileName() + " DocID " + mDocumentID);
					fileNames.add(file.getFileName().toString());
					indexFile(file.toFile(), index, mDocumentID);
					mDocumentID++;
				}
				return FileVisitResult.CONTINUE;
			}

			// don't throw exceptions if files are locked/other errors occur
			public FileVisitResult visitFileFailed(Path file,
					IOException e) {

				return FileVisitResult.CONTINUE;
			}

		});
		
		
		printResults(index, fileNames);
	}

	/**
	   Indexes a file by reading a series of tokens from the file, treating each 
	   token as a term, and then adding the given document's ID to the inverted
	   index for the term.
	   @param file a File object for the document to index.
	   @param index the current state of the index for the files that have already
	   been processed.
	   @param docID the integer ID of the current document, needed when indexing
	   each term from the document.
	 */
	private static void indexFile(File file, NaiveInvertedIndex index, 
			int docID) {
		// Construct a SimpleTokenStream for the given File.
		// Read each token from the stream and add it to the index.
		try {
			AdvancedTokenStream readFile = new AdvancedTokenStream(file);
			//Each file will have a new term position counter
			int termPos = 0;
			while(readFile.hasNextToken()){
				/*if hyphenanted token, remove hyphen and create a token, than split
				 *original token into two tokens without a hyphen
				 */
				String curToken;
				if(readFile.isHyphenatedToken()){
					curToken = readFile.nextHyphenToken();
					//Remove hyphens from token and add term to index
					index.addTerm(curToken.replaceAll(" ", ""), docID, termPos);
					termPos++;
					//Steps to process original token into two tokens without hyphen
					AdvancedTokenStream readHyphenToken = new AdvancedTokenStream(curToken);
					while(readHyphenToken.hasNextToken()){
						curToken = readHyphenToken.nextToken();
						index.addTerm(PorterStemmer.processToken(curToken), docID, termPos);
						termPos++;
					}
				}else{
					curToken = readFile.nextToken();
					String stemmedToken = PorterStemmer.processToken(curToken);
					index.addTerm(stemmedToken, docID, termPos);
					termPos++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void printResults(NaiveInvertedIndex index, 
			List<String> fileNames) {

		// TO-DO: print the inverted index.
		// Retrieve the dictionary from the index. (It will already be sorted.)
		// For each term in the dictionary, retrieve the postings list for the
		// term. Use the postings list to print the list of document names that
		// contain the term. (The document ID in a postings list corresponds to 
		// an index in the fileNames list.)

		// Print the postings list so they are all left-aligned starting at the
		// same column, one space after the longest of the term lengths. Example:
		// 
		// as:      document0 document3 document4 document5
		// engines: document1
		// search:  document2 document4  

		String[] termsList;		// list of terms
		int maxTermLength = 0;   // length of the longest term

		// find the length of the longest term
		termsList = index.getDictionary();
		for(int i = 0; i < termsList.length; i++){
			if(termsList[i].length() > maxTermLength){
				maxTermLength = termsList[i].length();
			}
		}

		// print out the terms and the respective postings
		for(int j = 0; j < termsList.length; j++){
			String currentLine = "";
			List<Integer> postings = index.getPostings(termsList[j]);

			// insert term
			int spacesNeeded = maxTermLength + 1 - termsList[j].length();

			// insert spaces to align postings
			currentLine = currentLine + termsList[j] + ":";
			while(spacesNeeded > 0){
				currentLine = currentLine + " ";
				spacesNeeded--;
			}

			// insert associated documents
			for(int k = 0; k < postings.size(); k++){
				Integer docIndex = postings.get(k);	// get the document ID
				List<Integer> positions = index.getTermPositions(termsList[j], docIndex);
				
				currentLine = currentLine + fileNames.get(docIndex) + "<";
				for(int n = 0; n < positions.size(); n++){
					currentLine = currentLine + positions.get(n) + ", ";
				}
				currentLine = currentLine.substring(0, (currentLine.length()-2));
				currentLine = currentLine + "> ";
			}
			currentLine = currentLine.trim();

			System.out.println(currentLine);
		}
	}

}
