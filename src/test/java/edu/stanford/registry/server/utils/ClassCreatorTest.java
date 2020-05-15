package edu.stanford.registry.server.utils;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.test.FakeSiteInfo;

public class ClassCreatorTest {
  private static Logger logger = Logger.getLogger(ClassCreator.class);

  String me(String method) {
    return "ClassCreatorTest."+method;
  }
  
  String RC = "RegistryCustomizer";

  @Test
  public void testGoodPainMgmtRegCust() {
    ClassCreator<RegistryCustomizer> cc = new ClassCreator<RegistryCustomizer>("testGoodPainMgmtRegCust", RC, logger, SiteInfo.class);
    FakeSiteInfo si = new FakeSiteInfo();
    
    RegistryCustomizer reg = cc.createClass("edu.stanford.registry.server.PainManagementCustomizer", si);
    Assert.assertNotNull(reg);
  }
  
  @Test
  public void testGoodRegCustDflt() {
    ClassCreator<RegistryCustomizer> cc = new ClassCreator<RegistryCustomizer>("testGoodRegCustDflt", RC, logger, SiteInfo.class);
    FakeSiteInfo si = new FakeSiteInfo();
    
    RegistryCustomizer reg = cc.createClass("edu.stanford.registry.server.RegistryCustomizerDefault", si);
    Assert.assertNotNull(reg);
  }
  
  @Test
  public void testBadClassname() {
    ClassCreator<RegistryCustomizer> cc = new ClassCreator<RegistryCustomizer>("testBadClassname", RC, logger, SiteInfo.class);
    FakeSiteInfo si = new FakeSiteInfo();
    
    RegistryCustomizer reg = cc.createClass("edu.stanford.registry.server.RegistryCustomizerDefa", si);
    Assert.assertNull(reg);
  }
  
  @Test
  public void testClassWithoutConstructor() {
    ClassCreator<RegistryCustomizer> cc = new ClassCreator<RegistryCustomizer>("testClassWithoutConstructor", RC, logger, SiteInfo.class);
    FakeSiteInfo si = new FakeSiteInfo();
    
    RegistryCustomizer reg = cc.createClass("edu.stanford.registry.server.ProxyAuthFilter", si);
    Assert.assertNull(reg);
  }
  
}