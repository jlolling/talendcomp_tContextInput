package de.jlo.talendcomp.context;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextLoader {

	private Map<String, String> propertyFileMap = new HashMap<>();
	private Properties properties = new Properties();
	private List<FileFilterConfig> fileFilters = new ArrayList<>();
	private boolean enableIncludes = false;
	private Pattern includeKeyFilter = null;
	private boolean decryptPasswords = false;
	private List<ContextParameter> jobContextParameters = new ArrayList<>();
	
	public void addJobContextParameterValue(String key, String value, boolean isPrompt) {
		ContextParameter cp = new ContextParameter();
		cp.setName(key);
		// replace the original value with the value loaded from context files 
		cp.setValue(properties.getProperty(key, value));
		cp.setConfigured(true);
		cp.setPrompt(isPrompt);
		cp.setLoadFromFile(propertyFileMap.get(key));
		jobContextParameters.add(cp);
	}
	
	public void setupContextParameters() {
		// iterate through loaded parameters and add them to the job parameters if not exist in the list
		for (String propertyName : properties.stringPropertyNames()) {
			ContextParameter cp = new ContextParameter();
			cp.setName(propertyName);
			cp.setConfigured(false);
			cp.setValue(properties.getProperty(propertyName));
			cp.setLoadFromFile(propertyFileMap.get(propertyName));
			int index = jobContextParameters.indexOf(cp);
			if (index == -1) {
				jobContextParameters.add(cp); // add loaded none-job parameter
			}
		}
		Collections.sort(jobContextParameters);
	}
	
	public List<ContextParameter> getContextParameterValues() {
		return jobContextParameters;
	}
	
	public String getContextParamValue(String key) {
		for (ContextParameter p : jobContextParameters) {
			if (p.getName().equals(key)) {
				return p.getValue();
			}
		}
		return null;
	}
	
	/**
	 * add a filter expression containing a path and a wildcard filter
	 * @param pathFilter path and filter like /path/to/context*.properties
	 * @param ignoreMissing allow to find none files, otherwise it will raise an exception
	 */
	public void addFileFilter(String pathFilter, boolean ignoreMissing) {
		fileFilters.add(new FileFilterConfig(pathFilter, ignoreMissing));
	}
	
	public void loadProperties() throws Exception {
		for (FileFilterConfig config : fileFilters) {
			loadProperties(config);
		}
	}
	
	private void loadProperties(FileFilterConfig config) throws Exception {
		// get the files from the parent dir and filter them
		File dir = config.getDir();
		File[] files = dir.listFiles(config.getFileFilter());
		for (File file : files) {
			loadProperties(config, file);
		}
	}
	
	private void loadProperties(FileFilterConfig parentConfig, File file) throws Exception {
		Properties lp = new Properties();
		lp.load(new FileInputStream(file));
		// first load not includes
		for (String propertyName : lp.stringPropertyNames()) {
			if (checkIfKeyIsIncludeKey(propertyName) == false) {
				String value = lp.getProperty(propertyName);
				propertyFileMap.put(propertyName, file.getAbsolutePath());
				if (decryptPasswords && propertyName.toLowerCase().contains("password")) {
					value = TalendContextPasswordUtil.decryptPassword(value);
				}
				properties.setProperty(propertyName, value);
			}
		}
		if (enableIncludes) {
			for (String propertyName : lp.stringPropertyNames()) {
				if (checkIfKeyIsIncludeKey(propertyName)) {
					String value = lp.getProperty(propertyName);
					// the value should be a file
					File includeFile = null;
					// take we have an absolute reference
					if (FileFilterConfig.isAbsolute(value)) {
						includeFile = new File(value);
					} else {
						includeFile = new File(file.getParentFile(), value);
					}
					FileFilterConfig includeConfig = new FileFilterConfig(includeFile.getAbsolutePath(), parentConfig.isIgnoreMissing());
					loadProperties(includeConfig);
				}
			}
		}
	}
	
	public List<String> getVariableNames() {
		List<String> list = new ArrayList<>();
		for (Object key : properties.keySet()) {
			list.add((String) key);
		}
		return list;
	}

	public boolean isEnableIncludes() {
		return enableIncludes;
	}

	public void setEnableIncludes(boolean enableIncludes) {
		this.enableIncludes = enableIncludes;
	}
	
	private boolean checkIfKeyIsIncludeKey(String key) {
		if (enableIncludes) {
			if (includeKeyFilter != null) {
				Matcher m = includeKeyFilter.matcher(key);
				return m.find();
			}
		}
		return false;
	}

	public void setIncludeKeyFilter(String includeKeyFilter) {
		if (includeKeyFilter != null && includeKeyFilter.trim().isEmpty() == false) {
			this.includeKeyFilter = Pattern.compile(includeKeyFilter, Pattern.CASE_INSENSITIVE);
		}
	}
	
	public int countLoadedProperties() {
		return properties.size();
	}
	
	public String getValueAsString(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		if (missingAllowed == false && properties.containsKey(key) == false) {
			throw new Exception("Variable: " + key + " not not available in the loaded context variables but configured as mandatory!");
		}
		String value = properties.getProperty(key);
		if (value == null && nullAllowed == false) {
			throw new Exception("Variable: " + key + " is null but null is not allowed!");
		}
		return value;
	}
	
	public Integer getValueAsInteger(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Integer ret = null;
		try {
			ret = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Integer!", nfe);
		}
		return ret;
	}
	
	public Long getValueAsLong(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Long ret = null;
		try {
			ret = Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Long!", nfe);
		}
		return ret;
	}
	
	public Double getValueAsDouble(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Double ret = null;
		try {
			ret = Double.parseDouble(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Double!", nfe);
		}
		return ret;
	}

	public Float getValueAsFloat(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Float ret = null;
		try {
			ret = Float.parseFloat(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Float!", nfe);
		}
		return ret;
	}

	public Short getValueAsShort(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Short ret = null;
		try {
			ret = Short.parseShort(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Short!", nfe);
		}
		return ret;
	}

	public Boolean getValueAsBoolean(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Boolean ret = null;
		try {
			ret = Boolean.parseBoolean(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Boolean!", nfe);
		}
		return ret;
	}

	public Date getValueAsDate(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		Date ret = null;
		try {
			ret = GenericDateUtil.parseDate(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to Date!", nfe);
		}
		return ret;
	}

	public BigDecimal getValueAsBigDecimal(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		String value = getValueAsString(key, nullAllowed, missingAllowed);
		BigDecimal ret = null;
		try {
			ret = new BigDecimal(value);
		} catch (Exception nfe) {
			throw new Exception("Get variable: " + key + " failed while converting value: " + value + " to BigDecimal!", nfe);
		}
		return ret;
	}

	public boolean isDecryptPasswords() {
		return decryptPasswords;
	}

	public void setDecryptPasswords(boolean decryptPasswords) {
		this.decryptPasswords = decryptPasswords;
	}

}
