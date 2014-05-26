package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class MapEntry extends Entry {

	TableRef parentRef;
	Vector<Map> maps;
	
	public MapEntry() {
		maps = new Vector<Map>();
	}

	@Override
	public int computeSize() throws UnsupportedEncodingException {
        int payloadSize = 16; // Hard-coded header size
		for (Map map : maps) {
            payloadSize += map.getSize();
        }
        return payloadSize;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		writeUint16(output, 16); // Hard-coded size, does not include maps
		writeUint16(output, isPublic ? 3 : 1);
		writeUint32(output, stringPoolRef);
		if (parentRef == null) {
			writeUint32(output, 0);
		} else {
			parentRef.write(output);
		}
		writeUint32(output, maps.size());
		for (Map map : maps) {
			map.write(output);
		}
	}
	
	public void addMap(Map map) {
		maps.add(map);
	}
}
