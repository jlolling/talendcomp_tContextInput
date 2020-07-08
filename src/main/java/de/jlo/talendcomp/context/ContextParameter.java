package de.jlo.talendcomp.context;

public class ContextParameter implements Comparable<ContextParameter> {
	
	private String name = null;
	private Object value = null;
	private String originalValue = null;
	private boolean isConfigured = false;
	private boolean isPrompt = false;
	private String sourceFile = null;
	private boolean isPassword = false;
	
	public ContextParameter(String name) {
		this.name = name;
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("The name of the context parameter cannot be null or empty!");
		}
	}
	
	public String getName() {
		return name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
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
	
	public boolean isPassword() {
		return isPassword;
	}
	
	public void setPassword(boolean isPassword) {
		this.isPassword = isPassword;
	}

}
