package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Represents the top-level resource archive file.
 * 
 * It consists of an ordered set of Chunks. Typically, this will include
 * only a single Table chunk, but other structures are possible.
 * 
 * @author iclelland
 *
 */
public class ResourceArchive extends Streamable {
    Vector<Chunk> components;

    public ResourceArchive() {
    	components = new Vector<Chunk>();
    }

	@Override
	public int computeSize() throws UnsupportedEncodingException {
        int payloadSize = 0;
        for (Chunk component : components) {
            payloadSize += component.getSize();
        }
        return payloadSize;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        for (Chunk component : components) {
            component.write(output);
        }
    }

    public void addComponent(Chunk component) {
    	components.add(component);
    }
}
