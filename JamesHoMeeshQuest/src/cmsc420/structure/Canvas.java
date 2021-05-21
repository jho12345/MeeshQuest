package cmsc420.structure;

import cmsc420.drawing.CanvasPlus;

public class Canvas {

	public static CanvasPlus canvas;
	
	private int spW, spH;

	private Canvas(){}

	public static synchronized CanvasPlus getInstance() {
		if (canvas == null) {
			canvas = new CanvasPlus();
		}
		return canvas;
	}

	public static boolean isEnabled() {
		return canvas != null;
	}

	public static void setEnabled() {
		canvas = new CanvasPlus("MeeshQuest");
	}

	public static void setDisabled() {
		canvas = null;
	}

	public static void dispose() {
		if (canvas != null) {
            canvas.dispose();
		}
	}
	
	public void setFrameSize(int w, int h) {
		this.spH = h;
		this.spW = w;
	}
}
