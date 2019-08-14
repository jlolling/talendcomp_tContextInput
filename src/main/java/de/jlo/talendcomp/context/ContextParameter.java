package de.jlo.talendcomp.context;

public class ContextParameter implements Comparable<ContextParameter> {
	
	private String name = null;
	private String value = null;
	private boolean isConfigured = false;
	private boolean isPrompt = false;
	private String sourceFile = null;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isConfigured() {
		return isConfigured;
	}
	public void setConfigured(boolean isConfigured) {
		this.isConfigured = isConfigured;
	}
	public boolean isPrompt() {
		return isPrompt;
	}
	public void setPrompt(boolean isPrompt) {
		this.isPrompt = isPrompt;
	}
	public String getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(String loadFromFile) {
		this.sourceFile = loadFromFile;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ContextParameter) {
			return ((ContextParameter) o).getName().equals(name);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public int compareTo(ContextParameter p) {
		return name.toLowerCase().compareTo(p.name.toLowerCase());
	}

}
