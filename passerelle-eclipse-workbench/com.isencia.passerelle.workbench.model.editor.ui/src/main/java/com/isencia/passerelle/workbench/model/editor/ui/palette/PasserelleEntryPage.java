package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.util.Arrays;
import java.util.List;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.ui.palette.customize.DefaultEntryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasserelleEntryPage extends DefaultEntryPage {

	private static Logger logger = LoggerFactory.getLogger(PasserelleEntryPage.class);
	
	@Override
	public void createControl(Composite parent, PaletteEntry entry) {
		// TODO Auto-generated method stub
		super.createControl(parent, entry);
		Composite panel = (Composite) getControl();
		Control[] tablist = new Control[1];
		createLabel(panel, SWT.NONE, "Group");
		try {
			tablist[0] = createGroupText(panel, entry);
		} catch (Exception e) {
			logger.error("Cannot create group text", e);
		}

		panel.setTabList(tablist);
	}

	protected Combo createGroupText(Composite panel, PaletteEntry entry) throws Exception {
		PaletteContainer container = entry.getParent();
		Combo group = new Combo(panel, SWT.SINGLE);
		String[] favoriteGroupNames = PaletteBuilder.getInstance().getFavoriteGroupNames();
		group.setItems(favoriteGroupNames);
		String label = getEntry().getParent().getLabel();
		List groups = Arrays.asList(favoriteGroupNames);
		String current = entry.getParent().getLabel();
		if (groups.contains(current)) {
			group.select(groups.indexOf(label));
		}

		group.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleGroupChanged(((Combo) e.getSource()).getText());
			}
		});
		return group;
	}

	protected void handleGroupChanged(String text) {
		PaletteEntry cont = PaletteBuilder.getFavoriteGroup(text);
		if (cont instanceof PaletteContainer) {
			PaletteContainer cont2 = (PaletteContainer) cont;
			getEntry().setParent(cont2);
			cont2.add(getEntry());
		}

	}
}
