package net.wasdev.wlp.maven.test.app;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 
 * Web application test case
 * 
 */

public class CopyDependenciesTest {

    @Test
    public void testCopyDependenciesFilesExist() throws Exception {
    	
        // Check that the derby dependency was copied to the correct location
        File f = new File("liberty/usr/shared/resources/derby-10.15.2.0.jar");
        Assert.assertTrue(f.getCanonicalFile() + " doesn't exist", f.exists());
   }
}
