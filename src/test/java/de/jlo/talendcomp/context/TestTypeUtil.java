package de.jlo.talendcomp.context;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class TestTypeUtil {
	
	@Test
	public void testConvertToDate() throws Exception {
		String dateStr = "2019-02-23 23:44:01.123";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date expected = sdf.parse(dateStr);
		Date actual = TypeUtil.convertToDate(dateStr);
		assertEquals(expected, actual);
		dateStr = "dd.MM.yyyy HH:mm:ss.SSS;23.02.2019 23:44:01.123";
		actual = TypeUtil.convertToDate(dateStr);
		assertEquals(expected, actual);
	}

}
