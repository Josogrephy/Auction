package auction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AuctionLogic {

	public static void main(String[] args) throws FileNotFoundException {
		// Main function to read and write the output file
		// Declare variables
		ArrayList<ArrayList<String>> datalist = new ArrayList<>();
		int i = 0;
		int finalTimeStamp = 0;
		ArrayList<String> completedBid = null;
		ArrayList<String> completedBidList= new ArrayList<String>();
		// Read file
		File myObj = new File("input.txt");
		Scanner myReader = new Scanner(myObj);
		while (myReader.hasNextLine()) {
			// Read each line
			String data = myReader.nextLine();
			// If it is a heart beat message
			if(convertData(data).size() == 1) {
				// process expired bid
				completedBid = checkExpire(Integer.parseInt(data), datalist);
				if (completedBid != null) {
					// add to the final print list
					completedBidList.addAll(completedBid);
				}
				// remove processed bids from the list
				for (int counter = 0; counter < datalist.size() && completedBid.size() > 0; counter++) { 	
					for (int sCounter = 0; sCounter < completedBid.size(); sCounter++) {
						if(datalist.get(counter).get(3).contains(convertData(completedBid.get(sCounter)).get(1))) {
							datalist.remove(datalist.get(counter));
							i = i-1;
						}
					}
				}   
			} else {
				// convert data and add it into datalist for processing at the next heartbeat 
				datalist.add(new ArrayList<String>());
				datalist.get(i).addAll(convertData(data));
				finalTimeStamp = Integer.parseInt(datalist.get(i).get(0));
				i++;
			}
		}
		// run a final process on the last line
		completedBid = checkExpire(finalTimeStamp + 1, datalist);
		completedBidList.addAll(completedBid);
		// write into output file
		try {
			write(completedBidList);
		} catch (IOException e) {
			System.out.println(e);
		}
		myReader.close();
	}

	// method to change piped string to array
	public static ArrayList<String> convertData(String data) {
		String[] data_split = data.split("\\|");
		ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(data_split));
		return arrayList;
	}

	// method to check the winner and build the string
	public static String checkWinner(String expiredItem, ArrayList<ArrayList<String>> datalist, float rPrice, int timeStamp) {
		float highestPrice = 0;
		float secondPrice = 0;
		float lowestPrice = 0;
		String outcome ="";
		String bidFinal ="";
		int count = 0;
		String userid = "";
		
		// run through the datalist
		for(int i = 0; i < datalist.size(); i++){
			// process bids of the expired auction items
			if (datalist.get(i).get(2).equals("BID") && datalist.get(i).get(3).equals(expiredItem)) {
				count++;
				// build the lowest price
				if (lowestPrice == 0) {
					lowestPrice = Float.parseFloat(datalist.get(i).get(4));
				} else if (Float.parseFloat(datalist.get(i).get(4)) <= lowestPrice) {
					lowestPrice = Float.parseFloat(datalist.get(i).get(4));
				}
				// build reserve price and highest price
				if(Float.parseFloat(datalist.get(i).get(4)) >= rPrice) {	
					// Users with the same bid price, will only record the earliest record
					if (Float.parseFloat(datalist.get(i).get(4)) > highestPrice) {
						secondPrice = highestPrice;
						highestPrice = Float.parseFloat(datalist.get(i).get(4));
						userid = datalist.get(i).get(1);
						if (secondPrice == 0) {
							secondPrice = rPrice;
						}
						outcome = "SOLD";
						bidFinal = Integer.toString(timeStamp) + "|" + expiredItem + "|" + userid + "|" + outcome + "|" + String.format("%.2f", secondPrice) + "|" + count + "|" + String.format("%.2f", highestPrice) + "|" + String.format("%.2f", lowestPrice);
					}
					// if auction item is unsold
				} else {
					outcome = "UNSOLD";
					if (Float.parseFloat(datalist.get(i).get(4)) >= highestPrice) {
						highestPrice = Float.parseFloat(datalist.get(i).get(4));
						bidFinal = Integer.toString(timeStamp) + "|" + expiredItem + "|" + outcome + "|" + String.format("%.2f", secondPrice) + "|" + count + "|" + String.format("%.2f", highestPrice) + "|" + String.format("%.2f", lowestPrice);
					}

				}
			}


		}

		return bidFinal;
	}

	// Method to check the expiry on heart beat message
	public static ArrayList<String> checkExpire(int timeStamp, ArrayList<ArrayList<String>> datalist) {
		ArrayList<String> finalOutput = new ArrayList<>();
		for(int i = 0; i < datalist.size(); i++){
			if (datalist.get(i).get(2).equals("SELL")) {
				if (Integer.parseInt(datalist.get(i).get(5)) <= timeStamp) {
					String output = checkWinner(datalist.get(i).get(3), datalist, Float.parseFloat(datalist.get(i).get(4)), timeStamp);
					finalOutput.add(output);	
				}
			}	
		}

		return finalOutput;

	}

	// Method to write the output file
	public static void write (ArrayList<String> finalOutput) throws IOException{
		FileWriter fw = new FileWriter("output.txt");
		for (int i = 0; i < finalOutput.size(); i++) {
			fw.write(finalOutput.get(i) + "\n");
		}
		fw.close();
	} 
}







