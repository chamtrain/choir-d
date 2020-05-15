/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.stanford.registry.server.randomset;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetCategory;
import edu.stanford.registry.shared.RandomSetGroup;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

//import org.apache.log4j.Logger;

//import edu.stanford.registry.shared.TreatmentSet;

/**
 * RandomSetter contains the methods used to manipulate a RandomSet -
 * convert it to/from a json, add/withdraw a patient, update it if
 * the version in the database changed (if there are multiple servers).
 * Each randomization algorithm will create a subclass of this.
 *
 * @author rstr
 */
abstract public class RandomSetter {
  private static final Logger logger = LoggerFactory.getLogger(RandomSetter.class);

  // Can't store a SiteInfo because this is cached
  protected final Long siteId;
  protected final RandomSet rset;  // the data object, shared with the UI
  protected final RandomSetDao.Supplier rsetDaoSupplier;
  private final PatientDao.Supplier patDaoSupplier;


  /**
   * For testing- Can't pass in SiteInfo, due to an initialization cycle with tests.
   * @param name site-unique name
   */
  public RandomSetter(Long siteId, String name, String algorithm) {
    this(siteId, name, algorithm, new RandomSetDao.Supplier(), new PatientDao.Supplier());
  }

  /**
   * For testing- Can't pass in SiteInfo, due to an initialization cycle with tests.
   * @param name site-unique name
   */
  public RandomSetter(Long siteId, String name, String algorithm,
                      RandomSetDao.Supplier daoSupplier, PatientDao.Supplier patDaoSupplier) {
    this.siteId = siteId;
    rset = new RandomSet(siteId, name, algorithm);
    this.rsetDaoSupplier = daoSupplier;
    this.patDaoSupplier = patDaoSupplier;
  }

  /**
   * Initializes a RandomSet from its JSON definition
   */
  public RandomSetter init(JSONObject json) {
    rset.init(json.getString("title"),  RandomSet.RSType.valueOf(json.getString("type")),
              json.getString("desc"),   json.getString("user"),
              json.optInt("pop"), optionalDate(json, "enddt"), json.optInt("trialdays"));
    JSONArray jgroups = json.getJSONArray("groups");
    int ng = (jgroups == null) ? 0 : jgroups.length();
    if (ng < 2) {
      throw new RuntimeException(ng+" groups were defined, need at least 2");
    }
    ArrayList<RandomSetGroup> groups = new ArrayList<>(ng);
    for (int i = 0;  i < jgroups.length();  i++) {
      JSONObject j = jgroups.getJSONObject(i);
      groups.add(new RandomSetGroup(i, j.getString("name"), j.getString("desc"), j.getInt("pct"),
                                 j.getBoolean("sub"), j.getInt("max"), j.getBoolean("closeOnMax")));
    }
    rset.initGroups(groups);
    rset.setCategories(getCategories(rset.getName(), json));
    return this;
  }


  /**
   * Create the category array from the JSON object's "categories" array field.
   */
  private RandomSetCategory[] getCategories(String rsetName, JSONObject json) {
    JSONArray jcats;
    try {
      jcats = json.getJSONArray("categories");
    } catch (JSONException e) {
      return null;
    }

    int i = -1, j = -1;
    try {
      RandomSetCategory cats[] = new RandomSetCategory[jcats.length()];
      for (i = 0;  i < jcats.length();  i++) {
        JSONObject jcat = jcats.getJSONObject(i);
        RandomSetCategory cat = new RandomSetCategory(jcat.getString("name"), jcat.getString("title"),
                                                jcat.getString("question"));
        cats[i] = cat;
        JSONArray jvals = jcat.getJSONArray("values");
        RandomSetCategory.Value vals[] = new RandomSetCategory.Value[jvals.length()];
        for (j = 0;  j < jvals.length();  j++) {
          JSONObject jval = jvals.getJSONObject(j);
          RandomSetCategory.Value val = new RandomSetCategory.Value(jval.getString("name"), jval.getString("title"),
                                                              jval.getString("answer"));
          vals[j] = val;
        }
        cat.setValues(vals);
      }
      return cats;
    } catch (Throwable e) {
      logger.error("Error reading RandomSet "+rsetName+" category "+i+", value "+j);
      throw e;
    }
  }

  private Date optionalDate(JSONObject json, String key) {
    String value = null;
    try {
      value = json.getString(key);
      SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
      return format.parse(value);
    } catch (JSONException e) {
      return null;
    } catch(Throwable e) { // bad date format
      logger.warn("Could not read JSONObject date for RandomSet name, ."+key+"="+value);
      return null;
    }
  }

  public RandomSet getRandomSet() {
    return rset;
  }


  /**
   * Use this to get a JSon string to externalize and put into a config value.
   *
   * To store
   */
  public String toJsonString() {
    return toJson().toString();
  }


  /**
   * Creates a JSON out of a RandomSet and the groups it contains
   */
  private JSONObject toJson() {
    JSONObject jset = new JSONObject();
    jset.put("name",  rset.getName());
    jset.put("alg",   rset.getAlgorithm());
    jset.put("title", rset.getTitle());
    jset.put("type",  rset.getType().toString());
    jset.put("desc",  rset.getDescription());
    jset.put("user",  rset.getUsername());
    jset.put("pop",   rset.getTargetPopulation());
    jset.put("state", rset.getState().toString());

    for (RandomSetGroup group: rset.getGroups()) {
      jset.accumulate("groups", makeJsonGroup(group));
    }

    if (rset.getCategories() != null) {
      for (RandomSetCategory cat: rset.getCategories()) {
        jset.append("categories", makeJsonCategory(cat));
      }
    }

    return jset;
  }


  /**
   * Creates a JSON of one groups definition- not the counts or other state.
   */
  private JSONObject makeJsonGroup(RandomSetGroup g) {
    JSONObject j = new JSONObject();
    j.put("name", g.getGroupName());
    j.put("desc", g.getDescription());
    j.put("pct",  g.getTargetPerBlock());
    j.put("sub",  g.shouldSubtractWithdrawals());
    j.put("max",  g.getMaxSize());
    j.put("closeOnMax", g.shouldCloseOnMax());
    return j;
  }

  private JSONObject makeJsonCategory(RandomSetCategory cat) {
    JSONObject j = new JSONObject();
    j.put("name", cat.getName()).put("title", cat.getTitle()).put("question", cat.getQuestion());
    for (RandomSetCategory.Value val: cat.getValues()) {
      j.accumulate("values", makeJsonCatValue(val));
    }
    return j;
  }

  private JSONObject makeJsonCatValue(RandomSetCategory.Value val) {
    JSONObject j = new JSONObject();
    return j.put("name", val.getName()).put("title", val.getTitle()).put("answer", val.getAnswer());
  }


  /**
   * Update a patient to the RandomSet, assigning him/her to a random group or the declined group as state dictates
   */
   public RandomSetParticipant updateParticipant(SiteInfo siteInfo, Supplier<Database> dbp, User user, RandomSetParticipant rsp) {
     RandomSet rset = rsp.getRandomSet();
     if (!rsp.changed()) {
       logger.error("Site#"+siteId+". Wont add/chg participant, no changes, state="+rsp.getState()+
                    " reason="+rsp.getReason());
       return null;
     }
     if (rset == null) {
       logger.error("Site#"+siteId+". Can not add/chg participant, no random set named: "+rsp.getName());
       return null;
     }

     Database db = dbp.get();
     RandomSetDao rsetDao = rsetDaoSupplier.get(siteInfo, db);
     PatientDao patDao = patDaoSupplier.get(db, siteId, user);
     RandomSetGroup g = null;

     // Need to handle assign, withdraw, re-assign, and maybe later, un-assign (i.e. assigned by mistake)
     boolean fromAssigned = rsp.getOriginalState().isAnAssignedState();
     boolean toAssigned = rsp.getState().isAnAssignedState();
     RandomSetParticipant.State state = rsp.getState();
     RandomSetParticipant.State origState = rsp.getOriginalState();
     if (!state.equals(origState)) { // state changed
       if (fromAssigned) {
         if (toAssigned) {  // assigned -> assigned
           if (origState.isWithdrawn() && state.isComplete()) {
             throw new RuntimeException("Wont allow RandomSet participation to change from "+origState+" to "+state);
           }
           g = rset.getGroup(rsp.getGroup());
           fixCounts(rsp, g, g);
         } else {           // assigned -> unassigned
           if (origState.isComplete() || origState.isWithdrawn()) {
             throw new RuntimeException("You must unassign from Assigned, not from Withdrawn or Complete");
           }
           g = rset.getGroup(rsp.getGroup());
           removeRandomPatient(rsp, db);  // throws an exception if not supported
           fixCounts(rsp, g, null);
         }
       } else {
         if (toAssigned) {  // unassigned -> assigned
           g = addRandomPatient(db, rsp.getStratumName());
           rsp.setGroup(g.getGroupName());
           rsp.setAssignedDate();
           fixCounts(rsp, null, g);

           rset.setFirstAssignedDate(rsp.getAssignedDate());
           rset.setLastAssignedDate(rsp.getAssignedDate());
         } else {          // unassigned -> unassigned
           fixCounts(rsp, null, null);
         }
       }
     }

     PatientAttribute pa = new PatientAttribute(rsp.getPatientId(), rsp.getAttrName(), rsp.getValue());
     patDao.insertAttribute(pa);
     rsp = rsetDao.updateOrInsertParticipant(rsp);
     return rsp;
   }

  private void fixCounts(RandomSetParticipant rsp, RandomSetGroup g1, RandomSetGroup g2) {
    incRsetForState(rsp.getOriginalState(), g1, -1);
    incRsetForState(rsp.getState(), g2, 1);
  }

   /**
    * Public because it's needed by the DAO
    * @param plusMinusCount  If state is originalState, pass -1 else +1
    */
   public int incRsetForState(RandomSetParticipant.State state, RandomSetGroup group, int plusMinusCount) {
     switch (state) {
     case Assigned:
     case Completed: // these are equivalent, for this purpose
       if (group != null) {
         group.receivePatientCounts(plusMinusCount, true);
       }
       return rset.incPatients(plusMinusCount);
     case Declined:
       return rset.incDeclined(plusMinusCount);
     case Disqualified:
       return rset.incExcluded(plusMinusCount);
     case Withdrawn:
       if (group != null) {
         group.receivePatientCounts(plusMinusCount, false);
       }
       return rset.incWithdrawn(plusMinusCount);

     case NotYetQualified:
     case Unset:
     default:
       return 0;
     }
   }

   // ==== Customized per algorithm ====

   abstract public String getAlgorithm();

   /**
    * Add a patient to the RandomSet
    */
  abstract protected RandomSetGroup addRandomPatient(Database db, String stratumName);

  /**
   * Remove a patient from the RandomSet (e.g. if someone is accidentally added)
   */
  abstract protected void removeRandomPatient(RandomSetParticipant rsp, Database db);

  // ==== Utilities


  /**
   * Hack: This must be run on the server because GWT7 can't handle String.format(format, objects)
   * This runs through the groups and refreshes the summary strings right before sending it to client.
   * @return the input participant, for convenience in chaining
   *
   * Commenting out until there's a UI for this
   * /
  public RandomSetParticipant updateGroupInfo(RandomSetParticipant rsp) {  // NUKE???
    if (rsp != null) {
      RandomSet rset = rsp.getRandomSet();
      for (RandomGroup g: rset.getGroups()) {
        g.refreshSummaryString(String.format(RandomGroup.summaryFormat, g.getSummaryObjects()));
      }
    }
    return rsp;
  } /* */



  // ==== Getters and setters

  public String getName() {
    return rset.getName();
  }

  public Long getSiteId() {
    return rset.getSiteId();
  }

  /**
   * Initializes a RandomSet's state from the database. If there's none, inserts it.
   */
  void initState(Supplier<Database> dbp) {
    RandomSetDao dao = rsetDaoSupplier.get(rset.getSiteId(), dbp.get());
    if (!dao.fetchRandomState(rset)) {
      dao.insertSet(rset);
      RandomSetCategory cats[] = rset.getCategories();
      if (cats == null || cats.length < 1) {
        dao.insertSetStratum(rset, RandomSetCategory.NoStratumName);
      } else {
        generateAllCategoryNames(dao, rset, new StringBuilder(100), 0);
      }

    }

    dao.initPatientCount(this);  // compute from DB patient counts and first/last dates
  }

  private void generateAllCategoryNames(RandomSetDao dao, RandomSet rset, StringBuilder nameBuffer, int catI) {
    if (catI == rset.getCategories().length) {
      dao.insertSetStratum(rset, nameBuffer.toString());
    } else {
      int len = nameBuffer.length();
      RandomSetCategory cat = rset.getCategories()[catI];
      for (RandomSetCategory.Value val: cat.getValues()) {
        generateAllCategoryNames(dao, rset, cat.addValueToNameID(nameBuffer,  val), catI+1);
        nameBuffer.setLength(len); // truncate the buffer to the original
      }
    }
  }
}
