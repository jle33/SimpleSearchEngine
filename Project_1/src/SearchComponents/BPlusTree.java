/**
 * Just a wrapper to easily interface with the B+ Tree library.
 */

package SearchComponents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import NET.sourceforge.BplusJ.BplusJ.BplusTreeLong;

public class BPlusTree {
	private int keylength = 24; //16, 32, 64 bytes 
	private int nodesize = 6;
	private RandomAccessFile mstream;
	private BplusTreeLong bpt;


	public void initialize(String folder){
		try {
			File f = new File(folder, "bptree.bin");
			if(f.exists()){
				f.delete();
			}
			mstream =  new RandomAccessFile(f.toString(), "rw");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
		try {
			bpt = BplusTreeLong.InitializeInStream(mstream, keylength, nodesize);
			bpt.Commit();
			//Restart to create new bptree
			bpt = BplusTreeLong.SetupFromExistingStream(bpt.fromfile, bpt.seekStart);;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open(String path){
		try {
			RandomAccessFile fromfile = new RandomAccessFile(new File(path, "bptree.bin"), "r");
			bpt = BplusTreeLong.SetupFromExistingStream(fromfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void store(String vocab, long vocabPos) throws Exception{
		bpt.set(vocab, vocabPos);
	}

	public long search(String term){
		try {
			if(bpt.ContainsKey(term)){
				System.out.println("Value Found for: " + term + " Value: " + bpt.LastValueFound);
				return bpt.LastValueFound;
			} else {
				System.out.println("Value Not Found for: " + term);
				return -1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void close() throws Exception{
		bpt.Commit();
		mstream.close();
		mstream = null;
	}

}
