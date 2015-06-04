

package com.isencia.passerelle.workbench.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {
	
	/**
	 * Centre shell in another.
	 * @param parent
	 * @param shell
	 */
	public static void centerDialog(Shell parent, Shell shell){
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = shell.getBounds();


		int locationX, locationY;
		locationX = (parentSize.width - mySize.width)/2+parentSize.x;
		locationY = (parentSize.height - mySize.height)/2+parentSize.y;


		shell.setLocation(new Point(locationX, locationY));
	}
}
