package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class TypeSpec extends Chunk {
    int typeId;
    Vector<ConfigVariant> variants;

    public TypeSpec() {
    	variants = new Vector<ConfigVariant>();
    }
    
    public TypeSpec(int typeId) {
    	this.typeId = typeId;
    	variants = new Vector<ConfigVariant>();
    }
    
	@Override
	public int getChunkType() {
		return RES_TABLE_TYPE_SPEC_TYPE;
	}

    @Override
    public int getHeaderSize() {
    	return 16;
    }
    
    @Override
	public int computeSize() throws UnsupportedEncodingException {
		return getHeaderSize() + variants.size() * 4;
	}

    @Override
    public void write(OutputStream output) throws IOException {
        writeHeader(output);
        for (ConfigVariant variant : variants) {
            variant.write(output);
        }
    }

	@Override
	public void writeHeader(OutputStream output) throws IOException {
		writeBaseHeader(output);
		writeUint8(output, typeId);
		writeUint8(output, 0);
		writeUint16(output, 0);
		writeUint32(output, variants.size());
	}

	public void addConfigVariant(ConfigVariant variant) {
		variants.add(variant);
	}
}
