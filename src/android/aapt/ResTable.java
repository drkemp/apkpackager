package org.chromium.aapt;

//TODO: this class is imcomplete

public class ResTable {

    Errors mError = Errors.NO_INIT;
	public ResTable() {
    	
	}
    public ResTable(Object data, int size, int cookie, boolean copyData) {
    	
    }
    
    class resource_name
    {
 	   String packagename; //was package
 	   int packageLen;
 	   String type;
 	   String type8;
 	   int typeLen;
 	   String name;
 	   String name8;
 	   int nameLen;
    }
 /*   
    Errors add(Asset asset, int cookie, boolean copyData, Object idmap ) {
    	
    }
    Errors add(Object data, int size) {
    	
    }
    Errors add(ResTable src) {

    }
*/
    Errors getError() {
    	return mError;
    }

    void uninit() {
    	
    }
    public class ResTable_map {
    	//TODO: this class is imcomplete
    	static public final int TYPE_ANY = 0x0000FFFF;
    }

    public class ResourceValue {
    	//TODO: this class is imcomplete
        public boolean ok;
        public Value outValue;
        public String outString;
        public ResourceValue(Value outval, String outstring, boolean success) {
        	outValue=outval;
        	outString=outstring;
        	ok=success;
        }
    }
    public class Accessor {
    	//TODO: this class is imcomplete
    }
    
    ResourceValue stringToValue( String s, boolean preserveSpaces, boolean coerceType, int attrID ,
            String defType,  String defPackage,
            Accessor accessor,
            Object accessorCookie,
            int attrType,
            boolean enforcePrivate) {
    	return new ResourceValue(null,null,false);
    	
    }
/*
    boolean getResourceName(int resID, boolean allowUtf8, resource_name outName) {
    	
    }
*/

}
