package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.draw2d.AbstractRouter;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.internal.routers.RectilinearRouter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.router.SCAManhattanConnectionRouter;

public class RouterFactory {

	public static final String ROUTER_PREF = "com.isencia.passerelle.workbench.model.editor.ui.RouterType";
	
	private static ROUTER_TYPE     type = ROUTER_TYPE.getDefaultType();
	private static CONNECTION_TYPE conn = CONNECTION_TYPE.STRAIGHT;
	
	public enum ROUTER_TYPE {
		
		SCAMANHATTAN(null) {
			public AbstractRouter getRouter(IFigure figure) {
				return new SCAManhattanConnectionRouter(figure);
			}
		},
		
		DIRECT(new BendpointConnectionRouter()), 
		
		RECTILINEAR(new RectilinearRouter());
		
		
		private AbstractRouter router;

		ROUTER_TYPE(AbstractRouter router) {
			this.router = router;
		}
		
		public AbstractRouter getRouter(IFigure figure) {
			return router;
		}
		
		public static ROUTER_TYPE getDefaultType() {
			
			int option = Activator.getDefault().getPreferenceStore().getInt(ROUTER_PREF);
			
			switch(option) {
			case 1:
				return SCAMANHATTAN;
			case 2:
				return DIRECT;
			case 3:
				return RECTILINEAR;
			default:
				return SCAMANHATTAN;
			}
		}
	}
	
	public enum CONNECTION_TYPE {
		STRAIGHT, 
		STRAIGHT_JUMPS, 
		CURVES, 
		CURVES_JUMPS;
	}

	
	public static AbstractRouter getRouter(IFigure topPane) {
		return type.getRouter(topPane);
	}
	public static ROUTER_TYPE getRouterType() {
		return type;
	}
	
	public static void setRouter(final ROUTER_TYPE rt) {
		// TODO Save preferred editor...
		type = rt;
		refreshEditors();
	}
	
	public static void setConnectionType(CONNECTION_TYPE type) {
		conn = type;
		refreshEditors();
	}
	
	private static void refreshEditors() {
		final IEditorReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
	    for (IEditorReference iEditorReference : refs) {
	    	final IEditorPart part = iEditorReference.getEditor(false);
	    	
	    	PasserelleModelEditor modEd = null;
	    	if (part instanceof PasserelleModelEditor) {
	    		modEd = (PasserelleModelEditor)part;
	    	} else if (part instanceof PasserelleModelMultiPageEditor) {
	    		modEd = ((PasserelleModelMultiPageEditor)part).getEditor();
	    	}
	    	if (modEd==null) continue;
	    	modEd.refresh();
		}
	}

	public static PolylineConnection getConnection() {
		
		if (conn == CONNECTION_TYPE.STRAIGHT) {
			return new PolylineConnection();
			
		} else if (conn == CONNECTION_TYPE.STRAIGHT_JUMPS) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_NONE);
			ex.setJumpLinks(true);
			return ex;
			
		} else if (conn == CONNECTION_TYPE.CURVES) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_LESS);
			ex.setJumpLinks(false);
			return ex;
			
		} else if (conn == CONNECTION_TYPE.CURVES_JUMPS) {
			PolylineConnectionEx ex = new PolylineConnectionEx();
			ex.setSmoothness(PolylineConnectionEx.SMOOTH_LESS);
			ex.setJumpLinks(true);
			return ex;
			
		}
		
		return new PolylineConnection();
	}
	
	public static void createConnectionActions(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(new Separator(ConnectionAction.class.getName()+"Group"));

		final MenuAction menu = new MenuAction("Connection types");
		menu.setId(ConnectionAction.class.getName()+"DropDown");
		if (actionBars.getToolBarManager().find(menu.getId())!=null) return;
		menu.setImageDescriptor(Activator.getImageDescriptor("icons/connection_menu.gif"));
		
		actionBars.getToolBarManager().add(menu);
		
		CheckableActionGroup group = new CheckableActionGroup();

		IAction action = new ConnectionAction(CONNECTION_TYPE.STRAIGHT);		
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Straight connections");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/connection_straight.gif"));
			action.setEnabled(true);
			action.setChecked(true);
			menu.add(action);
			group.add(action);
		}

		action = new ConnectionAction(CONNECTION_TYPE.STRAIGHT_JUMPS);		
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Straight connections with jumps");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/connection_straight_jumps.gif"));
			action.setEnabled(true);
			menu.add(action);
			group.add(action);
		}

		action = new ConnectionAction(CONNECTION_TYPE.CURVES);		
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Curved connections");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/connection_curved.gif"));
			action.setEnabled(true);
			menu.add(action);
			group.add(action);
		}
		
		action = new ConnectionAction(CONNECTION_TYPE.CURVES_JUMPS);		
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Curved connections with jumps");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/connection_curved_jumps.gif"));
			action.setEnabled(true);
			menu.add(action);
			group.add(action);
		}

	}
	
	
	public static void createRouterActions(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(new Separator(RouterAction.class.getName()+"Group"));
		
		CheckableActionGroup group = new CheckableActionGroup();
		
		IAction action = new RouterAction(ROUTER_TYPE.SCAMANHATTAN, 1);
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Manhattan routing with collision avoidance");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/router_manhattan.gif"));
			action.setEnabled(true);
			actionBars.getToolBarManager().add(action);
			group.add(action);
	    }
		
		action = new RouterAction(ROUTER_TYPE.DIRECT, 2);
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Direct routing");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/router_direct.gif"));
			action.setEnabled(true);
			actionBars.getToolBarManager().add(action);
			group.add(action);
		}
		
		action = new RouterAction(ROUTER_TYPE.RECTILINEAR, 3);
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			action.setText("Rectilinear router");
			action.setImageDescriptor(Activator.getImageDescriptor("icons/router_manhattan2.gif"));
			action.setEnabled(true);
			actionBars.getToolBarManager().add(action);
			group.add(action);
		}
	}
}
