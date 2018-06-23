package pitfformatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PitfFormatter {
	
	private String outputDir = new String();
	private Map<String, List<String>> dateMap = new HashMap<String, List<String>>();
	
	public  PitfFormatter(String outputDir) {
		
		Path path = Paths.get(outputDir);
		path.toFile().mkdir();
		
		this.outputDir = outputDir;
	}


	private boolean isHeader(String line) {
		
		List<String> headers = new ArrayList<String>();
		headers.add("Disclaimer: This research was conducted for the Political Instability Task Force (PITF).");
		headers.add("EVENT TYPE AND REPORTING");
		headers.add("Event Type");
		
		if (line.length() < 1)
			return true;
		
		for(String token : headers) {
			if(line.contains(token))
				return true;
		}
		
		return false;
	}
	
	public void process(Path path) {
		
		int lineNumber = 0;
		String[] latDMS = new String[3];
		String[] lngDMS = new String[3];
		
		List<String> outlines = new ArrayList<String>();
		outlines.add("lng,lat");
		
		List<String> inlines = getLines(path.toFile());	
		
		String year = new String();
		
		for (String line : inlines) {
			++lineNumber;
			line = line.trim();
			if (isHeader(line))
				continue;
			
			// parse input line
			try {
				String[] arr = line.split("\\t");
				if (arr.length > 19) {
					year = arr[5];
					latDMS[0] = arr[13].trim();
					latDMS[1] = arr[14].trim();
					latDMS[2] = arr[15].trim();
					lngDMS[0] = arr[17].trim();
					lngDMS[1] = arr[18].trim();
					lngDMS[2] = arr[19].trim();
				}
				
			} catch (Exception e) {
				System.out.println("Error parsing line: " + lineNumber + " in " +
						path.toString());
				e.printStackTrace();
				System.exit(1);
			}
			
			// Build output line
			try {
				boolean goodLocation = true;
				for (int i = 0; i < 3; ++i) {
					if ((latDMS[i].length() < 1) || (lngDMS[i].length() < 1))
						goodLocation = false;
				}
				if (goodLocation) {
					String goodLine = DMStoDegrees(lngDMS) + ", " + DMStoDegrees(latDMS);
					if (dateMap.containsKey(year)) {
						dateMap.get(year).add(goodLine);
					} else {
						List<String> lineList = new ArrayList<String>();
						lineList.add(goodLine);
						dateMap.put(year, lineList);
					}
				}
					
				else {
					System.out.println("Bad location data line: " + lineNumber + 
							" in " + path.toString());
				}
			} catch (Exception e) {
				
				System.out.println("Bad number format in: " + path.toString());
			}
		}
		
		try {
			writeFiles(path, dateMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	
	private String DMStoDegrees(String[] dms) {

		
		Integer deg = Integer.parseInt(dms[0]);
		Integer min = Integer.parseInt(dms[1]);
		Integer sec = Integer.parseInt(dms[2]);

		Double mind = (Double)(min + sec/60.0);
		Double degd = (Double)(deg + mind/60.0);

		return Double.toString(degd);
		
	}
	
	
	private List<String> getLines(File file) {
		
		List<String> lines = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
	

	
	public void writeFiles(Path inPath, Map<String, List<String>> dateMap) 
			throws IOException {
		
		for(String year : dateMap.keySet()) {
			String outFile = inPath.getFileName().toString();
			outFile = outFile.substring(0, outFile.lastIndexOf("."));
			outFile += "-" + year + "-out.txt";
			outFile = outputDir + outFile;
			FileWriter writer = new FileWriter(outFile); 
			writer.write("lng,lat" + "\n");
			List<String> lines = dateMap.get(year);
			for(String str: lines) {
				  writer.write(str + "\n");
			}
			System.out.println("Wrote: " + outFile);
			writer.close();
		}

	}
}
