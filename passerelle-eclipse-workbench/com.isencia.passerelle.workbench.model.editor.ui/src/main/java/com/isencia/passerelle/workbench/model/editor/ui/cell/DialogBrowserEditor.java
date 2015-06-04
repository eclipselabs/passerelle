package com.isencia.passerelle.workbench.model.editor.ui.cell;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public abstract class DialogBrowserEditor extends TextCellEditor {

	private Button button;

	/**
	 * Listens for 'focusLost' events and  fires the 'apply' event as long
	 * as the focus wasn't lost because the dialog was opened.
	 */
	private FocusListener buttonFocusListener;

 
	public DialogBrowserEditor(Composite aComposite) {
		super(aComposite);
	}

	   /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected Control createControl(final Composite parent) {
    	
    	final Composite editor = new Composite(parent, getStyle());
        editor.setFont(parent.getFont());
        editor.setBackground(parent.getBackground());
        editor.setLayout(new DialogCellLayout());

        super.createControl(editor);
        text.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
        
        button = createButton(editor);
        button.setFont(editor.getFont());
        
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

        button.addKeyListener(new KeyAdapter() {
        	/* (non-Javadoc)
        	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
        	 */
        	public void keyReleased(KeyEvent e) {
        		if (e.character == '\u001b') { // Escape
        			fireCancelEditor();
        		}
        	}
        });

        button.addFocusListener(getButtonFocusListener());

        button.addSelectionListener(new SelectionAdapter() {
        	/* (non-Javadoc)
        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
        	 */
        	public void widgetSelected(SelectionEvent event) {
        		// Remove the button's focus listener since it's guaranteed
        		// to lose focus when the dialog opens
        		button.removeFocusListener(getButtonFocusListener());

        		Object newValue = openDialogBox(editor, doGetValue());

        		// Re-add the listener once the dialog closes
        		button.addFocusListener(getButtonFocusListener());

        		if (newValue != null) {
        			boolean newValidState = isCorrect(newValue);
        			if (newValidState) {
        				markDirty();
        				doSetValue(newValue);
        			} else {
        				// try to insert the current value into the error message.
        				setErrorMessage(MessageFormat.format(getErrorMessage(),
        						new Object[] { newValue.toString() }));
        			}
        			fireApplyEditorValue();
        		}
        	}
        });

        setValueValid(true);
        
        return editor;
    }

    /**
     * Creates the button for this cell editor under the given parent control.
     * <p>
     * The default implementation of this framework method creates the button 
     * display on the right hand side of the dialog cell editor. Subclasses
     * may extend or reimplement.
     * </p>
     *
     * @param parent the parent control
     * @return the new button control
     */
    protected Button createButton(Composite parent) {
        Button result = new Button(parent, SWT.DOWN);
        result.setText("..."); //$NON-NLS-1$
        return result;
    }

    /**
     * Return a listener for button focus.
     * @return FocusListener
     */
    private FocusListener getButtonFocusListener() {
    	if (buttonFocusListener == null) {
    		buttonFocusListener = new FocusListener() {

				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
				 */
				public void focusGained(FocusEvent e) {
					// Do nothing
				}

				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
				 */
				public void focusLost(FocusEvent e) {
					DialogBrowserEditor.this.focusLost();
				}
    		};
    	}
    	
    	return buttonFocusListener;
	}
    
    /**
     * Opens a dialog box under the given parent control and returns the
     * dialog's value when it closes, or <code>null</code> if the dialog
     * was canceled or no selection was made in the dialog.
     * <p>
     * This framework method must be implemented by concrete subclasses.
     * It is called when the user has pressed the button and the dialog
     * box must pop up.
     * </p>
     *
     * @param cellEditorWindow the parent control cell editor's window
     *   so that a subclass can adjust the dialog box accordingly
     * @return the selected value, or <code>null</code> if the dialog was 
     *   canceled or no selection was made in the dialog
     */
    protected abstract Object openDialogBox(Control cellEditorWindow, final Object value);
    
    
	
	protected void focusLost() {
		if (text.isFocusControl() || button.isFocusControl()) return;
		if (isActivated()) {
			fireApplyEditorValue();
			deactivate();
		}
	}

    /**
     * Internal class for laying out the dialog.
     */
    private class DialogCellLayout extends Layout {
        public void layout(Composite editor, boolean force) {
            Rectangle bounds = editor.getClientArea();
            Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
            if (text != null) {
            	text.setBounds(0, 0, bounds.width - size.x, bounds.height);
			}
            button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
        }

        public Point computeSize(Composite editor, int wHint, int hHint,
                boolean force) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
            Point contentsSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    force);
            Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    force);
            // Just return the button width to ensure the button is not clipped
            // if the label is long.
            // The label will just use whatever extra width there is
            Point result = new Point(buttonSize.x, Math.max(contentsSize.y,
                    buttonSize.y));
            return result;
        }
    }

}
