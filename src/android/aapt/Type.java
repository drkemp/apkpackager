package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class Type extends Chunk {
    int typeId;
    Config config;
    Vector<Entry> entries;
    
    Vector<Integer> offsets = null;

    public Type() {
    	entries = new Vector<Entry>();
    }
    
	@Override
	public int getChunkType() {
		return RES_TABLE_TYPE_TYPE;
	}

    @Override
    public int getHeaderSize() throws UnsupportedEncodingException {
        return 20 + config.getSize();
    }

    @Override
   	public int computeSize() throws UnsupportedEncodingException {
        offsets = new Vector<Integer>(entries.size());
        int payloadSize = getHeaderSize() + entries.size() * 4;
        int currentOffset = 0;
        for (Entry entry : entries) {
            offsets.add(currentOffset);
            int entrySize = entry.getSize();
            payloadSize += entrySize;
            currentOffset += entrySize;
        }
        return payloadSize;
	}

	@Override
	public void writeHeader(OutputStream output) throws IOException {
		writeBaseHeader(output);
		writeUint8(output, typeId);
		writeUint8(output, 0);
		writeUint16(output, 0);
		writeUint32(output, entries.size());
		writeUint32(output, getHeaderSize() + entries.size() * 4); // entries start
		config.write(output);
	}

	@Override
    public void write(OutputStream output) throws IOException {
        writeHeader(output);
        for (int i = 0, n = offsets.size(); i < n; ++i) {
            writeUint32(output, offsets.get(i));
        }
        for (Entry entry : entries) {
            entry.write(output);
        }
    }
	
	public void addEntry(Entry entry) {
		entries.add(entry);
	}

}
