package edu.stanford.registry.server.security;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.test.PrivateAccessor;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PatientDaoTest {
  long yearInMillis = 365L * 24 * 60 * 60 * 1000;


  @SuppressWarnings("unchecked")
  @Test
  public void testDistributeAttrsToPatientsWithMultis() throws Exception {
    // and distribute the list of 5 attributes to the 6 copies of 2 patients
    Date dateJH = new Date(System.currentTimeMillis() - 60 * yearInMillis);
    Date dateMD = new Date(System.currentTimeMillis() - 46 * yearInMillis);
    Patient jh, md;  // the 2 patients

    // Simulate one patient wi 2 attributes and 3 appointments
    // and another wi 3 attributes and 3 appointments
    ArrayList<Patient> pats = new ArrayList<>();
    pats.add(jh = new Patient("John", "Henry", "jhenry30", dateJH));
    pats.add(     new Patient("John", "Henry", "jhenry30", dateJH));
    pats.add(     new Patient("Mary", "David", "mdavid29", dateMD));
    pats.add(     new Patient("John", "Henry", "jhenry30", dateJH));
    pats.add(     new Patient("Mary", "David", "mdavid29", dateMD));
    pats.add(md = new Patient("Mary", "David", "mdavid29", dateMD));

    // Easiest to create attributes by setting them on a patient
    ArrayList<PatientAttribute> attrs = new ArrayList<>(5);
    attrs.add(md.setAttribute("md1", "1md"));
    attrs.add(md.setAttribute("md2", "2md"));
    attrs.add(md.setAttribute("yes", "yes")); // both have this attribute
    attrs.add(jh.setAttribute("jh1", "1jh"));
    attrs.add(jh.setAttribute("yes", "yes"));

    for (int i = 0;  i < pats.size();  i++)
      pats.get(i).setAttribute("not", "not"); // so we can test this is cleared

    // Exercise the method dao.distributeAttrsToPatients(pats, attrs)
    PatientDao dao = new PatientDao(null, 1L);
    PrivateAccessor<PatientDao> acc = new PrivateAccessor<>(dao);

    Method method = acc.getMethod("makeHashOfPatients", pats.getClass());
    Object ob = method.invoke(dao, pats);
    HashMap<String, List<PatientAttribute>> idToLists =  null;
    if (ob instanceof HashMap<?,?>) {
      idToLists = (HashMap<String, List<PatientAttribute>>)ob;
    }
    acc.callMethod("distributeAttrsToPatients", idToLists, attrs);

    Assert.assertEquals(2, pats.get(0).getAttributes().size());
    Assert.assertEquals(2, pats.get(1).getAttributes().size());
    Assert.assertEquals(3, pats.get(2).getAttributes().size()); // md
    Assert.assertEquals(2, pats.get(3).getAttributes().size());
    Assert.assertEquals(3, pats.get(4).getAttributes().size()); // md
    Assert.assertEquals(3, pats.get(5).getAttributes().size()); // md

    for (int i = 0;  i < pats.size();  i++) {
      Assert.assertFalse(pats.get(i).hasAttribute("not"));
      Assert.assertTrue(pats.get(i).hasAttribute("yes"));
    }

    // Known side effect- all jh's share the same list
    Assert.assertNotEquals(pats.get(0), pats.get(1));  // patients are different
    Assert.assertEquals(pats.get(0).getAttributes(), pats.get(1).getAttributes()); // attribute lists are the same
    Assert.assertEquals(pats.get(0).getAttributes(), pats.get(3).getAttributes()); // attribute lists are the same

    // Of course, JHs and MDs have different lists
    Assert.assertNotEquals(pats.get(0).getAttributes(), pats.get(2).getAttributes()); // different patients
    Assert.assertNotEquals(pats.get(0).getAttributes(), pats.get(4).getAttributes()); // different patients
    Assert.assertNotEquals(pats.get(0).getAttributes(), pats.get(5).getAttributes()); // different patients

  }

}