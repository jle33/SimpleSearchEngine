package SearchComponents;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class DiskPositionalIndex {

	private String mPath;
	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostings;
	private long[] mVocabTable;
	private List<String> mFileNames;
	//Added for bptree
	private BPlusTree bptree;

	public DiskPositionalIndex(String path) {
		try {
			mPath = path;
			mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
			mVocabTable = readVocabTable(path);
			mFileNames = readFileNames(path);

			bptree = new BPlusTree();
			bptree.open(path);
		}
		catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}

	private static int[] readPostingsFromFile(RandomAccessFile postings, 
			long postingsPosition) {
		try {
			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer = new byte[4];
			postings.read(buffer, 0, buffer.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer).getInt();

			// initialize the array that will hold the postings. 
			int[] docIds = new int[documentFrequency];

			// write the following code:
			// read 4 bytes at a time from the file, until you have read as many
			//    postings as the document frequency promised.
			//    
			// after each read, convert the bytes to an int posting. this value
			//    is the GAP since the last posting. decode the document ID from
			//    the gap and put it in the array.
			//
			// repeat until all postings are read.

			int curGap = 0; //First gap is 0
			for(int i = 0; i < documentFrequency; i++){
				postings.read(buffer,0, buffer.length);
				int docID = ByteBuffer.wrap(buffer).getInt() + curGap;
				curGap = docID;
				docIds[i] = docID;

				// read the 4 bytes for the position frequency
				postings.read(buffer, 0, buffer.length);

				// use ByteBuffer to convert the 4 bytes to an int representing the position frequency
				int positionFrequency = ByteBuffer.wrap(buffer).getInt();

				// for now skip reading all positions
				postings.skipBytes(4*positionFrequency); 
			}


			return docIds;
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	public int[] GetPostings(String term) {
		//long postingsPosition = BinarySearchVocabulary(term);
		long bin = BinarySearchVocabulary(term);
		long postingsPosition = bptree.search(term);
		System.out.print("Binary postings position: "+ postingsPosition);
		System.out.print(""+ BPlusTreeSearch(term));
		asserts(bin, postingsPosition);
		if (postingsPosition >= 0) {
			return readPostingsFromFile(mPostings, postingsPosition);
		}
		return null;
	}

	private static termPostingList[] readPostingsPositionsFromFile(RandomAccessFile postings, 
			long postingsPosition) {
		try {
			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer = new byte[4];
			postings.read(buffer, 0, buffer.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer).getInt();

			// initialize the array that will hold the postings. 
			termPostingList[] docIds = new termPostingList[documentFrequency];

			// write the following code:
			// read 4 bytes at a time from the file, until you have read as many
			//    postings as the document frequency promised.
			//    
			// after each read, convert the bytes to an int posting. this value
			//    is the GAP since the last posting. decode the document ID from
			//    the gap and put it in the array.
			//
			// repeat until all postings are read.

			int curGap = 0; //First gap is 0
			for(int i = 0; i < documentFrequency; i++){
				postings.read(buffer,0, buffer.length);
				int docID = ByteBuffer.wrap(buffer).getInt() + curGap;
				curGap = docID;
				docIds[i] = new termPostingList();
				docIds[i].setDocID(docID);

				// read the 4 bytes for the position frequency
				postings.read(buffer, 0, buffer.length);

				// use ByteBuffer to convert the 4 bytes to an int representing the position frequency
				int termFreq = ByteBuffer.wrap(buffer).getInt();
				docIds[i].positions = new int[termFreq];
				docIds[i].setTermFreq();

				// Loop through all the positions in document and decode
				int curPosGap = 0;
				for(int j = 0; j < termFreq; j++){
					postings.read(buffer, 0, buffer.length);
					int pos = ByteBuffer.wrap(buffer).getInt() + curPosGap;
					curPosGap = pos;
					docIds[i].positions[j] = pos;
				}
			}


			return docIds;
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	public void asserts(long binary, long bpetree){
		if(binary == bpetree){
			System.out.println("MATCHES!!!");
		} else {
			System.out.println("NOPE NOPE NOPE");
		}
	}

	public termPostingList[] GetPostingsPositions(String term){
		//long postingsPosition = BinarySearchVocabulary(term);
		long bin = BinarySearchVocabulary(term);
		long postingsPosition = bptree.search(term);
		asserts(bin, postingsPosition);
		if (postingsPosition >= 0) {
			return readPostingsPositionsFromFile(mPostings, postingsPosition);
		}
		return null;
	}


	private long BPlusTreeSearch(String term){
		return bptree.search(term);
	}

	private long BinarySearchVocabulary(String term) {
		// do a binary search over the vocabulary, using the vocabTable and the file vocabList.
		int i = 0, j = mVocabTable.length / 2 - 1;
		while (i <= j) {
			try {
				int m = (i + j) / 2;
				long vListPosition = mVocabTable[m * 2];
				System.out.println("vListPosition: " + vListPosition);
				int termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
				mVocabList.seek(vListPosition);

				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);
				String fileTerm = new String(buffer, "ASCII");

				int compareValue = term.compareTo(fileTerm);
				if (compareValue == 0) {
					// found it!
					System.out.println("pListPosition: " + mVocabTable[m * 2 + 1]);
					return mVocabTable[m * 2 + 1];
				}
				else if (compareValue < 0) {
					j = m - 1;
				}
				else {
					i = m + 1;
				}
			}
			catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return -1;
	}


	private static List<String> readFileNames(String indexName) {
		try {
			final List<String> names = new ArrayList<String>();
			final Path currentWorkingPath = Paths.get(indexName).toAbsolutePath();

			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
				int mDocumentID = 0;

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
						names.add(file.toFile().getName());
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file,
						IOException e) {

					return FileVisitResult.CONTINUE;
				}

			});
			return names;
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	private static long[] readVocabTable(String indexName) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(
					new File(indexName, "vocabTable.bin"),
					"r");

			byte[] byteBuffer = new byte[4];
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			int tableIndex = 0;
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;
			}
			tableFile.close();
			return vocabTable;
		}
		catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
		catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	public List<String> getFileNames() {
		return mFileNames;
	}

	public int getTermCount() {
		return mVocabTable.length / 2;
	}
}
