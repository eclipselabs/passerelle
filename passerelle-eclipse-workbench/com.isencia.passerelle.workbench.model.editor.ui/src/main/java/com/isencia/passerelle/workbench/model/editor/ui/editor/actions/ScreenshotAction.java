package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.io.File;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.ui.IPasserelleEditor;

public class ScreenshotAction extends SelectionAction {

	public static final String ID = ScreenshotAction.class.getName();
	
	public ScreenshotAction(IPasserelleEditor part) {
		super((IWorkbenchPart)part);
		setId(ID);
	}
	
	protected void init() {
		setText("Screenshot");
		setToolTipText("Screenshot of workflow in png format.");
		setEnabled(true);
		setImageDescriptor(Activator.getImageDescriptor("icons/camera.gif"));
	}

	private static String previousDir;
	
	@Override
	public void run() {

	    // Prompt for file name
		FileDialog dialog = new FileDialog(Display.getDefault().getShells()[0], SWT.SAVE);
		dialog.setFilterNames(new String[] {"PNG Files", "All Files (*.*)" });
	    dialog.setFilterExtensions(new String[] { "*.png", "*.*" }); // Windows
	    if (previousDir!=null) dialog.setFilterPath(previousDir);
	    String path = dialog.open();
	    if (path == null || path.length() <= 0) return;
	    
	    final File file = new File(path);
	    previousDir = file.getParent();
	    if (file.exists()) {
	    	final boolean ok = MessageDialog.openConfirm(Display.getDefault().getShells()[0], "Confirm Overwrite", "Would you like to over-write '"+file.getName()+"'?");
	        if (!ok) return;
	    }
	    
	    // Have valid name, so get image
	    ImageLoader loader = new ImageLoader();
	    
	    final IPasserelleEditor ed = (IPasserelleEditor)getWorkbenchPart();
	    final IFigure     workflow = ed.getWorkflowFigure();
	    
	    final Rectangle bounds = workflow.getBounds();
	    Image image = new Image(null, bounds.width + 6, bounds.height + 6);
		GC gc = new GC(image);
		SWTGraphics graphics = new SWTGraphics(gc); 
		graphics.translate(-bounds.x + 3, -bounds.y + 3);
		graphics.setForegroundColor(workflow.getForegroundColor());
		graphics.setBackgroundColor(workflow.getBackgroundColor());		
		workflow.paint(graphics);
		gc.dispose();
		
	    loader.data = new ImageData[]{image.getImageData()};
	    image.dispose();
		// Assert *.png at end of file name
	    if (! path.toLowerCase().endsWith(".png")) path = path + ".png";
	    // Save
	    loader.save(path, SWT.IMAGE_PNG);
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

}
