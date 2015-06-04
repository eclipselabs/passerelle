package com.isencia.passerelle.workbench.model.ui;

public class GeneralAttribute {
	public GeneralAttribute(ATTRIBUTE_TYPE type, String value) {
		super();
		this.type = type;
		this.value = value;
	}
	public static enum ATTRIBUTE_TYPE {
		NAME , CLASS , TYPE;
	}
	private ATTRIBUTE_TYPE type;
	public ATTRIBUTE_TYPE getType() {
		return type;
	}
	public void setType(ATTRIBUTE_TYPE type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	private String value;
}
