package fr.jcwebinfo.qrcode;

public class Color{
	private int r, g, b, a;

	public Color() {
		super();
	}

	public static Color valueOf(int r, int g, int b, int a){
		Color c;
		c = new Color();
		c.r = r;
		c.g = g;
		c.b = b;
		c.a = a;
		return c;
	}

	public int toArgb() {
		return ((this.a & 0xff) << 24) | ((this.r & 0xff) << 16) | ((this.g & 0xff) << 8) | (this.b & 0xff);
	}
}
