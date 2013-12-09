package data;

/**
 * WhiteLine objects represent single drawn strokes (lines) on a white board and
 * are immutable. They are used both on the client and server sides to this end.
 */
public class WhiteLine {
	private final int x1, x2, y1, y2;
	private final java.awt.Color color;
	private final java.awt.BasicStroke thickness;
	public static final int Y_SIZE = 600, X_SIZE = 800;

	/**
	 * Constructs a WhiteLine object with the given properties for the line.
	 * 
	 * @param x1
	 *            The X-coordinate of the origin of the line (x1,y1) in the
	 *            range [0,X_SIZE).
	 * @param y1
	 *            The Y-coordinate of the origin of the line (x1,y1) in the
	 *            range [0,Y_SIZE).
	 * @param x2
	 *            The X-coordinate of the terminus of the line (x2,y2) in the
	 *            range [0,X_SIZE).
	 * @param y2
	 *            The Y-coordinate of the terminus of the line (x2,y2) in the
	 *            range [0,Y_SIZE).
	 * @param color
	 *            The color of the represented line.
	 * @param thickness
	 *            The thickness of the represented line from the integer range
	 *            [1,10].
	 * @throws IllegalArgumentException
	 *             One of the parameters was outside of the specified bounds.
	 */
	public WhiteLine(int x1, int y1, int x2, int y2, java.awt.Color color,
			int thickness) {
		// bound checking
		if (x1 < 0 || x1 >= X_SIZE)
			throw new IllegalArgumentException(
					"The specified 'x1' value was out of bounds.");
		if (x1 < 0 || x2 >= X_SIZE)
			throw new IllegalArgumentException(
					"The specified 'x2' value was out of bounds.");
		if (x1 < 0 || y1 >= Y_SIZE)
			throw new IllegalArgumentException(
					"The specified 'y1' value was out of bounds.");
		if (x1 < 0 || y2 >= Y_SIZE)
			throw new IllegalArgumentException(
					"The specified 'y2' value was out of bounds.");
		if (thickness < 0 || thickness > 10)
			throw new IllegalArgumentException(
					"The specified 'thickness' value was out of bounds.");

		// color and thickness
		this.thickness = new java.awt.BasicStroke(thickness);
		this.color = color; // Color objects are immutable

		// coordinates
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	/**
	 * Returns the X-coordinate of the origin of the line (x1,y1).
	 * 
	 * @return the x1 value
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * Returns the Y-coordinate of the origin of the line (x1,y1)
	 * 
	 * @return the y1 value
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * Returns the X-coordinate of the terminus of the line (x2,y2).
	 * 
	 * @return the x2 value
	 */
	public int getX2() {
		return x2;
	}

	/**
	 * Returns the Y-coordinate of the terminus of the line (x2,y2)
	 * 
	 * @return the y2 value
	 */
	public int getY2() {
		return y2;
	}

	/**
	 * Returns the color of the line.
	 * 
	 * @return the line color as a Color object
	 */
	public java.awt.Color getColor() {
		return color;
	}

	/**
	 * Returns a BasicStroke object with the specified thickness of the line.
	 * 
	 * @return the line thickness as a BasicStroke object
	 */
	public java.awt.BasicStroke getThickness() {
		return thickness;
	}

	/**
	 * The complete properties of the line expressed in the STROKE message
	 * format with the BOARD_ID omitted. For example, a thin red line across the
	 * board would produce "stroke 1 0 0 799 599 255 0 0".
	 * 
	 * @return a string representation of this WhiteLine
	 */
	@Override
	public String toString() {
		// convert properties to Strings
		String thick = String.valueOf(Math.round(this.thickness.getLineWidth()));
		String color = String.valueOf(this.color.getRed()) + " "
				+ String.valueOf(this.color.getGreen()) + " "
				+ String.valueOf(this.color.getBlue());
		String coords = String.valueOf(x1) + " " + String.valueOf(y1) + " "
				+ String.valueOf(x2) + " " + String.valueOf(y2);

		// return in STROKE format
		return "stroke " + thick + " " + coords + " " + color;
	}
}
