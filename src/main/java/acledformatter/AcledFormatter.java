package acledformatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AcledFormatter {

	private String DATA_DIR = "./data/";
	
	public List<String> getLines(String fileName) {
		
		String filePath = DATA_DIR + fileName;
		List<String> lines = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       lines.add(line);
		    }
		    br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return lines;
	}
	
	public List<String> getColumns(int maxLines, List<Integer> cols,
			List<String> lines) {
		
		List<String> results = new ArrayList<String>();
		
		for (String line : lines) {
			String outline = new String();
			String[] arr = line.split("\\t");
			for (int i = 0; i < cols.size(); ++i) {
				String token = arr[cols.get(i)];
				outline += token;
				if (i < cols.size() - 1)
					outline += ",";
			}
			results.add(outline);
			if(results.size() > maxLines)
				break;
		}
		return results;
	}
	
	public void writeFile(String fileName, List<String> lines) 
			throws IOException {
		
		String filePath = DATA_DIR + fileName;
		FileWriter writer = new FileWriter(filePath); 
		for(String str: lines) {
		  writer.write(str + "\n");
		}
		writer.close();

	}
	
	
	public static void main(String[] args) {
		
		//String acledFile = "ACLED-Africa_2008-2018_upd-May19.txt";
		String acledFile = "acled2018.txt";
		
		//String outFile = "ACLED-Africa_2008-2018_upd-May19-OUT.txt";
		String outFile = "acled2018.out.txt";
		
		
		AcledFormatter af = new AcledFormatter();
		List<String> lines = af.getLines(acledFile);
		
		List<Integer> cols = new ArrayList<Integer>();
		cols.add(AcledConstants.LONGITUDE);
		cols.add(AcledConstants.LATITUDE);
		lines = af.getColumns(lines.size(), cols, lines);
					
		try {
			af.writeFile(outFile, lines);
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		
		System.out.println("Done.");
	}
	
	

}
