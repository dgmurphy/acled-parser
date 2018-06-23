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
	private Map<String, List<String>> dateMap; 
	
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
		
		System.out.println("Processing: " + path.toString());
		
		int lineNumber = 0;
		dateMap = new HashMap<String, List<String>>();
		
		List<Integer> badLineList = new ArrayList<Integer>();
		String[] latDMS = new String[4];  // deg min sec direction
		String[] lngDMS = new String[4];
		
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
				if (arr.length > 20) {
					year = arr[5];
					latDMS[0] = arr[13].trim();
					latDMS[1] = arr[14].trim();
					latDMS[2] = arr[15].trim();
					latDMS[3] = arr[16].trim();
					lngDMS[0] = arr[17].trim();
					lngDMS[1] = arr[18].trim();
					lngDMS[2] = arr[19].trim();
					lngDMS[3] = arr[20].trim();
				}
				
			} catch (Exception e) {
				System.out.println("Error parsing line: " + lineNumber + " in " + path.toString());
				e.printStackTrace();
				System.exit(1);
			}
			
			// Build output line
			if (!buildOutputLine(latDMS, lngDMS, year, path.toString(), lineNumber))
				badLineList.add(lineNumber);
				
		}
		
		try {
			writeFiles(path, dateMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (!badLineList.isEmpty()) {
			System.out.println("Bad Line List for " + path.toString()+ ": ");
			for (int lineNum : badLineList) {
				System.out.println("Line: " + lineNum);
			}
		} else {
			System.out.println("No bad lines found in: " + path.toString());
		}
		
		System.out.println("\n\n");
	}
	
	private boolean buildOutputLine(String[] latDMS, String[] lngDMS, 
			String year, String fileName, int lineNumber) {


		// Normalize directions
		if(latDMS[3].equalsIgnoreCase("North"))
			latDMS[3] = "N";
		if(latDMS[3].equalsIgnoreCase("South"))
			latDMS[3] = "S";
		if(lngDMS[3].equalsIgnoreCase("East"))
			lngDMS[3] = "E";
		if(lngDMS[3].equalsIgnoreCase("West"))
			lngDMS[3] = "W";
		
		for (int i = 0; i < 3; ++i) {
			if ((latDMS[i].length() < 1) || (lngDMS[i].length() < 1)) {
				System.out.println("Missing lat/lng on line: " + lineNumber + " in " + fileName);
				return false;
			}
		}
		if (!latDMS[3].equalsIgnoreCase("N") && !latDMS[3].equalsIgnoreCase("S")) {
			System.out.println("Bad N/S code on line: " + lineNumber + " in " + fileName);
			return false;
		}
		if (!lngDMS[3].equalsIgnoreCase("E") && !lngDMS[3].equalsIgnoreCase("W")) {
			System.out.println("Bad E/W code on line: " + lineNumber + " in " + fileName);
			return false;
		}

		String goodLine = DMSdirToDegrees(lngDMS) + ", " + DMSdirToDegrees(latDMS);
		if(goodLine.contains("error")) {
			System.out.println("Bad location conversion on line: " + lineNumber + " in " + fileName);
			return false;
		}
		
		if (dateMap.containsKey(year)) {
			dateMap.get(year).add(goodLine);
		} else {
			List<String> lineList = new ArrayList<String>();
			lineList.add(goodLine);
			dateMap.put(year, lineList);
		}

		return true;

	}
	
	
	private String DMSdirToDegrees(String[] dms) {

		try {
		Integer deg = Integer.parseInt(dms[0]);
		Integer min = Integer.parseInt(dms[1]);
		Integer sec = Integer.parseInt(dms[2]);
		String dir = dms[3];

		Double mind = (Double)(min + sec/60.0);
		Double degd = (Double)(deg + mind/60.0);
		
		if (dir.equalsIgnoreCase("S") || dir.equalsIgnoreCase("W"))
			degd = -degd;

		return Double.toString(degd);
		
		} catch (Exception e) {
			return ("error");
		}
		
		
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
			System.out.println("Wrote " + lines.size() + " lines to " + outFile);
			writer.close();
		}

	}
}
