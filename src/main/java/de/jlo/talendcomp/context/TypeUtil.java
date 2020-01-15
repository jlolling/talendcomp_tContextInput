package de.jlo.talendcomp.context;
/**
 * Copyright 2015 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class TypeUtil {
	
	private static final Map<String, DecimalFormat> numberformatMap = new HashMap<String, DecimalFormat>();
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String DEFAULT_LOCALE = "en_UK";
	
	public TypeUtil() {}
	
	public static DecimalFormat getNumberFormat(String localeStr) {
		DecimalFormat nf = numberformatMap.get(localeStr);
		if (nf == null) {
			Locale locale = new Locale(localeStr);
			nf = (DecimalFormat) NumberFormat.getInstance(locale);
			numberformatMap.put(localeStr, nf);
		}
		return nf;
	} 
	
	public static String toString(Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
			return sdf.format((Date) value);
		} else {
			return value.toString();
		}
	}
	
	/**
	 * concerts the string format into a Date
	 * @param dateString
	 * @param pattern
	 * @return the resulting Date
	 */
	public static Date convertToDate(Object value) throws Exception {
		if (value instanceof Date) {
			return (Date) value;
		} else if (value instanceof String) {
			String dateString = (String) value;
			if (dateString == null || dateString.isEmpty()) {
				return null;
			}
			try {
				String dateValue = dateString;
				String pattern = DEFAULT_DATE_PATTERN;
				int pos = dateString.indexOf(";");
				if (pos > 0) {
					pattern = dateString.substring(0, pos);
					if (pos < dateString.length() - 1) {
						dateValue = dateString.substring(pos + 1);
					}
				}
				Date date = null;
				try {
					date = GenericDateUtil.parseDate(dateValue, pattern);
				} catch (ParseException pe) {
					date = GenericDateUtil.parseDate(dateValue);
				}
				return date;
			} catch (Throwable t) {
				throw new Exception("Failed to convert string to date:" + t.getMessage(), t);
			}
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Date failed because the given class: " + value.getClass().getName() + " cannot be converted to Date");
		}
		return null;
	}
	
	public static Timestamp convertToTimestamp(Object dateString) throws Exception {
		Date date = convertToDate(dateString);
		if (date != null) {
			return new Timestamp(date.getTime());
		} else {
			return null;
		}
	}

	public static Boolean convertToBoolean(Object value) throws Exception {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			String valueStr = ((String) value).toLowerCase();
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			if ("true".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("false".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("1".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("0".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("yes".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("y".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("sí".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("да".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("no".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("нет".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("n".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("ja".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("j".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("nein".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("oui".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("non".equals(valueStr)) {
				return Boolean.FALSE;
			} else if ("ok".equals(valueStr)) {
				return Boolean.TRUE;
			} else if ("x".equals(valueStr)) {
				return Boolean.TRUE;
			} else if (valueStr != null) {
				throw new Exception("Value: " + value + " cannot be parsed to a boolean");
			}
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Boolean failed because the given class: " + value.getClass().getName() + " cannot be converted to Boolean");
		}
		return null;
	}

	public static Double convertToDouble(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(DEFAULT_LOCALE);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).doubleValue();
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Double failed because the given class: " + value.getClass().getName() + " cannot be converted to Double");
		}
		return null;
	}

	public static Integer convertToInteger(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(DEFAULT_LOCALE);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).intValue();
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Integer failed because the given class: " + value.getClass().getName() + " cannot be converted to Integer");
		}
		return null;
	}
	
	public static Short convertToShort(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			return Short.parseShort(valueStr);
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Short failed because the given class: " + value.getClass().getName() + " cannot be converted to Short");
		}
		return null;
	}

	public static String convertToString(Object value) throws Exception {
		if (value == null) {
			return null;
		} else if (value instanceof String) {
			String valueStr = (String) value;
			return valueStr.replace("\\n", "\n").replace("\\\"", "\"");
		} else {
			return String.valueOf(value);
		}
	}

	public static Float convertToFloat(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			return Float.parseFloat(valueStr);
		} else if (value != null) {
			// we got no Date or String, we cannot handle it!
			throw new Exception("convert object to Float failed because the given class: " + value.getClass().getName() + " cannot be converted to Float");
		}
		return null;
	}

	public static Long convertToLong(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			return Long.parseLong(valueStr);
		} else if (value != null) {
			throw new Exception("convert object to Long failed because the given class: " + value.getClass().getName() + " cannot be converted to Long");
		}
		return null;
	}

	public static BigDecimal convertToBigDecimal(Object value) throws Exception {
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			try {
				DecimalFormat decfrm = getNumberFormat(DEFAULT_LOCALE);
				decfrm.setParseBigDecimal(true);
				ParsePosition pp = new ParsePosition(0);
				return (BigDecimal) decfrm.parse(valueStr, pp);
			} catch (RuntimeException e) {
				throw new Exception("convertToBigDecimal:" + value + " failed:" + e.getMessage(), e);
			}
		} else if (value != null) {
			throw new Exception("convert object to BigDecimal failed because the given class: " + value.getClass().getName() + " cannot be converted to BigDecimal");
		}
		return null;
	}

	public static BigInteger convertToBigInteger(Object value) throws Exception {
		if (value instanceof BigInteger) {
			return (BigInteger) value;
		} else if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			try {
				return new BigInteger(valueStr);
			} catch (RuntimeException e) {
				throw new Exception("convertToBigInteger:" + value + " failed:" + e.getMessage(), e);
			}
		} else if (value != null) {
			throw new Exception("convert object to BigInteger failed because the given class: " + value.getClass().getName() + " cannot be converted to BigInteger");
		}
		return null;
	}

	public static double roundScale(double value, int scale) {
    	double d = Math.pow(10, scale);
        return Math.round(value * d) / d;
    }
 
}
