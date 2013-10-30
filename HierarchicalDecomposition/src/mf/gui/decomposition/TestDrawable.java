package mf.gui.decomposition;

import mf.gui.ButtonRow;
import mf.gui.Markable;

public class TestDrawable extends Drawable implements Runnable{

	private Boolean stop = false;
	
	public TestDrawable(String name, Markable m, ButtonRow buttonRow) {
		super(name, m, buttonRow);
	}

	@Override
	public synchronized void draw() {
		Integer x = 0;
		Integer y = 0;
		
		while(!stop) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
			x = (x+3) % m.getImageHeight();
			y = (y+3) % m.getImageWidth();
			m.markPixel(x, y);
		}
	}

	@Override
	public void onActivate() {
		System.out.println("activated!");
		new Thread(this).start();
	}

	@Override
	public void run() {
		draw();
	}
	
	public void stop() {
		stop = true;
	}

}
