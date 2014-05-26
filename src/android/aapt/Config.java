package org.chromium.aapt;

import java.io.IOException;
import java.io.OutputStream;

public class Config extends Streamable {

	/* Constants */
	public static final int DENSITY_LOW = 120;
	public static final int DENSITY_MEDIUM = 160;
	public static final int DENSITY_TV = 213;
	public static final int DENSITY_HIGH = 240;
	public static final int DENSITY_XHIGH = 320;
	public static final int DENSITY_XXHIGH = 480;
	public static final int DENSITY_XXXHIGH = 640;
	
	Integer mcc;
	Integer mnc;
	String language;
	String country;
	Integer orientation;
	Integer touchscreen;
	Integer density;
	Integer keyboard;
	Integer navigation;
	Integer inputFlags;
	Integer screenWidth;
	Integer screenHeight;
	Integer sdkVersion;
	Integer minorVersion;
	Integer screenLayout;
	Integer uiMode;
	Integer smallestScreenWidthDp;
	Integer screenWidthDp;
	Integer screenHeightDp;

	public boolean equals(Config other) {
		return (
		    (this.mcc == other.mcc) &&
		    (this.mnc == other.mnc) &&
		    (this.language == other.language) &&
		    (this.country == other.country) &&
		    (this.orientation == other.orientation) &&
		    (this.touchscreen == other.touchscreen) &&
		    (this.density == other.density)
		);
	}
	
	@Override
	public int computeSize() {
		// TODO Determine when extended locales (and 48-byte configs) are appropriate
		return 36;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		writeUint32(output, getSize());
		
		writeUint16(output, mcc == null ? 0 : mcc);
		writeUint16(output, mnc == null ? 0 : mnc);

		if (language == null) {
			writeUint16(output, 0);
		} else {
			output.write(language.getBytes("UTF-8"),0,2);
		}
		if (country == null) {
			writeUint16(output, 0);
		} else {
			output.write(country.getBytes("UTF-8"),0,2);
		}

		writeUint8(output, orientation == null ? 0 : orientation);
		writeUint8(output, touchscreen == null ? 0 : touchscreen);
		writeUint16(output, density == null ? 0 : density);

		writeUint8(output, keyboard == null ? 0 : keyboard);
		writeUint8(output, navigation == null ? 0 : navigation);
		writeUint8(output, inputFlags == null ? 0 : inputFlags);
		writeUint8(output, 0);

		writeUint16(output, screenWidth == null ? 0 : screenWidth);
		writeUint16(output, screenHeight == null ? 0 : screenHeight);

		writeUint16(output, sdkVersion == null ? 0 : sdkVersion);
		writeUint16(output, minorVersion == null ? 0 : minorVersion);

		writeUint8(output, screenLayout == null ? 0 : screenLayout);
		writeUint8(output, uiMode == null ? 0 : uiMode);
		writeUint16(output, smallestScreenWidthDp == null ? 0 : smallestScreenWidthDp);

		writeUint16(output, screenWidthDp == null ? 0 : screenWidthDp);
		writeUint16(output, screenHeightDp == null ? 0 : screenHeightDp);
	}

	/**
	 * Compute the appropriate variant flags for this configuration.
	 */
	public ConfigVariant getVariant() {
		int flags = ConfigVariant.NONE;
		if (mcc != null) flags |= ConfigVariant.MCC;
		if (mnc != null) flags |= ConfigVariant.MNC;
		if (language != null || country != null) flags |= ConfigVariant.LOCALE;
		if (orientation != null) flags |= ConfigVariant.ORIENTATION;
		if (density != null) flags |= ConfigVariant.DENSITY;
		if (keyboard != null) flags |= ConfigVariant.KEYBOARD;
		if (navigation != null) flags |= ConfigVariant.NAVIGATION;
		if (inputFlags != null && ((inputFlags & 0x0f) != 0)) flags |= ConfigVariant.KEYBOARD_HIDDEN;
		if (orientation != null) flags |= ConfigVariant.ORIENTATION;
		if (screenWidth != null || screenHeight != null) flags |= ConfigVariant.SCREEN_SIZE;
		if (sdkVersion != null || minorVersion != null ) flags |= ConfigVariant.VERSION;
		if (screenLayout != null && ((screenLayout & 0x3f) != 0)) flags |= ConfigVariant.SCREEN_LAYOUT;
		if (screenLayout != null && ((screenLayout & 0xc0) != 0)) flags |= ConfigVariant.LAYOUTDIR;
		if (uiMode != null) flags |= ConfigVariant.UI_MODE;
		if (density != null) flags |= ConfigVariant.DENSITY;
		if (smallestScreenWidthDp != null) flags |= ConfigVariant.SMALLEST_SCREEN_SIZE;
		if (screenWidthDp != null || screenHeightDp != null) flags |= ConfigVariant.SCREEN_SIZE;
		return new ConfigVariant(flags);
	}

	public static Config fromDirNameParts(String[] dirNameParts) {
		Config cfg = new Config();
		if (dirNameParts.length > 1) {
			if (dirNameParts[1] == "ldpi") cfg.density = DENSITY_LOW;
			if (dirNameParts[1] == "mdpi") cfg.density = DENSITY_MEDIUM;
			if (dirNameParts[1] == "hdpi") cfg.density = DENSITY_HIGH;
			if (dirNameParts[1] == "xhdpi") cfg.density = DENSITY_XHIGH;
			if (dirNameParts[1] == "xxhdpi") cfg.density = DENSITY_XXHIGH;
			if (dirNameParts[1] == "xxxhdpi") cfg.density = DENSITY_XXXHIGH;
		}
		return cfg;
	}
}
