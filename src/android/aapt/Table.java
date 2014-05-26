package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class Table extends Chunk {
    private StringPool stringPool;
    private Vector<Package> packages;

    public Table() {
    	packages = new Vector<Package>();
    }

	@Override
	public int getChunkType() {
		return RES_TABLE_TYPE;
	}

    @Override
    public int getHeaderSize() {
        return 12;
    }

    @Override
    public int computeSize() throws UnsupportedEncodingException {
        int payloadSize = getHeaderSize() + getStringPool().getSize();
        for (Package p : packages) {
            payloadSize += p.getSize();
        }
        return payloadSize;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        writeHeader(output);
        getStringPool().write(output);
        for (Package p : packages) {
            p.write(output);
        }
    }

	@Override
	public void writeHeader(OutputStream output) throws IOException {
		writeBaseHeader(output);
		writeUint32(output, packages.size());
	}

	StringPool getStringPool() {
		return stringPool;
	}

	void setStringPool(StringPool stringPool) {
		this.stringPool = stringPool;
	}

	void addPackage(Package pkg) {
		packages.add(pkg);
	}
}
