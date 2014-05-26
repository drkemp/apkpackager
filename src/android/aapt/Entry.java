package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Entry extends Streamable {

	boolean isPublic;
	int stringPoolRef;
	
	@Override
	public int computeSize() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		writeUint16(output, getSize());
		writeUint16(output, isPublic ? 2 : 0);
		writeUint32(output, stringPoolRef);
	}

}
