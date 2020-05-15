package edu.stanford.registry.server.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import edu.stanford.registry.test.FakeSiteInfo;

@RunWith(MockitoJUnitRunner.class)
public class SiteInfoTest {

  AppConfig appConfig = new AppConfig();

  @Test
  public void testNothing() {
    Assert.assertTrue(true);
  }

  final static String HELLO = "hello";
  final static String THERE = "there";
  final static String GLOBAL = "global";
  final static String LOCAL = "local";
  final static String DEFAULT = "default";
  
  @Test
  public void testGetPathPropertyWithSite() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    siteInfo.addLocalProps(HELLO, "{site}"+THERE);
    String exp = siteInfo.getSiteName()+THERE;
    String got = siteInfo.getPathProperty(HELLO, DEFAULT);
    Assert.assertEquals(exp, got);
  }

  @Test
  public void testGetPathPropertyWithBadSite() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    siteInfo.addLocalProps(HELLO, "{site"+THERE);
    String exp = siteInfo.getSiteName()+THERE;
    String got = siteInfo.getPathProperty(HELLO, DEFAULT);
    Assert.assertNotEquals(exp, got);
  }

  @Test
  public void testGetPathPropertyReplacesSiteInDefault() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    String dflt = THERE+"{site}";
    String exp = THERE+siteInfo.getSiteName();
    String got = siteInfo.getPathProperty(HELLO, dflt);
    Assert.assertEquals(exp, got);
    Assert.assertNotEquals(exp, dflt);
  }

  @Test
  public void testGlobalComesThrough() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    siteInfo.addGlobalProps(HELLO, GLOBAL);
    String got = siteInfo.getProperty(HELLO, DEFAULT);
    Assert.assertEquals(GLOBAL, got);
  }

  @Test
  public void testLocalOverrides() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    siteInfo.addLocalProps(HELLO, LOCAL);
    siteInfo.addGlobalProps(HELLO, GLOBAL);
    String got = siteInfo.getProperty(HELLO, DEFAULT);
    Assert.assertEquals(LOCAL, got);
  }

  @Test
  public void testDefaultReturned() {
    FakeSiteInfo siteInfo = new FakeSiteInfo();
    String got = siteInfo.getProperty(HELLO, GLOBAL);
    Assert.assertEquals(GLOBAL, got);
  }
}
