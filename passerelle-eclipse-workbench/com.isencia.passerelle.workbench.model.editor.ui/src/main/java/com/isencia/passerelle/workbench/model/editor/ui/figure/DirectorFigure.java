package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;


public class DirectorFigure extends AbstractBaseFigure {
	

	public final static int DIRECTOR_WIDTH = 120;
	public final static int DIRECTOR_HEIGHT = 60;
	public final static Dimension DIRECTOR_SIZE = new Dimension(DIRECTOR_WIDTH,DIRECTOR_HEIGHT);
	public final static Color DIRECTOR_BACKGROUND_COLOR = new Color(null,204,0,0);

    private Body body = null;
    
	@Override
	public Color getDefaultColor() {
		return DIRECTOR_BACKGROUND_COLOR;
	}

    public DirectorFigure(String name,Class type, Image image) {
    	super(name,type);
        body = new Body();
        body.initImage(image);
        add(body);
        
    	setBackgroundColor(DIRECTOR_BACKGROUND_COLOR);
    }

    private class Body extends RoundedRectangle {
    	ImageFigure imageFigure;

        public Body() {
            ToolbarLayout layout = new ToolbarLayout();
            layout.setVertical(true);
            setLayoutManager(layout);
            //setCornerDimensions(new Dimension(10, 10));
            setOpaque(true);
        }

        private void initImage(Image image) {
        	if( image != null ) {
        		imageFigure = new ImageFigure(image);
        		imageFigure.setAlignment(PositionConstants.WEST);
        		imageFigure.setBorder(new MarginBorder(5,5,10,10));
        		add(imageFigure);
        	}
        }
       /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
         */
        public Dimension getPreferredSize(int wHint, int hHint) {
            return getParent().getSize().getCopy();
        }

       
        protected void fillShape(Graphics graphics) {
        	graphics.pushState();
        	graphics.setForegroundColor(ColorConstants.white);
        	graphics.setBackgroundColor(getBackgroundColor());
			final Rectangle bounds = getBounds();
			graphics.fillGradient(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2, false);
        	graphics.popState();
        }
		protected void outlineShape(Graphics graphics) {
			
			graphics.setForegroundColor(ColorConstants.gray);
			super.outlineShape(graphics);
		}
      
    }

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		return DIRECTOR_SIZE;
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#layout()
     */
    public void validate() {
        LayoutManager layout = getLayoutManager();
        layout.setConstraint(body, new Rectangle(0,0,-1,-1));
        super.validate();
    }

	
    public void setBackgroundColor(Color c) {
        if (body != null) {
            body.setBackgroundColor(c);
        }
    }
}
