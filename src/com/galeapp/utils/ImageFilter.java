package com.galeapp.utils;

import java.io.File;
import java.io.FilenameFilter;

public class ImageFilter implements FilenameFilter {
	public boolean isJpg(String file) {
		if (file.toLowerCase().endsWith(".jpg")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isJpeg(String file) {
		if (file.toLowerCase().endsWith(".jpeg")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isPng(String file) {
		if (file.toLowerCase().endsWith(".png")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean accept(File dir, String fname) {
		return (isJpg(fname) || isJpeg(fname) || isPng(fname));
	}

}
