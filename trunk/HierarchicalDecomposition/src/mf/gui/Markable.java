package mf.gui;

public interface Markable {

	public void markPixel(Integer x , Integer y);
	public void markArea(Integer[] x , Integer[] y);
	public void startLoading();
	public void stopLoading();
	public void clear();
	
}
