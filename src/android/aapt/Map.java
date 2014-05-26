package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Map extends Streamable {

	public static final int ATTR_TYPE = 0x01000000;
	public static final int ATTR_MIN = 0x01000001;
	public static final int ATTR_MAX = 0x01000002;
	public static final int ATTR_L10N = 0x01000003;
	public static final int ATTR_OTHER = 0x01000004;
	public static final int ATTR_ZERO = 0x01000005;
	public static final int ATTR_ONE = 0x01000006;
	public static final int ATTR_TWO = 0x01000007;
	public static final int ATTR_FEW = 0x01000008;
	public static final int ATTR_MANY = 0x01000009;

	public static final int TYPE_ANY = 0x0000ffff;
	public static final int TYPE_REFERENCE = 0x00000001;
	public static final int TYPE_STRING = 0x00000002;
	public static final int TYPE_INTEGER = 0x00000004;
	public static final int TYPE_BOOLEAN = 0x00000008;
	public static final int TYPE_COLOR = 0x00000010;
	public static final int TYPE_FLOAT = 0x00000020;
	public static final int TYPE_DIMENSION = 0x00000040;
	public static final int TYPE_FRACTION = 0x00000080;
	public static final int TYPE_ENUM = 0x00010000;
	public static final int TYPE_FLAGS = 0x00020000;
	
	public static final int L10N_NOT_REQUIRED = 0;
	public static final int L10N_SUGGESTED = 1;

	TableRef name;
	Value value;

	@Override
	public int computeSize() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 4 + value.getSize();
	}

	@Override
	public void write(OutputStream output) throws IOException {
		name.write(output);
		value.write(output);
	}

}
