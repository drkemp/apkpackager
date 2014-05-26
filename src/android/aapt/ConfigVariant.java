package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;

public class ConfigVariant extends Streamable {
	
	public static final int NONE = 0;
	public static final int MCC = 0x0001;
	public static final int MNC = 0x0002;
	public static final int LOCALE = 0x0004;
	public static final int TOUCHSCREEN = 0x0008;
	public static final int KEYBOARD = 0x0010;
	public static final int KEYBOARD_HIDDEN = 0x0020;
	public static final int NAVIGATION = 0x0040;
	public static final int ORIENTATION = 0x0080;
	public static final int DENSITY = 0x0100;
	public static final int SCREEN_SIZE = 0x0200;
	public static final int VERSION = 0x0400;
	public static final int SCREEN_LAYOUT = 0x0800;
	public static final int UI_MODE = 0x1000;
	public static final int SMALLEST_SCREEN_SIZE = 0x2000;
	public static final int LAYOUTDIR = 0x4000;
	
    private int flags;

    public ConfigVariant(int flags) {
    	this.flags = flags;
    }
    
	@Override
	public int computeSize() {
		return 4;
	}

	@Override
	public void write(OutputStream output) throws IOException {
        writeUint32(output, flags);
    }

	int getFlags() {
		return flags;
	}

	void setFlags(int flags) {
		this.flags = flags;
	}

}
