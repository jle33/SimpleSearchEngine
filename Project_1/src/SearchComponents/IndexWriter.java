package SearchComponents;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

	private String mFolderPath;
	private static List<Float> Wd = new ArrayList<Float>();

	/**
   Constructs an IndexWriter object which is prepared to index the given folder.
	 */
	public IndexWriter(String folderPath) {
		mFolderPath = folderPath;
	}

	/**
   Builds and writes an inverted index to disk. Creates three files: 
   vocab.bin, containing the vocabulary of the corpus; 
   postings.bin, containing the postings list of document IDs;
   vocabTable.bin, containing a table that maps vocab terms to postings locations
	 */
	public void buildIndex() {
		buildIndexForDirectory(mFolderPath);
	}

	/**
   Builds the normal NaiveInvertedIndex for the folder.
	 */
	private static void buildIndexForDirectory(String folder) {
		PositionalInvertedIndex index = new PositionalInvertedIndex();

		// Index the directory using a naive index
		indexFiles(folder, index);
		
		// Finalize index statistics 
		index.finalize();

		// at this point, "index" contains the in-memory inverted index 
		// now we save the index to disk, building three files: the postings index,
		// the vocabulary list, and the vocabulary table.

		// the array of terms
		String[] dictionary = index.getDictionary();
		// an array of positions in the vocabulary file
		long[] vocabPositions = new long[dictionary.length];

		buildVocabFile(folder, dictionary, vocabPositions);
		buildPostingsFile(folder, index, dictionary, vocabPositions);
		buildWeightsFile(folder, Wd);
		buildStatsFile(folder, index);
	}

	
	/**
   Builds the postings.bin file for the indexed directory, using the given
   NaiveInvertedIndex of that directory.
	 */
	private static void buildPostingsFile(String folder, PositionalInvertedIndex index,
			String[] dictionary, long[] vocabPositions) {
		FileOutputStream postingsFile = null;
		try {
			postingsFile = new FileOutputStream(
					new File(folder, "postings.bin")
					);

			// simultaneously build the vocabulary table on disk, mapping a term index to a
			// file location in the postings file.
			FileOutputStream vocabTable = new FileOutputStream(
					new File(folder, "vocabTable.bin")
					);

			// the first thing we must write to the vocabTable file is the number of vocab terms.
			byte[] tSize = ByteBuffer.allocate(4)
					.putInt(dictionary.length).array();
			vocabTable.write(tSize, 0, tSize.length);
			//create b plus tree stuff
			BPlusTree bptree = new BPlusTree();
			bptree.initialize(folder);
			int vocabI = 0;
			for (String s : dictionary) {
				// for each String in dictionary, retrieve its postings.
				List<Integer> postings = index.getPostings(s);
				//Sort postings
				Collections.sort(postings);
				// write the vocab table entry for this term: the byte location of the term in the vocab list file,
				// and the byte location of the postings for the term in the postings file.
				byte[] vPositionBytes = ByteBuffer.allocate(8)
						.putLong(vocabPositions[vocabI]).array();

				vocabTable.write(vPositionBytes, 0, vPositionBytes.length);

				byte[] pPositionBytes = ByteBuffer.allocate(8)
						.putLong(postingsFile.getChannel().position()).array();

				//store b plus tree stuff
				bptree.store(s, postingsFile.getChannel().position());

				vocabTable.write(pPositionBytes, 0, pPositionBytes.length);

				// write the postings file for this term. first, the document frequency for the term, then
				// the document IDs, encoded as gaps.
				byte[] docFreqBytes = ByteBuffer.allocate(4)
						.putInt(postings.size()).array();
				postingsFile.write(docFreqBytes, 0, docFreqBytes.length);

				int lastDocId = 0;
				for (int docId : postings) {
					if(docId < lastDocId){
						System.out.println("ERROR NOT IN ORDER docIDs: " + docId + " and prev " + lastDocId);
					}
					byte[] docIdBytes = ByteBuffer.allocate(4)
							.putInt(docId - lastDocId).array(); // encode a gap, not a doc ID

					postingsFile.write(docIdBytes, 0, docIdBytes.length);
					lastDocId = docId;


					//Add posFreq than positions
					List<Integer> positions = index.getTermPositions(s, docId);
					byte[] posFreqBytes = ByteBuffer.allocate(4)
							.putInt(positions.size()).array();
					postingsFile.write(posFreqBytes, 0, posFreqBytes.length);

					//Add positions using gaps instead
					int lastPosID = 0;
					for(int posID : positions){
						if(posID < lastPosID){
							System.out.println("ERROR NOT IN ORDER termPos: " + posID + " and prev " + lastPosID);
						}
						byte[] curPosBytes = ByteBuffer.allocate(4)
								.putInt(posID - lastPosID).array();

						postingsFile.write(curPosBytes, 0, curPosBytes.length);
						lastPosID = posID;
					}


				}

				vocabI++;
			}
			vocabTable.close();
			postingsFile.close();
			//close bptree
			bptree.close();
		}
		catch (FileNotFoundException ex) {
		}
		catch (IOException ex) {
		} catch (Exception e) {
			//b+ tree exception
			e.printStackTrace();
		}
		finally {
			try {
				postingsFile.close();
			}
			catch (IOException ex) {
			}
		}
	}

	private static void buildVocabFile(String folder, String[] dictionary,
			long[] vocabPositions) {
		OutputStreamWriter vocabList = null;
		try {
			// first build the vocabulary list: a file of each vocab word concatenated together.
			// also build an array associating each term with its byte location in this file.
			int vocabI = 0;
			vocabList = new OutputStreamWriter(
					new FileOutputStream(new File(folder, "vocab.bin")), "ASCII"
					);

			int vocabPos = 0;
			for (String vocabWord : dictionary) {
				// for each String in dictionary, save the byte position where that term will start in the vocab file.
				vocabPositions[vocabI] = vocabPos;
				vocabList.write(vocabWord); // then write the String
				vocabI++;
				vocabPos += vocabWord.length();
			}

		}
		catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
		catch (UnsupportedEncodingException ex) {
			System.out.println(ex.toString());
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		finally {
			try {
				vocabList.close();
			}
			catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
	}
	
	private static void buildWeightsFile(String folder, List<Float> dWeights) {
		FileOutputStream weightsFile = null;
		try{
			weightsFile = new FileOutputStream(new File(folder, "docWeights.bin"));
			
			// add the document weights to the file: first the number of documents, then their weights
			byte[] WeightBytes = ByteBuffer.allocate(4).putInt(Wd.size()).array();
			//System.out.println("Wd size = " + Wd.size());
			weightsFile.write(WeightBytes, 0, WeightBytes.length);
			for (int i = 0; i < Wd.size(); i++){
				WeightBytes = ByteBuffer.allocate(4).putFloat(dWeights.get(i)).array();
				weightsFile.write(WeightBytes, 0, WeightBytes.length);
			}
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		finally{
			try{
				weightsFile.close();
			}
			catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}

	}
	
	private static void buildStatsFile(String folder, PositionalInvertedIndex index){
		FileOutputStream statsFile = null;
		try{
			statsFile = new FileOutputStream(new File(folder, "stats.bin"));
			
			// add each piece of the statistics in order 
			byte[] StatBytes = ByteBuffer.allocate(8).putInt(index.getNumTypes()).array();	// number of types
			statsFile.write(StatBytes, 0, StatBytes.length);
			StatBytes = ByteBuffer.allocate(8).putInt(index.getNumTerms()).array();			// number of terms
			statsFile.write(StatBytes, 0, StatBytes.length);
			StatBytes = ByteBuffer.allocate(8).putDouble(index.getAvgPosts()).array();		// number of avergae docs per post
			statsFile.write(StatBytes, 0, StatBytes.length);
			StatBytes = ByteBuffer.allocate(8).putInt(index.getTotalIndexSize()).array();	// approximate size of index
			statsFile.write(StatBytes, 0, StatBytes.length);
			
			// get top term frequencies
			double[] termFreq = index.getTopTermFreq();
			StatBytes = ByteBuffer.allocate(8).putInt(termFreq.length).array();				// number of frequencies
			statsFile.write(StatBytes, 0, StatBytes.length);
			for(double frequency : termFreq){
				StatBytes = ByteBuffer.allocate(8).putDouble(frequency).array();			// frequency of term, no name
				statsFile.write(StatBytes, 0, StatBytes.length);
			}
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		finally{
			try{
				statsFile.close();
			}
			catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
	}

	private static void indexFiles(String folder, final PositionalInvertedIndex index) {
		int documentID = 0;
		final Path currentWorkingPath = Paths.get(folder).toAbsolutePath();

		try {
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
						// System.out.println("Indexing file " + file.getFileName());


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
		}
		catch (IOException ex) {
			Logger.getLogger(IndexWriter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void indexFile(File fileName, PositionalInvertedIndex index,
			int documentID) {

		try {
			AdvancedTokenStream stream = new AdvancedTokenStream(fileName);
			HashMap<String, Integer> mTFtd = new HashMap<String, Integer>();	// map of term frequency for current document
			Float[] Wdt;
			int curPos = 0;
			while (stream.hasNextToken()) {
				String term = stream.nextToken();
				index.addType(term);				
				String stemmed = PorterStemmer.processToken(term);

				if (stemmed != null && stemmed.length() > 0) {
					index.addTerm(stemmed, documentID, curPos++, mTFtd);
				}
			}
			// calculate the Wd weight of current document
			Integer[] temp = mTFtd.values().toArray(new Integer[0]);		// collect the TFtd values
			Wdt = new Float[temp.length];
			int i = 0;
			for(Integer kd : temp){
				Wdt[i] = temp[i].floatValue();
				i++;
			}
			float valueWd = 0;
			for (int x = 0; x < Wdt.length; x++){				// find the sum of Wdt values based on TFtd
				Wdt[x] = (float) (1 + Math.log(Wdt[x]));		// convert TFtd to Wdt value
				valueWd += (float) Math.pow(Wdt[x], 2);
			}
			Wd.add((float) Math.sqrt(valueWd));
			//System.out.println(Wd.size());
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
