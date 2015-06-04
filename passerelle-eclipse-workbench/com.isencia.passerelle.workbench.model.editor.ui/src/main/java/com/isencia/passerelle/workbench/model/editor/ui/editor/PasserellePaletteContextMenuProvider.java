package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.ChangeIconSizeAction;
import org.eclipse.gef.ui.palette.CustomizeAction;
import org.eclipse.gef.ui.palette.LayoutAction;
import org.eclipse.gef.ui.palette.PaletteContextMenuProvider;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PinDrawerAction;
import org.eclipse.gef.ui.palette.SettingsAction;
import org.eclipse.gef.ui.palette.editparts.IPinnableEditPart;
import org.eclipse.jface.action.IMenuManager;

public class PasserellePaletteContextMenuProvider extends
		PaletteContextMenuProvider {

	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);

		EditPart selectedPart = (EditPart) getPaletteViewer()
				.getSelectedEditParts().get(0);
		IPinnableEditPart pinnablePart = (IPinnableEditPart) selectedPart
				.getAdapter(IPinnableEditPart.class);
		if (pinnablePart != null && pinnablePart.canBePinned()) {
			menu.appendToGroup(GEFActionConstants.MB_ADDITIONS,
					new PinDrawerAction(pinnablePart));
		}
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new LayoutAction(
				getPaletteViewer().getPaletteViewerPreferences()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, new LayoutAction(
				getPaletteViewer().getPaletteViewerPreferences()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW,
				new ChangeIconSizeAction(getPaletteViewer()
						.getPaletteViewerPreferences()));
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW,
				new ChangeIconSizeAction(getPaletteViewer()
						.getPaletteViewerPreferences()));
//		if (selectedPart instanceof ToolEntryEditPart){
//			menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
//					new MoveUpAction((ToolEntryEditPart)selectedPart,editor));
//			menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
//					new MoveDownAction((ToolEntryEditPart)selectedPart,editor));
//		}
		if (getPaletteViewer().getCustomizer() != null) {
			menu.appendToGroup(GEFActionConstants.GROUP_REST,
					new CustomizeAction(getPaletteViewer()));
		}
		menu.appendToGroup(GEFActionConstants.GROUP_REST, new SettingsAction(
				getPaletteViewer()));
	}
	private PasserelleModelMultiPageEditor editor;
	public PasserellePaletteContextMenuProvider(PaletteViewer palette,PasserelleModelMultiPageEditor editor) {
		super(palette);
		this.editor = editor;
		// TODO Auto-generated constructor stub
	}

}
