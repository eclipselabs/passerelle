package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ImageRegistry {
	private static ImageRegistry imageFactory;
	private ImageRegistry() {
		
	}
	public static ImageRegistry getInstance(){
		if (imageFactory == null){
			imageFactory = new ImageRegistry();
		}
		return imageFactory;
	}
	private Map<ImageDescriptor,Image> imageMap =new HashMap<ImageDescriptor,Image>();
	public Image getImage(ImageDescriptor descriptor){
		Image image = imageMap.get(descriptor);
		if (image == null){
			if (descriptor == null){
				return null;
			}
			image =descriptor.createImage();
			imageMap.put(descriptor,image);
		}
		return image;
	}
}
