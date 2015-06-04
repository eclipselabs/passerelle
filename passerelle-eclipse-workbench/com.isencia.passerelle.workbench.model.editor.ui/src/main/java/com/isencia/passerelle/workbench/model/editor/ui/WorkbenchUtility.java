package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.swt.widgets.Control;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PaletteMouseListener;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.DiagramEditPart;

public abstract class WorkbenchUtility {
	public static void addMouseListenerToPaletteViewer(PaletteViewer paletteViewer) {
		Control control = paletteViewer.getControl();
		EditPart contents = paletteViewer.getContents();
		Set<DrawerEditPart> drawers = new HashSet<DrawerEditPart>();
		for (Object o : contents.getChildren()) {
			if (o instanceof DrawerEditPart
					&& !((DrawerEditPart) o).getDrawer().getLabel().equals(
							"Utilities")) {
				drawers.add((DrawerEditPart) o);
			}
		}
		for (DrawerEditPart drawer : drawers) {
			DrawerFigure drawerFigure = drawer.getDrawerFigure();
			drawerFigure.addMouseMotionListener(new PaletteMouseListener(
					drawer,paletteViewer));

		}
	}
	public static CompositeEntity getParentActor(Object o) {
		if (o instanceof DiagramEditPart
				&& ((DiagramEditPart) o).getCompositeActor() != null) {
			NamedObj container = ((DiagramEditPart) o).getCompositeActor()
					.getContainer();
			if (container != null) {
				return ((DiagramEditPart) o).getCompositeActor();
			}
		}
		if (o instanceof EditPart
				&& ((EditPart) o).getParent() instanceof DiagramEditPart) {
			CompositeActor actor = ((DiagramEditPart) ((EditPart) o)
					.getParent()).getCompositeActor();
			return (CompositeEntity) actor;
		}
		return null;
	}

	public static String getPath(NamedObj model) {
		StringBuffer sb = new StringBuffer();
		List<String> names = new ArrayList<String>();
		addModelToPath(model, names);
		if (names.size() > 1) {
			for (int i = 0; i < names.size() - 1; i++) {
				sb.append(names.get(i));
				sb.append(".");
			}
		}
		sb.append(model.getDisplayName());
		return sb.toString();
	}

	private static void addModelToPath(NamedObj model, List<String> names) {
		if (model.getContainer() != null) {
			names.add(model.getContainer().getDisplayName());
			addModelToPath(model.getContainer(), names);
		}

	}

	public static CompositeEntity containsCompositeEntity(List selectedObjects) {

		if (selectedObjects != null) {
			Iterator<Object> it = selectedObjects.iterator();
			while (it.hasNext()) {
				Object next = it.next();
				CompositeEntity compositeActor = getParentActor(next);
				if (compositeActor != null) {
					return compositeActor;
				}
			}
		}
		return null;
	}

}
