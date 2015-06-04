package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class PortFigure extends RectangleFigure {
	
	private Color fillColor;
	private Color selectedColor;
	private boolean selected = false;
	private String name;
	protected int width;
	protected int height;
	public PortFigure(String name) {
		this(name,ActorFigure.ANCHOR_WIDTH,ActorFigure.ANCHOR_HEIGTH);
	}
	public PortFigure(String name,int width,int height) {
		super();
		this.width = width;
		this.height = height;
		setOpaque(false);
		setName(name);
		setSize(width,height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
	 */
	public Dimension getPreferredSize(int wHint, int hHint) {
		return new Dimension(width, height);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color getFillColor() {
		if (selected&&selectedColor!=null) {
			return selectedColor;
		}
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * 
	 * @param isSelected
	 * @param colorCode one of SWT.COLOR_XXX or -1.
	 */
	public void setSelectedColor(boolean isSelected, int colorCode) {
		selected = isSelected;
		if (colorCode>-1 && isSelected) {
			try {
				selectedColor = Display.getDefault().getSystemColor(colorCode);
			} catch (Throwable ne) {
				System.out.println("Internal error, the color code '"+colorCode+" is not allowed!");
				ne.printStackTrace();
				selectedColor = null;
			}
}

	}

}
