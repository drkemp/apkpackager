package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Higher-level interface to a collection of chunks within a table.
 * 
 * A type bundle consists of a single type spec chunk, declaring a number
 * of configuration variants, followed by a type chunk for each of those
 * variants. This class allows the type spec to be managed automatically.
 * 
 * @author iclelland
 *
 */
public class TypeBundle extends Streamable {
	private int typeId;
    TypeSpec spec;
    Vector<Type> types;

    public TypeBundle(int typeId) {
    	this.typeId = typeId;
    	spec = new TypeSpec(typeId);
    	types = new Vector<Type>();
    }

    @Override
    public int computeSize() throws UnsupportedEncodingException {
        int payloadSize = spec.getSize();
        for (Type type : types) {
            payloadSize += type.getSize();
        }
        return  payloadSize;
    }

    @Override
    public void write(OutputStream output) throws IOException {
        if (spec != null) {
            spec.write(output);
        }
        for (Type type : types) {
            type.write(output);
        }
    }

    public void addType(Type type) {
    	spec.addConfigVariant(type.config.getVariant());
    	types.add(type);
    }
}
