package de.jlo.talendcomp.context;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestContextLoad {

	@Test
	public void testContextLoadFilter() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/Data/Talend/testdata/context/*.properties", false);
		loader.loadProperties();
		assertEquals(25, loader.countLoadedProperties());
	}

	@Test
	public void testContextLoadDir() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/Data/Talend/testdata/context/", false);
		loader.loadProperties();
		for (String n : loader.getVariableNames()) {
			System.out.println(n);
		}
		assertEquals(25, loader.countLoadedProperties());
	}

	@Test
	public void testContextLoadWithIncludes() throws Exception {
		ContextLoader loader = new ContextLoader();
		loader.addFileFilter("/Data/Talend/testdata/context/context_includes.properties", false);
		loader.setEnableIncludes(true);
		loader.setIncludeKeyFilter("^file_");
		loader.loadProperties();
		for (String n : loader.getVariableNames()) {
			System.out.println(n);
		}
		assertEquals(17, loader.countLoadedProperties());
	}

}
