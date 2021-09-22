package de.jlo.talendcomp.context;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class FileFilterConfig {

	private File dir = null;
	private String filter = null;
	private boolean ignoreMissing = false;
	
	public FileFilterConfig(String filePathFilter, boolean ignoreMissing) {
		if (filePathFilter == null || filePathFilter.trim().isEmpty()) {
			throw new IllegalArgumentException("filePathFilter cannot be null or empty");
		}
		File test = new File(filePathFilter);
		// check if the filter refers to a directory
		if (test.exists()) {
			if (test.isDirectory()) {
				filter = null;
				dir = test;
			} else {
				dir = test.getParentFile();
				filter = test.getName();
			}
		} else {
			if (filePathFilter.endsWith("/") || filePathFilter.endsWith("\\")) {
				filter = null;
				dir = test;
			} else {
				dir = test.getParentFile();
				filter = test.getName();
			}
		}
		this.ignoreMissing = ignoreMissing;
	}
	
	public static boolean isAbsolute(String path) {
		File f = new File(path);
		if (f.exists()) {
			return f.isAbsolute();
		} else {
			return path.endsWith("/") || path.endsWith("\\");
		}
	}
	
	public FileFilter getFileFilter() {
		if (filter != null) {
			return new WildcardFileFilter(filter, IOCase.INSENSITIVE);
		} else {
			return new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.isFile();
				}
			};
		}
	}
	
	public File getDir() {
		return dir;
	}
	
	public String getFilterOrName() {
		return filter;
	}
	
	public void setFilterOrName(String filter) {
		if (filter == null || filter.trim().isEmpty()) {
			throw new IllegalArgumentException("filter cannot be null or empty");
		}
		this.filter = filter;
	}
	
	public boolean isWildcardFilter() {
		return filter != null && (filter.contains("*") || filter.contains("?"));
	}
	
	public boolean isIgnoreMissing() {
		return ignoreMissing;
	}
	
	@Override
	public String toString() {
		return (dir.getAbsolutePath() + "/" + (filter != null ? filter : ""));
	}

	public void setDir(File dir) {
		this.dir = dir;
	}
	
}
