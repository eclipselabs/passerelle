package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Color;

public class ColorRegistry {
	private static ColorRegistry colorFactory;

	private ColorRegistry() {

	}

	public static ColorRegistry getInstance() {
		if (colorFactory == null) {
			colorFactory = new ColorRegistry();
		}
		return colorFactory;
	}

	private Map<String, Color> colorMap = new HashMap<String, Color>();

	public Color getColor(String colorCode) {
		Color color = colorMap.get(colorCode);
		if (color == null) {
			if (colorCode == null) {
				return null;
			}
			color = newColor(colorCode);
			if (color == null) {
				return null;
			}
			colorMap.put(colorCode, color);
		}
		return color;
	}

	private Color newColor(String color) {
		if (color.contains("rgb(")) {
			String[] parts = color.split("\\(");
			if (parts.length > 1) {
				color = parts[1].split("\\)")[0];
			}
		}
		String[] colors = color.split(",");

		if (colors.length == 3) {
			try {
				return new Color(null, Integer.parseInt(colors[0]), Integer
						.parseInt(colors[1]), Integer.parseInt(colors[2]));
			} catch (Exception e) {

			}
		}
		return null;
	}
}
