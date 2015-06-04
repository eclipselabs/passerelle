package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;

import com.isencia.passerelle.util.ptolemy.FileParameter;
import com.isencia.passerelle.util.ptolemy.IAvailableChoices;
import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CellEditorAttribute;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
import com.isencia.passerelle.workbench.util.ListUtils;

public class VariableLabelProvider extends ColumnLabelProvider {

	private final ActorAttributesTableViewer actorAttributesTableViewer;
	private final Font  italicFont,  boldFont;
	private final Image folderImage;

	public VariableLabelProvider(ActorAttributesTableViewer actorAttributesView) {
		this.actorAttributesTableViewer = actorAttributesView;
		
		final FontData shellFd = actorAttributesView.getControl().getShell().getFont().getFontData()[0];
		FontData fd      = new FontData(shellFd.getName(), shellFd.getHeight(), SWT.ITALIC);
		italicFont = new Font(null, fd);
		fd      = new FontData(shellFd.getName(), shellFd.getHeight(), SWT.BOLD);
		boldFont = new Font(null, fd);
		
		folderImage = Activator.getImageDescriptor("icons/folder.png").createImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {

		if (element instanceof GeneralAttribute) {
			return ((GeneralAttribute)element).getValue();
		}
		if (element instanceof CellEditorAttribute) {
			final String text = ((CellEditorAttribute) element)
					.getRendererText();
			if (text != null)
				return text;
		}

		final Attribute attr = (Attribute) element;
		if (attr instanceof Variable) {
			Variable param = (Variable) attr;
			try {
				if (param.getToken() != null
						&& param.getToken() instanceof BooleanToken) {
					return "";
				}
			} catch (Exception ignored) {
				// There is another exception which will show if this happens.
			}

			String label = element == null ? "" : param.getExpression();

			if (param instanceof StringChoiceParameter) {
				final IAvailableChoices choice = ((StringChoiceParameter) param)
						.getAvailableChoices();
				final Map<String, String> vis = choice.getVisibleChoices();
				if (vis != null) {
					final StringBuilder buf = new StringBuilder();
					final List<String> vals = ListUtils.getList(label);
					if (vals!=null) for (int i = 0; i < vals.size(); i++) {
						buf.append(vis.get(vals.get(i)));
						if (i < vals.size() - 1) {
							buf.append(", ");
						}
					}
					label = buf.toString();
				}
			} else {
	      List txtStyleAttrs = attr.attributeList(TextStyle.class);
	      if(txtStyleAttrs!=null && !txtStyleAttrs.isEmpty()) {
	        label =  label.replace("\n", ", ");
	      }
			}
			return label;
		}
		if (attr instanceof StringAttribute) {
		  String expr = ((StringAttribute)attr).getExpression();
		  List txtStyleAttrs = attr.attributeList(TextStyle.class);
		  if(txtStyleAttrs!=null && !txtStyleAttrs.isEmpty()) {
		    return expr.replace("\n", ", ");
		  } else {
		    return expr;
		  }
		}
		return "";

	}

	private Image ticked, unticked;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {

		if (element instanceof String)
			return null;

		if (element instanceof Variable) {
			final Variable param = (Variable) element;
			try {
				if (param.getToken() != null
						&& param.getToken() instanceof BooleanToken) {
					if (((BooleanToken) param.getToken()).booleanValue()) {
						if (ticked == null)
							ticked = Activator.getImageDescriptor("icons/ticked.png").createImage();
						return ticked;
					} else {
						if (unticked == null)
							unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
						return unticked;

					}
				}
			} catch (Exception ignored) {
				// There is another exception which will show if this happens.
			}
		}
		
		if (element instanceof ResourceParameter) {
			return folderImage; // Might use slightly different icon in future
		}
		if (element instanceof FileParameter) {
			return folderImage;
		}
		
		return null;
	}
	
	public void update(ViewerCell cell) {
		
		super.update(cell);
		
		// May possibly add more customization here

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		final boolean canEdit = actorAttributesTableViewer.canEditAttribute(element);
		if (canEdit) {
			if (element instanceof GeneralAttribute) return boldFont; // It is probably name
			return null;
		}
		return italicFont;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		final boolean canEdit = actorAttributesTableViewer.canEditAttribute(element);
		if (canEdit) return null;
		return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	}

	private final Color lightGrey = new Color(null, 240,240,240);

	public Color getBackground(Object element) {
		if (element instanceof GeneralAttribute) return lightGrey;
		return null;
	}

	public void dispose() {
		super.dispose();
        this.italicFont.dispose();
        folderImage.dispose();
        boldFont.dispose();
        if (ticked!=null)   ticked.dispose();
        if (unticked!=null) unticked.dispose();
		lightGrey.dispose();
}

}
