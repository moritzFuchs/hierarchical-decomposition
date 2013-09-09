package mf.gui;

public class Pixel {
	private Integer x;
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

    public Integer getX() {
    	return x;
    }

    public void setX(Integer x) {
    	this.x = x;
    }

    public Integer getY() {
    	return y;
    }

    public void setSecond(Integer y) {
    	this.y = y;
    }
}
