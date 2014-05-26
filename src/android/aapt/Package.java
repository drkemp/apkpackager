package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class Package extends Chunk {
    private int id;
    private String name;
    private StringPool typeStrings;
    private StringPool keyStrings;
    // We don't have private types and keys yet
    /* int lastPublicType; */
    /* int lastPublicKey; */
    Vector<TypeBundle> types;

    public Package() {
    	typeStrings = new StringPool();
    	typeStrings.setUseUTF8(true);
    	keyStrings = new StringPool();
    	keyStrings.setUseUTF8(true);
    	types = new Vector<TypeBundle>();
    }

	@Override
	public int getChunkType() {
		return RES_TABLE_PACKAGE_TYPE;
	}

    @Override
    public int getHeaderSize() {
        return 284;
    }

    @Override
    public int computeSize() throws UnsupportedEncodingException {
        int payloadSize = getHeaderSize() + typeStrings.getSize() + keyStrings.getSize();
        for (TypeBundle type : types) {
            payloadSize += type.getSize();
        }
        return payloadSize;
    }

    @Override
	public void writeHeader(OutputStream output) throws IOException {
		writeBaseHeader(output);
		writeUint32(output, id);
		writePaddedUTF16(output, name, 128);
		writeUint32(output, getHeaderSize()); // offset of type string pool
		writeUint32(output, typeStrings.count()); // all strings public
		writeUint32(output, getHeaderSize() + typeStrings.getSize()); // offset of key string pool
		writeUint32(output, keyStrings.count()); // all strings public
	}

    @Override
    public void write(OutputStream output) throws IOException {
        writeHeader(output);
        typeStrings.write(output);
        keyStrings.write(output);
        for (TypeBundle type : types) {
            type.write(output);
        }
    }

	int getId() {
		return id;
	}

	void setId(int id) {
		this.id = id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}
	
	void addTypeString(String typeString) {
		typeStrings.addString(typeString);
	}

	void addKeyString(String keyString) {
		keyStrings.addString(keyString);
	}
	
	void addTypeBundle(TypeBundle bundle) {
		types.add(bundle);
	}

}
