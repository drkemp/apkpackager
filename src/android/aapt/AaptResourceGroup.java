package org.chromium.aapt;

import java.io.File;
import java.util.HashMap;

/**
 * Contains all of the files for a given resource type, eg "drawable" or "xml".
 *
 * For all of the resources of this type, it maintains a mapping of the leaf
 * filename to a set of files, distinguished by their configuration variant.
 *
 * @author iclelland
 *
 */
public class AaptResourceGroup {
	
	HashMap<Config, Config> configs; // list of config variants used
	HashMap<String, HashMap<Config, File>> fileSets;
	
	public AaptResourceGroup() {
		this.configs = new HashMap<Config, Config>();
		this.fileSets = new HashMap<String, HashMap<Config, File>>();
	}
	
	public void addFileWithConfig(Config config, File file) {
		String fileName = file.getName();
		HashMap<Config, File> fileSet = fileSets.get(fileName);
		if (fileSet == null) {
			fileSet = new HashMap<Config, File>();
			fileSets.put(fileName, fileSet);
		}
		/* Normalize config */
		Config normalizedConfig = configs.get(config);
		if (normalizedConfig == null) {
			normalizedConfig = config;
			configs.put(config, config);
		}
		/* Add file */
		fileSet.put(normalizedConfig, file);
	}
}