package org.chromium.aapt;

public class AccessorCookie {
 	//TODO: this class is imcomplete
	    SourcePos sourcePos;
	    String attr;
	    String value;

	    AccessorCookie( SourcePos p, String a, String v) {
	        sourcePos=p;
	        attr =a;
	        value=v;
	    }

}
