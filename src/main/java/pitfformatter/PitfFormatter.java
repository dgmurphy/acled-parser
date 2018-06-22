package pitfformatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PitfFormatter {
	
	private String outputDir = new String();
	
	public  PitfFormatter(String outputDir) {
		
		Path path = Paths.get(outputDir);
		path.toFile().mkdir();
		
		this.outputDir = outputDir;
	}


	public void process(Path path) {
		
		List<String> outlines = new ArrayList<String>();
		outlines.add("lng,lat");
		
		List<String> inlines = getLines(path.toFile());	
		
		for (String line : inlines) {
			line = line.trim();
			if (line.length() < 1)
				continue;
			
			String[] latDMS = new String[3];
			String[] lngDMS = new String[3];
			String[] arr = line.split("\\t");
			for (int i = 0; i < arr.length; i++) {
				String val = arr[i].trim();
				if(val.length() > 0) {
					if (i < 3) {
						latDMS[i] = val;
					} else {
						lngDMS[i - 3] = val;
					}
				}	
			}
			try {
				outlines.add(DMStoDegrees(lngDMS) + ", " + DMStoDegrees(latDMS));	
			} catch (Exception e) {
				//System.out.println("Error processing file: " + path.toString());
				//e.printStackTrace();
				System.out.println("Bad number format in: " + path.toString());
			}
		}
		
		String outFileName = path.getFileName().toString();
		outFileName = outFileName.substring(0, outFileName.lastIndexOf(".")) + "-out.txt";
		String outFilePath = outputDir + "/" + outFileName;
		try {
			writeFile(outFilePath, outlines);
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
	

	
	public void writeFile(String fileName, List<String> lines) 
			throws IOException {
		
		String filePath = fileName;
		FileWriter writer = new FileWriter(filePath); 
		for(String str: lines) {
		  writer.write(str + "\n");
		}
		writer.close();

	}
}
