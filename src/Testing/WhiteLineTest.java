package Testing;

import java.awt.Color;
import java.awt.BasicStroke;

import static org.junit.Assert.*;
import java.io.IOException;

import org.junit.Test;

import data.WhiteLine;

/**
 * Testing Documentation: WhiteLineTest
 * The tests for WhiteLine are short and succinct, as it is a very basic class (compared to User and MasterBoard)
 * Begin by testing the construction of a valid instance of WhiteLine - this should obviously construct successfully
 * and throw no exceptions. Next we test a variety of invalid constructions of WhiteLine, including x-values, y-values,
 * and thickness values that are outside of the allowable range. Each of these tests should run into an exception when
 * attempting to construct the given WhiteLine. Next, we test boundary conditions, by attempting to draw WhiteLines
 * around the border of the canvas (essentially forming a rectangle with 4 instances of WhiteLine). These should
 * construct successfully as we have chosen to draw them along the inside edge of the canvas. Finally, we test the
 * remaining method, toString, to ensure that it outputs WhiteLine's properties appropriately.
 */

public class WhiteLineTest {

	@Test
	public void simpleConstructorTest() throws IOException
	{
		// Test construction of simple, valid WhiteLine object
		WhiteLine testLine = new WhiteLine(50,100, 250, 500, Color.blue, 7);
		assertEquals(testLine.getX1(), 50);
		assertEquals(testLine.getY1(), 100);
		assertEquals(testLine.getX2(), 250);
		assertEquals(testLine.getY2(), 500);
		assertEquals(testLine.getColor(), Color.blue);
		assertEquals(testLine.getThickness(), new BasicStroke(7));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void xOutOfBoundsTest() throws IOException
	{
		// Test construction where x-coordinates are not valid (0 <= x < X_SIZE)
		WhiteLine testLine = new WhiteLine(150, 75, 850, 450, Color.red, 2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void yOutOfBoundsTest() throws IOException
	{
		// Test construction where y-coordinates are not valid (0 <= y < Y_SIZE)
		WhiteLine testLine = new WhiteLine(150, -74, 50, 450, Color.red, 2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalidThicknessTest() throws IOException
	{
		// Test construction when an invalid thickness parameter is used
		WhiteLine testLine = new WhiteLine(200, 175, 400, 550, Color.green, -3);
	}
	
	@Test
	public void boundaryTests() throws IOException
	{
		// Test validity of WhiteLine along top edge of canvas
		WhiteLine testLine1 = new WhiteLine(0, 0, 799, 0, Color.black, 7);
		assertEquals(testLine1.getX1(), 0);
		assertEquals(testLine1.getY1(), 0);
		assertEquals(testLine1.getX2(), 799);
		assertEquals(testLine1.getY2(), 0);
		assertEquals(testLine1.getColor(), Color.black);
		assertEquals(testLine1.getThickness(), new BasicStroke(7));
		
		// Test validity of WhiteLine along bottom edge of canvas
		WhiteLine testLine2 = new WhiteLine(0, 599, 799, 599, Color.black, 7);
		assertEquals(testLine2.getX1(), 0);
		assertEquals(testLine2.getY1(), 599);
		assertEquals(testLine2.getX2(), 799);
		assertEquals(testLine2.getY2(), 599);
		assertEquals(testLine2.getColor(), Color.black);
		assertEquals(testLine2.getThickness(), new BasicStroke(7));
		
		// Test validity of WhiteLine along left edge of canvas
		WhiteLine testLine3 = new WhiteLine(0, 0, 0, 599, Color.black, 7);
		assertEquals(testLine3.getX1(), 0);
		assertEquals(testLine3.getY1(), 0);
		assertEquals(testLine3.getX2(), 0);
		assertEquals(testLine3.getY2(), 599);
		assertEquals(testLine3.getColor(), Color.black);
		assertEquals(testLine3.getThickness(), new BasicStroke(7));

		// Test validity of WhiteLine along right edge of canvas
		WhiteLine testLine4 = new WhiteLine(799, 0, 799, 599, Color.black, 7);
		assertEquals(testLine4.getX1(), 799);
		assertEquals(testLine4.getY1(), 0);
		assertEquals(testLine4.getX2(), 799);
		assertEquals(testLine4.getY2(), 599);
		assertEquals(testLine4.getColor(), Color.black);
		assertEquals(testLine4.getThickness(), new BasicStroke(7));
		
		// Test validity of a diagonal WhiteLine from the top-left to the bottom-right corner
		WhiteLine testLine5 = new WhiteLine(0, 0, 799, 599, Color.black, 7);
		assertEquals(testLine5.getX1(), 0);
		assertEquals(testLine5.getY1(), 0);
		assertEquals(testLine5.getX2(), 799);
		assertEquals(testLine5.getY2(), 599);
		assertEquals(testLine5.getColor(), Color.black);
		assertEquals(testLine5.getThickness(), new BasicStroke(7));
		
		// Test validity of a diagonal WhiteLine from the top-right to the bottom-left corner
		WhiteLine testLine6 = new WhiteLine(799, 0, 0, 599, Color.black, 7);
		assertEquals(testLine6.getX1(), 799);
		assertEquals(testLine6.getY1(), 0);
		assertEquals(testLine6.getX2(), 0);
		assertEquals(testLine6.getY2(), 599);
		assertEquals(testLine6.getColor(), Color.black);
		assertEquals(testLine6.getThickness(), new BasicStroke(7));
	}
	
	@Test
	public void stringFormatTest() throws IOException
	{
		// Test the output of WhiteLine's toString() method
		WhiteLine testLine = new WhiteLine(300, 250, 50, 550, Color.pink, 5);
		String expected = "stroke 5 300 250 50 550 255 175 175";
		assertEquals(expected, testLine.toString());
	}
}