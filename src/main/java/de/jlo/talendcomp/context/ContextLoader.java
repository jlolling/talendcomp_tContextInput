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
	private Properties propertiesFromFiles = new Properties();
	private Properties propertiesFromFilesWithReplacements = new Properties();
	private List<FileFilterConfig> fileFilters = new ArrayList<>();
	private boolean enableIncludes = false;
	private Pattern includeKeyFilter = null;
	private boolean decryptPasswords = false;
	private List<ContextParameter> jobContextParameters = new ArrayList<>();
	private static boolean already_loaded = false;
	private Map<String, String> valuePlaceholders = new HashMap<>();
	private boolean forceLoading = false;
	
	public static void preventFurtherJobsFromLoading() {
		already_loaded = true;
	}
	
	public void addValueReplacement(String placeHolder, String replacement) {
		if (placeHolder != null) {
			if (replacement == null) {
				replacement = "";
			}
			valuePlaceholders.put(placeHolder, replacement);
		}
	}
	
	public boolean contextLoadAlreadyDone() {
		return already_loaded && forceLoading == false;
	}
	
	public String applyValueReplacements(String variableName, String strValue) {
		if (strValue != null) {
			boolean changed = false;
			for (Map.Entry<String, String> entry : valuePlaceholders.entrySet()) {
				if (strValue.contains(entry.getKey())) {
					changed = true;
				}
				strValue = strValue.replace(entry.getKey(), entry.getValue());
			}
			if (changed) {
				propertiesFromFilesWithReplacements.put(variableName, strValue);
			}
			return strValue;
		} else {
			return null;
		}
	}
	
	public ContextParameter addJobContextParameterValue(String key, Object value, boolean isPrompt) {
		if (key == null || key.trim().isEmpty()) {
			throw new IllegalArgumentException("Adding context variables to the internal list failed: The key of the variable cannot be null or empty. The variable value was: " + value);
		}
		key = key.trim();
		ContextParameter cp = new ContextParameter(key);
		if (propertiesFromFiles.getProperty(key) != null) {
			cp.setValue(propertiesFromFiles.getProperty(key));
		} else {
			if (value instanceof String) {
				cp.setValue(applyValueReplacements(key, (String) value)); 
			} else {
				cp.setValue(value);
			}
		}
		cp.setConfigured(true);
		cp.setPrompt(isPrompt);
		cp.setSourceFile(propertyFileMap.get(key));
		jobContextParameters.add(cp);
		return cp;
	}
	
	public boolean isPropertyValueLoadedFromFile(String key) {
		if (propertiesFromFiles.getProperty(key) != null) {
			return true;
		}
		return false;
	}
	
	public boolean containsPropertyValueReplacedPlaceholders(String key) {
		if (propertiesFromFilesWithReplacements.getProperty(key) != null) {
			return true;
		}
		return false;
	}

	public void setupContextParameters() {
		// iterate through loaded parameters and add them to the job parameters if they not exist in the list
		for (String propertyName : propertiesFromFiles.stringPropertyNames()) {
			if (propertyName == null || propertyName.trim().isEmpty()) {
				continue;
			}
			ContextParameter cp = new ContextParameter(propertyName);
			cp.setConfigured(false);
			cp.setValue(propertiesFromFiles.getProperty(propertyName));
			cp.setSourceFile(propertyFileMap.get(propertyName));
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
	
	public Object getContextParamValue(String key) {
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
		if (pathFilter != null && pathFilter.trim().isEmpty() == false) {
			fileFilters.add(new FileFilterConfig(pathFilter, ignoreMissing));
		}
	}
	
	public boolean loadProperties() throws Exception {
		if (contextLoadAlreadyDone() == false) {
			boolean loaded = false;
			for (FileFilterConfig config : fileFilters) {
				if (loadProperties(config)) {
					loaded = true;
				}
			}
			return loaded;
		} else {
			return false;
		}
	}
	
	public void replacePlaceHoldersInFileFilters() {
		for (FileFilterConfig config : fileFilters) {
			String pathFilter = config.getFilterOrName();
			boolean changed = false;
			for (Map.Entry<String, String> entry : valuePlaceholders.entrySet()) {
				if (pathFilter.contains(entry.getKey())) {
					changed = true;
				}
				pathFilter = pathFilter.replace(entry.getKey(), entry.getValue());
			}
			if (changed) {
				config.setFilterOrName(pathFilter);
			}
		}
	}
	
	private boolean loadProperties(FileFilterConfig config) throws Exception {
		boolean loaded = false;
		// get the files from the parent dir and filter them
		File dir = config.getDir();
		File[] files = dir.listFiles(config.getFileFilter());
		if (files != null) {
			if (config.isIgnoreMissing() == false && files.length == 0) {
				throw new Exception("File filter: " + config + " does not find files and is configured as do not ignore missing files.");
			}
			for (File file : files) {
				loadProperties(config, file);
				loaded = true;
			}
		} else {
			if (config.isIgnoreMissing() == false) {
				throw new Exception("File filter: " + config + " does not find files and is configured as do not ignore missing files.");
			}
		}
		return loaded;
	}
	
	private void loadProperties(FileFilterConfig parentConfig, File file) throws Exception {
		Properties lp = new Properties();
		lp.load(new FileInputStream(file));
		// first load not includes
		for (String propertyName : lp.stringPropertyNames()) {
			if (propertyName == null || propertyName.trim().isEmpty()) {
				continue;
			}
			if (checkIfKeyIsIncludeKey(propertyName) == false) {
				String value = lp.getProperty(propertyName);
				propertyFileMap.put(propertyName, file.getAbsolutePath());
				if (decryptPasswords && propertyName.toLowerCase().contains("passwor")) {
					value = TalendContextPasswordUtil.decryptPassword(value);
				}
				propertiesFromFiles.setProperty(propertyName, applyValueReplacements(propertyName, value));
			}
		}
		if (enableIncludes) {
			for (String propertyName : lp.stringPropertyNames()) {
				if (propertyName == null || propertyName.trim().isEmpty()) {
					continue;
				}
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
		for (Object key : propertiesFromFiles.keySet()) {
			list.add((String) key);
		}
		return list;
	}

	public boolean isEnableIncludes() {
		return enableIncludes;
	}

	public void setEnableIncludes(Boolean enableIncludes) {
		if (enableIncludes != null) {
			this.enableIncludes = enableIncludes;
		}
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
		return propertiesFromFiles.size();
	}
	
	public int countJobContextVars() {
		return jobContextParameters.size();
	}
	
	public String getValueAsString(String key, boolean nullAllowed, boolean missingAllowed) throws Exception {
		if (missingAllowed == false && propertiesFromFiles.containsKey(key) == false) {
			throw new Exception("Variable: " + key + " not not available in the loaded context variables but configured as mandatory!");
		}
		String value = propertiesFromFiles.getProperty(key);
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

	public void setDecryptPasswords(Boolean decryptPasswords) {
		if (decryptPasswords != null) {
			this.decryptPasswords = decryptPasswords;
		}
	}

	public boolean isForceLoading() {
		return forceLoading;
	}

	public void setForceLoading(Boolean forceLoading) {
		if (forceLoading != null) {
			this.forceLoading = forceLoading;
		}
	}

}
