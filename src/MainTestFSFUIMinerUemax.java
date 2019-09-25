import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import skymine.*;
/**
 * Example of how to use the SFUPMinerUemax algorithm 
 * from the source code.
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger, 2016
 */
public class MainTestFSFUIMinerUemax {

	public static void main(String [] arg) throws IOException{
		
		String input;
//                input= fileToPath("contextHUIM.txt");
//                input= fileToPath("T10I4D100K_Utility.txt");
          //      input= fileToPath("retail_utility.txt");
                input= "mushroom_utility.txt"; //fileToPath("mushroom_utility.txt");
         //       input= fileToPath("foodmart_utility.txt");
          //      input= fileToPath("chess_utility.txt");
               String output; 
          //      output = ".//outputretailImprovedSFU.txt";
                 output = ".//outputmushroomImprovedSFU.txt";
         //       output = ".//outputfoodmartImprovedSFU.txt";
         //       output = ".//outputchessImprovedSFU.txt";
                
		AlgoFSFUIMinerUemax sfupMinerUemax1 = new AlgoFSFUIMinerUemax();
		sfupMinerUemax1.runAlgorithm(input, output);
		sfupMinerUemax1.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSFUPMinerUemax.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
