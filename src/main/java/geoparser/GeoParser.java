package geoparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import acledformatter.AcledConstants;
import acledformatter.AcledFormatter;
import pitfformatter.PitfFormatter;

public class GeoParser {
	
	
	private void processPitf(String outputDir, List<Path> inputPaths) {
		
		PitfFormatter pf = new PitfFormatter(outputDir);
		for(Path path : inputPaths) {
			try {
				pf.process(path);
			} catch (Exception e) {
				System.out.println("Error processing " + path.toString());
				e.printStackTrace();
			}
		}
		
	}
	
	private void processAcled() {
		
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
		
	}

	public static void main(String[] args) {
		
		String dataType = "pitf";   // or acled
		String inputDir = "./input"; 
		String outputDir = "./output/";  
		
		GeoParser gp = new GeoParser();
		
		List<Path> paths = new ArrayList<Path>();
		try {
			paths = Files.walk(Paths.get(inputDir))
				     .filter(Files::isRegularFile)
				     .collect(Collectors.toList());
			
			System.out.println("Found " + paths.size() + " input files.\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(dataType) {
		
		case "pitf":
			gp.processPitf(outputDir, paths);
			break;
			
		case "acled":
			gp.processAcled();
			break;
			
		default:
			break;
			
		}
		
		
		System.out.println("\nDone.");
	}

}


