package org.chromium.aapt;
//TODO: this class is imcomplete
public class AaptFile {
	String mPath;
	AaptGroupEntry mGroupEntry;
	
    String getPath()  { return mPath; }
    AaptGroupEntry getGroupEntry() { return mGroupEntry; }

    AaptFile(String sourceFile, AaptGroupEntry groupEntry, String resType) {
    	
    }
}
