package handler.file;

import java.io.File;

import java.io.FileFilter;

public class FiltroJava implements FileFilter {
	String extension;

	public FiltroJava(String ext) {
		this.extension = ext;
	}

	@Override
	public boolean accept(File f) {
		if (!f.isDirectory() && f.getName().split("\\.")[1].equals(this.extension))
			return true; // nos interesan carpetas y .java, aunque creo que
							// buscamos carpetas en el chooser.
		return false;
	}

}
