import java.io.*;
import java.util.*;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Scanner;
/**
 * @author edited by Gary Ramli
 * @version 04/28/17
 */
public class NaiveBayes {
	
	//	This function reads in a file and returns a 
	//	set of all the tokens. It ignores the subject line
	//
	//	If the email had the following content:
	//
	//	Subject: Get rid of your student loans
	//	Hi there ,
	//	If you work for us, we will give you money
	//	to repay your student loans . You will be 
	//	debt free !
	//	FakePerson_22393
	//
	//	This function would return to you
	//	[hi, be, student, for, your, rid, we, get, of, free, if, you, us, give, !, repay, will, loans, work, fakeperson_22393, ,, ., money, there, to, debt]
	public static TreeSet<String> tokenSet(File filename) throws IOException {
		TreeSet<String> tokens = new TreeSet<String>();
		Scanner filescan = new Scanner(filename);
		filescan.next(); //Ignoring "Subject"
		while(filescan.hasNextLine() && filescan.hasNext()) {
			tokens.add(filescan.next());
		}
		filescan.close();
		return tokens;
	}
	
	public static void main(String[] args) throws IOException {
		// 1: Iterate over labeled spam emails and compute P(w|S)
		// Variables
		File sDir = new File("data/train/spam");						// spam file directory
		File[] sList = sDir.listFiles();								// spam file list
		Iterator<String> sIter;											// Iterator
		Map<String, Double> sMap = new TreeMap<String, Double>();  	 	// Map of spam words
		TreeSet<String> sSet;											// Set of spam words
		Double sDouble;													// size
		String sNext;													// next word
		// Iterate through list of files
		for(File i : sList){
			// Update list of words
			sSet = tokenSet(i);
			sIter = sSet.iterator();
			// Iterates though file contents
			while(sIter.hasNext()){
				sDouble = 1 / (sList.length + 2.0);
				sNext = sIter.next();
				if (sMap.containsKey(sNext)){
					// word not seen
					sMap.put(sNext, sDouble + sMap.get(sNext));
				} else {
					// word seen - increment counter
					sMap.put(sNext, 2 * sDouble);
				}
			}
		}
		
		// 2: Iterate over labeled ham emails and compute P(w|H)
		// Variables
		File hDir = new File("data/train/ham");						// ham file directory
		File[] hList = hDir.listFiles();							// ham file list
		Iterator<String> hIter;										// Iterator
		Map<String, Double> hMap = new TreeMap<String, Double>();   	// Map of ham words
		TreeSet<String> hSet;											// Set of ham words
		Double hDouble;													// size
		String hNext;													// next word
		// Iterate through list of files
		for(File i : hList){
			// Update list of words
			hSet = tokenSet(i);
			hIter = hSet.iterator();
			// Iterates though file contents
			while(hIter.hasNext()){
				hDouble = 1 / (hList.length + 2.0);
				hNext = hIter.next();
				if (hMap.containsKey(hNext)){
					// word not seen
					hMap.put(hNext, hDouble + hMap.get(hNext));
				} else {
					// word seen - increment counter
					hMap.put(hNext, 2 * hDouble);
				}
			}
		}
		
		// 3 AND 4: Compute P(S) and P(H)
		double slen = (double) (sList.length);
		double hlen = (double) (hList.length);
		double PS = slen / slen + hlen;
		double PH = hlen / slen + hlen;

		// 5: Iterate over given set of emails
		File tDir = new File("data/test");		// test file directory
		File[] tList = tDir.listFiles();		// test file list
		Iterator<String> tIter;					// Iterator
		TreeSet<String> tSet;					// Set of test words
		Double tDouble;							// size
		String tNext;							// next word
		double s;								// spam threshold
		double h;								// ham threshold
		
		// Overrides comparator on tList and sorts list
		Arrays.sort(tList, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Integer.compare(Integer.parseInt(f1.toString().substring(10, f1.toString().length() - 4)), 
                					   Integer.parseInt(f2.toString().substring(10, f2.toString().length() - 4)));
            }
        });
		
		// Iterate through list of files
		for(File i : tList){
			// Update variables
			tSet = tokenSet(i);
			tIter = tSet.iterator();
			s = Math.log(PS);
			h = Math.log(PH);
			// Iterates though file contents
			while(tIter.hasNext()){
				// Update list of words
				tDouble = sList.length + 2.0;
				tNext = tIter.next();
				// Checks for word occurrences in both maps, updates thresholds accordingly
				if(sMap.containsKey(tNext)){
					if(hMap.containsKey(tNext)){
						// word found in both
						s += Math.log(sMap.get(tNext));
						h += Math.log(hMap.get(tNext));
					}else{
						// word only found in spam
						s += Math.log(sMap.get(tNext));
						h += Math.log(1.0 / tDouble);
					}
				}else{
					if(hMap.containsKey(tNext)){
						// word found only in ham
						s += Math.log(1.0 / tDouble);
						h += Math.log(hMap.get(tNext));
					}else{
						// word not found; do nothing
						continue;
					}
				}
			}
			// s > h print spam, otherwise print ham
			if(s > h){
				System.out.println(i.getName() + " spam");
			}else{
				System.out.println(i.getName() + " ham");
			}
		}
	}
}
