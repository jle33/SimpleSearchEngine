package testing;

import java.util.*;
import NET.sourceforge.BplusJ.BplusJ.*;

public class encodingTest 
{

public static void main(String argv[]) 
			throws Exception
{
	hBplusTreeBytes HT = (hBplusTreeBytes) 
		hBplusTreeBytes.Initialize("/tmp/junk.bin", "/tmp/junk2.bin", 6);
	String stuff = "cæser";
	String test = HT.PrefixForByteCount(stuff, 5);
	System.out.println("test="+test);
	//HT[stuff] = "goober";
	byte[] bytes = new byte[0];
	HT.set(stuff, bytes);
}

}