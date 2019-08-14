package de.jlo.talendcomp.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.junit.Test;

public class TestFileFilterConfig {

	@Test
	public void testFileFilterConfigWildcard() throws Exception {
		String dir = "/path/to/dir/";
		String filter = "*.properties";
		boolean ignoreMissing = true;
		FileFilterConfig c = new FileFilterConfig(dir + filter, ignoreMissing);
		System.out.println(c);
		assertEquals("Dir does not match", new File(dir), c.getDir());
		assertEquals("Filter does not match", filter, c.getFilterOrName());
		assertTrue("Filter type does not match", c.isWildcardFilter());
		assertTrue(c.getFileFilter().accept(new File(dir, "anything-else-file.properties")));
	}
	
	@Test
	public void testFileFilterConfigWithoutWildcard() throws Exception {
		String dir = "/Data/Talend/testdata/context/";
		String filter = "context_includes.properties";
		boolean ignoreMissing = true;
		FileFilterConfig c = new FileFilterConfig(dir + filter, ignoreMissing);
		System.out.println(c);
		assertEquals("Dir does not match", new File(dir), c.getDir());
		assertEquals("Filter does not match", filter, c.getFilterOrName());
		assertTrue("Filter type does not match", c.isWildcardFilter() == false);
		assertTrue(c.getFileFilter().accept(new File(dir, filter)));
		assertTrue(c.getFileFilter().accept(new File(dir, "anything-else-file")) == false);
		assertTrue(c.getFileFilter().accept(new File(dir, "anything-else-file.properties")) == false);
	}

	@Test
	public void testFileFilterConfigOnlyDir() throws Exception {
		String dir = "/path/to/dir/";
		boolean ignoreMissing = true;
		FileFilterConfig c = new FileFilterConfig(dir, ignoreMissing);
		System.out.println(c);
		assertEquals("Dir does not match", new File(dir), c.getDir());
		assertTrue("Filter does not match", c.getFilterOrName() == null);
		assertTrue("Filter type does not match", c.isWildcardFilter() == false);
	}

	@Test
	public void testFileFilterConfigOnlyDirRelative() throws Exception {
		String dir = "path/to/dir/";
		boolean ignoreMissing = true;
		FileFilterConfig c = new FileFilterConfig(dir, ignoreMissing);
		System.out.println(c);
		assertEquals("Dir does not match", new File(dir), c.getDir());
		assertTrue("Filter does not match", c.getFilterOrName() == null);
	}
	
	@Test
	public void testMatcher() {
		String name = "test-file.properties";
		String[] wildcards = new String[] {"*.properties"};
		assertTrue(FilenameUtils.wildcardMatch(name, wildcards[0], IOCase.INSENSITIVE));
		name = "xzy.txt";
		assertTrue(FilenameUtils.wildcardMatch(name, wildcards[0], IOCase.INSENSITIVE) == false);
	}

}
