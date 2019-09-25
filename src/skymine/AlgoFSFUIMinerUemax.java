package skymine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** * * * This is an implementation of the skyline frequent-utility itemsets mining algorithm using uemax array and rtwu model
* 
* Copyright (c) 2018 Nguyen Manh Hung, Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author  Nguyen Manh Hung, Philippe Fournier-Viger
*/

public class AlgoFSFUIMinerUemax {
	
	double maxMemory = 0;     // the maximum memory usage
	long startTimestamp = 0;  // the time the algorithm started
	long endTimestamp = 0;   // the time the algorithm terminated
	int csfuiCount =0;  //the number of PSFUP
	int sfuiCount =0;  // the number of SFUP generated
	int searchCount =0;  //the number of search patterns
        int numberOfJoins=0;//the number of joining two utility lists 
	Map<Integer, Integer> mapItemToTWU;
        Map<Integer, Map<Integer, Long>> mapRTWU; 
	BufferedWriter writer = null;  // writer to write the output file
	
	// this class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
	}
	
	public AlgoFSFUIMinerUemax() {
	}
	public void runAlgorithm(String input, String output) throws IOException {
		// reset maximum
		maxMemory =0;		
		startTimestamp = System.currentTimeMillis();		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
                mapRTWU =  new HashMap<Integer, Map<Integer, Long>>();		
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);                                        
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)? 
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
				
		// CREATE A LIST TO STORE THE EXTENT UTILITY LIST OF ITEMS
		List<ExtentUtilityList> listOfUtilityLists = new ArrayList<ExtentUtilityList>();
		// CREATE A MAP TO STORE THE EXTENT UTILITY LIST FOR EACH ITEM.
		// Key : item    Value : Extent utility list associated to that item
		Map<Integer, ExtentUtilityList> mapItemToExtUtilityList = new HashMap<Integer, ExtentUtilityList>();
		
		// For each item
		for(Integer item: mapItemToTWU.keySet()){			
			// create an empty Extent Utility List that we will fill later.
			ExtentUtilityList uList = new ExtentUtilityList(item);
			mapItemToExtUtilityList.put(item, uList);
			// add the item to the list of high TWU items
			listOfUtilityLists.add(uList);
                }
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<ExtentUtilityList>(){
			public int compare(ExtentUtilityList o1, ExtentUtilityList o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS TO CONSTRUCT THE EXTENT UTILITY LISTS OF ALL 1-ITEMSETS 
		int tid =0;// A variable to count the number of transaction
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				
				// Copy the transaction into lists
				
				int remainingUtility =0;
				
				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);

					// add it to variale revisedTransaction
					revisedTransaction.add(pair);
					remainingUtility += pair.utility;
				
				}				
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});

								
				// for each item left in the transaction
				//for(Pair pair : revisedTransaction){
                                for(int i = 0; i< revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);    
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;					
					// get the utility list of this item
					ExtentUtilityList extUtilityListOfItem = mapItemToExtUtilityList.get(pair.item);
					
					// Add a new Element to the utility list of this item corresponding to this transaction
					ExtentElement element = new ExtentElement(tid, 0, pair.utility, remainingUtility);
					//Sua lai
					extUtilityListOfItem.addElementImproved(element);
                                        
                                        // BEGIN NEW rtwu model
					
                                        Map<Integer, Long> mapRtwuItem = mapRTWU.get(pair.item);
                                        
					if(mapRtwuItem == null) {
						
                                                mapRtwuItem = new HashMap<Integer, Long>();
						mapRTWU.put(pair.item, mapRtwuItem);
					}
                                        long UtilFromItem_i=remainingUtility+pair.utility;
					for(int j = i+1; j< revisedTransaction.size(); j++){
						Pair pairAfter = revisedTransaction.get(j);
                                                Long rtwuSum = mapRtwuItem.get(pairAfter.item);
						if(rtwuSum == null) {
							 
                                                        mapRtwuItem.put(pairAfter.item, UtilFromItem_i);
						}else {
							 
                                                        //twuSum + UtilFromItem_i
                                                        mapRtwuItem.put(pairAfter.item, rtwuSum + UtilFromItem_i);
						}
					}
					// END rtwu model
				}
				tid++; // increase tid number for next transaction
			}
			
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// check the memory usage
		checkMemory();

		// Mine the database recursively
		//This array is used to store the max utility value of each frequency,uEmax[0] is meaningless
		//uEmax[1] stored the max utiliey value of all the itemsets which have frequency equals to 1
		int uEmax[]=new int[tid+1];
		//The list is used to store the current candidate  skyline frequent-utility itemsets (CSFUIs)
		//csfuiList[1]store the psfup has frequent equals to 1
		SkylineList csfuiList[] = new SkylineList[tid+1];
		//The list is used to store the current skyline frequent-utility itemsets (SFUIs)
		List<Skyline> skylineList=new ArrayList<Skyline>();
		
		//test
		//This method is used to mine all the CSFUIs
		SFUIMiner(new int[0], null, listOfUtilityLists, csfuiList, skylineList, uEmax);
		//This method is used to mine all the SFUIs from CSFUIs
		judgeSkyline(skylineList,csfuiList,uEmax);
		//This method is used to write out all the CSFUIs
		writeOut(skylineList);
		csfuiCount=getcsfuiCount(csfuiList);
		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	
	private int compareItems(int item1, int item2) {
		int compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	/**
	 * This is the recursive method to find all candidate  skyline frequent-utility itemsets
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Extent Utility List of the prefix. Initially, it is empty.
	 * @param ULs The extent utility lists corresponding to each extension of the prefix.
	 * @param csfuiList Current candidate skyline frequent-utility itemsets.Initially, it is empty.
	 * @param skylineList Current skyline frequent-utility itemsets.Initially, it is empty.
	 * @param uEmax The array of max utility value of each frequency.Initially, it is zero.
	 * @throws IOException
	 */
	private void SFUIMiner(int [] prefix, ExtentUtilityList pUL, List<ExtentUtilityList> ULs, SkylineList csfuiList[], List<Skyline> skylineList, int [] uEmax)
			throws IOException {		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			ExtentUtilityList X = ULs.get(i);
			searchCount++;
			//temp store the frequency of X
			int temp=X.elements.size();				
			//judge whether whether X is a PSFUP
			//if the utility of X equals to the PSFUP which has same frequency with X, insert X to csfuiList
                        // Nếu có Util bằng đúng uEmax thì Them vào danh sách
			if(X.sumIutils+X.sumItemutils== uEmax[temp]&&uEmax[temp]!=0){
				Skyline tempPoint=new Skyline();
				tempPoint.itemSet=itemSetString(prefix, X.item);
				tempPoint.frequent=temp;
				tempPoint.utility=X.sumIutils+X.sumItemutils;
				csfuiList[temp].add(tempPoint);
			}
			//if the utility of X more than the PSFUP which has same frequency with X, update csfuiList
                        // Nếu có util lớn hơn thì reset danh sách
			if(X.sumIutils+X.sumItemutils>uEmax[temp]){
				uEmax[temp]=X.sumIutils+X.sumItemutils;
				//if csfuiList[temp] is null, insert X to csfuiList
				if(csfuiList[temp]==null){
					SkylineList tempList= new SkylineList();
					Skyline tempPoint=new Skyline();
					tempPoint.itemSet=itemSetString(prefix, X.item);
					tempPoint.frequent=temp;
					tempPoint.utility=X.sumIutils+X.sumItemutils;
					tempList.add(tempPoint);
					csfuiList[temp]=tempList;
				}
				//if csfuiList[temp] is not null, update csfuiList[temp]
				else{
					//This is the number of CSFUIs which has same frequency with X.
					int templength=csfuiList[temp].size();
					
					if(templength==1){
						csfuiList[temp].get(0).itemSet=itemSetString(prefix, X.item);
						csfuiList[temp].get(0).utility=X.sumIutils+X.sumItemutils;
					}
					else {
						for(int j=templength-1;j>0;j--){
							csfuiList[temp].remove(j);
						}
						csfuiList[temp].get(0).itemSet=itemSetString(prefix, X.item);
						csfuiList[temp].get(0).utility=X.sumIutils+X.sumItemutils;
					}
				}
			}

			// If the sum of the remaining utilities for pX
			// is higher than uEmax[j], we explore extensions of pX.
			// (this is the pruning condition)
                        // Nếu cùng tần suất mà có thể cho util lơn hơn thì tiếp tục xây dựng Ulist
			if(X.sumIutils + +X.sumItemutils+ X.sumRutils >= uEmax[temp] && uEmax[temp]!=0){	
				// This list will contain the utility lists of pX extensions.
				List<ExtentUtilityList> exULs = new ArrayList<ExtentUtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					ExtentUtilityList Y = ULs.get(j);
                                        int tmp1=Y.elements.size();
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
                                        // Bo sung kiem tra
                                        // =========================== END OF NEW OPTIMIZATION
					// NEW OPTIMIZATION USED IN MFHM
                                        // (min (X.sumIutils,Y.sumIutils)+RTWU(X<Y)
                                        // Chú ý phải sửa lại trong UList
                                        Map<Integer, Long> mapRTWUF = mapRTWU.get(X.item);
                                        Long rtwuF;
					if(mapRTWUF != null) {
						rtwuF = mapRTWUF.get(Y.item);// Lay util cua cap   
                                                long test;                                           
                                                if (X.sumIutils<Y.sumIutils )
                                                    test=X.sumIutils;
                                                else
                                                    test=Y.sumIutils;                                           
                                                if(rtwuF != null)
                                                     test+=rtwuF;                                                    
						if(rtwuF == null || test < uEmax[temp] || test<uEmax[tmp1]) {// Bo qua Y theo dieu kien EUCS
							continue;
						}
					}
                                        //
                                        
					exULs.add(construct(pUL, X, Y));
                                        numberOfJoins++;
				}
				// We create new prefix pX
				int [] newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = X.item;				
				// We make a recursive call to discover all itemsets with the prefix pXY
				SFUIMiner(newPrefix, X, exULs, csfuiList, skylineList, uEmax); 
			}
		}
	}	
	/**
	 * This method constructs the extent utility list of pXY
	 * @param P :  the extent utility list of prefix P.
	 * @param px : the extent utility list of pX
	 * @param py : the extent utility list of pY
	 * @return the extent utility list of pXY
	 */
	private ExtentUtilityList construct(ExtentUtilityList P, ExtentUtilityList px, ExtentUtilityList py) {
		// create an empy utility list for pXY
		ExtentUtilityList pxyUL = new ExtentUtilityList(py.item);
		// for each element in the utility list of pX
		for(ExtentElement ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			ExtentElement ey = findElementWithTID(py, ex.tid);
			if(ey == null){                        
				continue;
			}
                        
                        ExtentElement eXY1 = new ExtentElement(ex.tid, ex.itemSetutils +ex.itemUtils,  ey.itemUtils, ey.rutils);
				// add the new element to the utility list of pXY
                        pxyUL.addElementImproved(eXY1);                                
				
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private ExtentElement findElementWithTID(ExtentUtilityList ulist, int tid){
		List<ExtentElement> list = ulist.elements;
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
       
        // the binary search
        while( first <= last )  {
            int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}
	/**
	 * Method to write out itemset name
	 * @param prefix This is the current prefix
	 * @param item This is the new item added after the prefix
	 * @return  the itemset name
	 */
	private String itemSetString(int[] prefix, int item) throws IOException {
	
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);

		return buffer.toString();

	}

	/**
	 * Method to write skyline frequent-utility itemset to the output file.
	 * @param skylineList The list of skyline frequent-utility itemsets 
	 */
	private void writeOut(List<Skyline> skylineList) throws IOException {
		sfuiCount=skylineList.size();
	//Create a string buffer
		StringBuilder buffer = new StringBuilder();
//		buffer.append("Total skyline frequent-utility itemset: ");
//		buffer.append(sfuiCount);
//		buffer.append(System.lineSeparator());
		
		for(int i=0;i<sfuiCount;i++){
			buffer.append(skylineList.get(i).itemSet);
			buffer.append(" #SUP:");
			buffer.append(skylineList.get(i).frequent);
			buffer.append(" #UTILITY:");
			buffer.append(skylineList.get(i).utility);
			buffer.append(System.lineSeparator());
			// write to file
		}
		writer.write(buffer.toString());
	}
	
	/**
	 * Method to judge whether the PSFUP is a SFUP
	 * @param skylineList The skyline frequent-utility itemset list
	 * @param csfuiList The candidate  skyline frequent-utility itemset list
	 * @param uEmax The max utility value of each frequency
	 */
	private void judgeSkyline(List<Skyline> skylineList, SkylineList csfuiList[], int uEmax[]) {
		for(int i=1;i<csfuiList.length;i++){
			//if temp equals to 0, the value of csfuiList[i] is higher than all the value of csfuiList[j](j>i)
			int temp=0;
			//compare csfuiList[i] with csfuiList[j],(j>i)
			if(csfuiList[i]!=null){
				int j=i+1;
				while(j<csfuiList.length){
					if(csfuiList[j]==null){
						j++;
					}
					else{
						if(csfuiList[i].get(0).utility <=csfuiList[j].get(0).utility){
							temp=1;
							break;
						}
						else{
							j++;
						}
					}
				}
				//it temp equals to 0, this PSFUP is a SFUP
				if(temp==0){
					for(int k=0;k<csfuiList[i].size();k++)
						skylineList.add(csfuiList[i].get(k));
				}
			}				
		}		
	}
	
	/**
	 * Method to get the count of PSFUP.
	 * @param csfuiList the candidate  skyline frequent-utility itemset list
	 * @return  the count of CSFUIs
	 */
	private int getcsfuiCount(SkylineList csfuiList[]) {
		for(int i=1;i<csfuiList.length;i++){
			if(csfuiList[i]!=null){
				csfuiCount=csfuiCount+csfuiList[i].size();
			}				
		}
		return csfuiCount;
	}
	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())
				/ 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  uEmax and rtwu skyline ALGORITHM v 2.12 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + maxMemory+ " MB");
		System.out.println(" Skyline itemsets count : " + sfuiCount);
		System.out.println(" The numer of extent utility list: " + searchCount);
		System.out.println(" The number of join operations two extent utility lists: " + numberOfJoins);
                System.out.println(" The number of potential SFUIs : " + csfuiCount);
		System.out.println("===================================================");
	}
}