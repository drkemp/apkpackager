package org.chromium.aapt;
//TODO: this class is imcomplete

public class AaptAssets {

    private String mPackage;
    private AssetManager mIncludedAssets;
    
	public String getPackage() {
        return mPackage;
	}
	public ResTable getIncludedResources() {
        return mIncludedAssets.getResources(false);
	}
}
