package org.chromium.aapt;

//TODO: this class is imcomplete
public class AssetManager {
    public String RESOURCES_FILENAME;
    public String IDMAP_BIN;
    public String OVERLAY_DIR;
    public String TARGET_PACKAGE_NAME;
    public String TARGET_APK_PATH;
    public String IDMAP_DIR;

    static int gCount = 0;
    ResTable mResources;
    
    enum CacheMode {
        CACHE_UNKNOWN(0), CACHE_OFF(1), CACHE_DEFER(2);        // construct cache as pieces are needed
        private final int id;
        CacheMode(int id) { this.id = id; }
        public int getValue() { return id; }
    }
    
    CacheMode cacheMode = CacheMode.CACHE_OFF;
    
    public AssetManager( ) {
    	
    }

    static int getGlobalCount() {
        return gCount;
    }
    ResTable getResTable(boolean required) {
    	return mResources;
    }
    ResTable getResources(boolean required) {
    	return getResTable(required);
    }

}
