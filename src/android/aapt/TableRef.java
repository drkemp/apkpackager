package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class TableRef extends Streamable {

	int packageIndex;
	int typeIndex;
	int entryIndex;
	
	@Override
	public int computeSize() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		writeUint8(output, packageIndex);
		writeUint8(output, typeIndex);
		writeUint16(output, entryIndex);
	}

}
