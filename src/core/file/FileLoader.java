package core.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileLoader {
	public static String readFile(String path) throws IOException {
		StringBuilder fileData = new StringBuilder();
		BufferedReader bf = new BufferedReader(new FileReader(path));
		String line;
		while((line=bf.readLine()) != null){
			fileData.append("\n"+line);
		}
		if(bf!=null){
			bf.close();
		}
		return fileData.toString();
	}
}
