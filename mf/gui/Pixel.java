package mf.gui;


/**
 * Representation of a pixel. Contains x- and y-coordinates.
 * Basically equal to the Pair-class here: <a>http://stackoverflow.com/questions/156275/what-is-the-equivalent-of-the-c-pairl-r-in-java</a>
 * 
 * @author moritzfuchs
 * @date 05.09.2013
 */
public class Pixel {
	/**
	 * x-coordinate
	 */
	private Integer x;
	/**
	 * y-coordinate
	 */
    private Integer y;

    public Pixel(Integer x, Integer y) {
    	super();
    	this.x = x;
    	this.y = y;
    }

    public int hashCode() {
    	int hashFirst = x != null ? x.hashCode() : 0;
    	int hashSecond = y != null ? y.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
    	if (other instanceof Pixel) {
    		Pixel otherPair = (Pixel) other;
    		return 
    		((  this.x == otherPair.x ||
    			( this.x != null && otherPair.x != null &&
    			  this.x.equals(otherPair.x))) &&
    		 (	this.y == otherPair.y ||
    			( this.y != null && otherPair.y != null &&
    			  this.y.equals(otherPair.y))) );
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + x + ", " + y + ")"; 
    }

    /**
     * Returns the x-coordinate.
     * 
     * @return Integer : x-coordinate.
     */
    public Integer getX() {
    	return x;
    }

    /**
     * Returns the y-coordinate.
     * 
     * @return Integer : y-coordinate.
     */
    public Integer getY() {
    	return y;
    }

}
