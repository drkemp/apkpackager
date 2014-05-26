package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class StringPool extends Chunk {
    private Vector<String> strings;
    private boolean useUTF8;
    private boolean sortStrings;

    public StringPool() {
    	strings = new Vector<String>();
    }
    
	@Override
	public int getChunkType() {
		return RES_STRING_POOL_TYPE;
	}

    @Override
    public int getHeaderSize() {
        return 28;
    }

    public void addString(String string) {
    	strings.add(string);
    }
    
    Vector<Integer> offsets = null;
    int padding = 0;

    @Override
    public int computeSize() throws UnsupportedEncodingException {
        offsets = new Vector<Integer>(strings.size());
        int payloadSize = strings.size() * 4;
        int currentOffset = 0;
        for (String string : strings) {
            offsets.add(currentOffset);
            int strSize = string.length();
            int totalSize; // # of bytes required for storage
            if (useUTF8) {
            	int encSize = string.getBytes("UTF-8").length;
            	totalSize = ((strSize > 127) ? 2 : 1) + (encSize > 127 ? 2 : 1) + encSize + 1;
            } else {
            	totalSize = (((strSize > 32767) ? 2 : 1) + strSize + 1) * 2;
            }
            payloadSize += totalSize;
            currentOffset += totalSize;
        }
    	padding = ((payloadSize + 3) & -4) - payloadSize; 
        return getHeaderSize() + payloadSize + padding;
    }

    void writeString(OutputStream output, String string) throws IOException {
        int strSize = string.length();
        byte[] encString;
        if (useUTF8) {
            encString = string.getBytes("UTF-8");
            int encSize = encString.length;
            if (strSize > 127) {
            	writeUint8(output, ((strSize >> 8) & 0x7f) | 0x80);
            }
           	writeUint8(output, (strSize & 0xff));
            if (encSize > 127) {
            	writeUint8(output, ((encSize >> 8) & 0x7f) | 0x80);
            }
           	writeUint8(output, (encSize & 0xff));
            output.write(encString);
           	writeUint8(output, 0);
        } else {
            encString = string.getBytes("UTF-16LE");
            if (strSize > 32767) {
            	writeUint16(output, ((strSize >> 16) & 0x7fff) | 0x8000);
            }
           	writeUint16(output, (strSize & 0xffff));
           	output.write(encString);
           	writeUint16(output, 0);
        }
    }

    private int getFlags() {
    	int flags = 0;
    	if (sortStrings) {
    		flags |= 0x0001;
    	}
    	if (useUTF8) {
    		flags |= 0x0100;
    	}
    	return flags;
    }

    @Override
	public void writeHeader(OutputStream output) throws IOException {
		writeBaseHeader(output);
		writeUint32(output, strings.size());
		writeUint32(output, 0); // styleCount
		writeUint32(output, getFlags());
		writeUint32(output, strings.size() * 4 + getHeaderSize());
		writeUint32(output, 0); // stylesStart
	}

	@Override
    public void write(OutputStream output) throws IOException {
        writeHeader(output);
        for (int i = 0, n = offsets.size(); i < n; ++i) {
            writeUint32(output, offsets.get(i));
        }
        for (String string : strings) {
            writeString(output, string);
        }
        for (int i = 0; i < padding; ++i) {
        	writeUint8(output, 0);
        }
    }

	boolean willUseUTF8() {
		return useUTF8;
	}

	void setUseUTF8(boolean useUTF8) {
		this.useUTF8 = useUTF8;
	}

	boolean willSortStrings() {
		return sortStrings;
	}

	void setSortStrings(boolean sortStrings) {
		this.sortStrings = sortStrings;
	}

	public int count() {
		return strings.size();
	}

}
