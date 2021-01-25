package de.jlo.talendcomp.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestContextLoad {

	@Test
	public void testContextLoadFilter() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/var/testdata/context/*.properties", false);
		loader.loadProperties();
		assertEquals(28, loader.countLoadedProperties());
	}

	@Test
	public void testContextCheckMissingFiles() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/var/testdata/context/missing.properties", false);
		try {
			loader.loadProperties();
			assertTrue("missing file not detected", false);
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testContextLoadReplace() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addValueReplacement("{{ph1}}", "ph1-value");
		loader.addValueReplacement("{{ph2}}", "ph2-value");
		loader.addFileFilter("/var/testdata/context/placeholders.properties", false);
		loader.loadProperties();
		String expected = "value ph1-value ph2-value";
		String actual = loader.getValueAsString("key1", false, false);
		assertEquals("Value wrong", expected, actual);
	}

	@Test
	public void testContextLoadDir() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/var/testdata/context/", false);
		loader.loadProperties();
		for (String n : loader.getVariableNames()) {
			System.out.println(n);
		}
		assertEquals(31, loader.countLoadedProperties());
	}

	@Test
	public void testContextLoadWithIncludes() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/var/testdata/context/context_includes.properties", false);
		loader.setEnableIncludes(true);
		loader.setIncludeKeyFilter("^file_");
		loader.loadProperties();
		for (String n : loader.getVariableNames()) {
			System.out.println(n);
		}
		assertEquals(17, loader.countLoadedProperties());
	}

	@Test
	public void testContextLoadWithIncludesAndAdditionalVars() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/var/testdata/context/context_includes.properties", false);
		loader.setEnableIncludes(true);
		loader.setIncludeKeyFilter("^file_");
		loader.loadProperties();
		for (String n : loader.getVariableNames()) {
			System.out.println(n);
		}
		loader.addJobContextParameterValue("build_in_1", new Object(), false);
		loader.setupContextParameters();
		assertEquals(17, loader.countLoadedProperties());
		assertEquals(18, loader.countJobContextVars());
	}

}
