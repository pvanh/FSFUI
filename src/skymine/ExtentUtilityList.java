package skymine;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a ExtentUtilityList as used by the HUI-Miner algorithm.
 *
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 */

class ExtentUtilityList {
	int item;  // the item
	int sumIutils = 0;  // the sum of item utilities
        int sumItemutils=0;// Bosung
	int sumRutils = 0;  // the sum of remaining utilities
	List<ExtentElement> elements = new ArrayList<ExtentElement>();  // the elements
	
	/**
	 * Constructor.
	 * @param item the item that is used for this utility list
	 */
	public ExtentUtilityList(int item){
		this.item = item;
	}
	
	/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(ExtentElement element){
		sumIutils += element.itemSetutils;                
		sumRutils += element.rutils;
		elements.add(element);
	}
        // Bosung
        public void addElementImproved(ExtentElement element){
		sumIutils += element.itemSetutils;
                sumItemutils += element.itemUtils;
		sumRutils += element.rutils;
		elements.add(element);
	}
}
