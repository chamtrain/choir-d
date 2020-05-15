package edu.stanford.registry.server.utils;

import java.util.ArrayList;

//import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.test.FakeSiteInfo;

public class XMLFileUtilsTest {
  //private static Logger logger = Logger.getLogger(XMLFileUtils.class);

  static class MyXMLFileUtils extends XMLFileUtils {

    public MyXMLFileUtils(SiteInfo siteInfo) {
      super(siteInfo, "process.xml");
    }
    
  }
  @Test
  public void testInitialAndFollowupProcessesExist() {
    FakeSiteInfo si = new FakeSiteInfo();
    MyXMLFileUtils utils = new MyXMLFileUtils(si);
    
    ArrayList<String> processes = utils.getProcessNames();
    Assert.assertTrue(processes.contains("Initial"));
    Assert.assertTrue(processes.contains("FollowUp"));

    Assert.assertNotNull(utils.getAttributeDate("Initial", XMLFileUtils.ATTRIBUTE_EXPIRE_DT));
    Assert.assertNotNull(utils.getAttributeDate("FollowUp", XMLFileUtils.ATTRIBUTE_EXPIRE_DT));
    
    // Ensure there's an initial and a follow-up with a null end-date, for future appointments
    String initial = null;
    String followup = null;
    for (String name: processes) {
      if (name.startsWith("Initial")) {
        if (initial == null && null == utils.getAttributeDate(name, XMLFileUtils.ATTRIBUTE_EXPIRE_DT)) {
          initial = name;
        }
      } else if (name.startsWith("Follow")) {
        if (followup == null && null == utils.getAttributeDate(name, XMLFileUtils.ATTRIBUTE_EXPIRE_DT)) {
          followup = name;
        }
      }
    }
    Assert.assertNotNull("Expected to find at least one Initial process with no expiration date", initial);
    Assert.assertNotNull("Expected to find at least one FollowUp process with no expiration date", followup);
  }

}