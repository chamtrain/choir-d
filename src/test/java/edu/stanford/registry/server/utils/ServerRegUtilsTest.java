package edu.stanford.registry.server.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

//import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.ProcessAttribute;
import edu.stanford.registry.test.FakeSiteInfo;

public class ServerRegUtilsTest {
  //private static Logger logger = Logger.getLogger(XMLFileUtils.class);

  static class MyXMLFileUtils extends XMLFileUtils implements Supplier<XMLFileUtils> {
    static FakeSiteInfo siteInfo = new FakeSiteInfo();
    public MyXMLFileUtils() {
      super(siteInfo, "process.xml");
    }
 
    @Override public XMLFileUtils get() {
      return this;
    }
    public SiteInfo getSiteInfo() {
      return siteInfo;
    }
  }

  ProcessAttribute getListItem(String name, ArrayList<ProcessAttribute> list) {
    for (ProcessAttribute item: list) {
      if (item.getName().equals(name))
        return item;
    }
    return null;
  }

  @Test
  public void testGoodPainMgmtRegCust() {
    MyXMLFileUtils xmlUtils = new MyXMLFileUtils();
    SurveyRegUtils srUtils = new SurveyRegUtils(xmlUtils.getSiteInfo(), xmlUtils);

    ArrayList<ProcessAttribute> list = srUtils.getVisitTypes();
    int num = list.size();
    Assert.assertTrue("Expect around 35", num > 30);
    
    ProcessAttribute item = getListItem("Initial", list);
    Assert.assertNotNull(item);
    Assert.assertNotNull(item.getEndDate());
    
    item = getListItem("FollowUp", list);
    Assert.assertNotNull(item);
    Assert.assertNotNull(item.getEndDate());
    
    Date d = new Date();
    String name = srUtils.getVisitType(0, d);
    Assert.assertTrue("Expecting starts wi Initial: "+name, name.startsWith("Initial"));
    
    name = srUtils.getVisitType(0, null);
    Assert.assertTrue("Expecting starts wi Initial: "+name, name.startsWith("Initial"));

    name = srUtils.getVisitType(1, d);
    Assert.assertTrue("Expecting starts wi FollowUp: "+name, name.startsWith("FollowUp"));

    name = srUtils.getVisitType(1, null);
    Assert.assertTrue("Expecting starts wi Initial: "+name, name.startsWith("FollowUp"));

  }

}