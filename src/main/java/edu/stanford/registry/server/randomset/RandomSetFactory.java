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
import edu.stanford.registry.server.utils.ClassCreator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * RandomSet definitions are stored in AppConfig, so are provided by siteInfo.
 * When it reads them in, it calls this to create
 */
public class RandomSetFactory {
  private static final org.apache.log4j.Logger log4jer = org.apache.log4j.Logger.getLogger(RandomSetFactory.class);
  private static final Logger logger = LoggerFactory.getLogger(RandomSetFactory.class);
  private static final ClassCreator<RandomSetter> classCreator =
      new ClassCreator<>("RandomSet.init(customs)", "RandomSet", log4jer, Long.class, String.class);

  private static final HashMap<String,String> typeHash = initTypes();

  private final SiteInfo siteInfo;


  public RandomSetFactory(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }


  private synchronized RandomSetter create(String type, Long siteId, String name) {
    String className = typeHash.get(type);
    if (className == null) {
      throw new AssertionError("Requested a RandomSet or non-existent type "+type+" for site="+siteId+" name="+name);
    }
    return classCreator.createClass(className, siteId, name);
  }


  // ==== initialization code

  // FYI:   https://dzone.com/articles/get-all-classes-within-package
  // for finding all classes in a given package, eg all RandomSets
  static private HashMap<String,String> initTypes() {
    HashMap<String,String> map = new HashMap<String,String>(10);
    addType(map, "Pure", "edu.stanford.registry.server.randomset.RandomSetPure");
    addType(map, "KSort", "edu.stanford.registry.server.randomset.RandomSetKSort");
    addType(map, "TSort", "edu.stanford.registry.server.randomset.RandomSetTenSet");
    return map;
  }


  /**
   * NOT YET USED/tested... loads from a string of algorithm,class,algorithm,class
   * This should be called whenever the global SitesInfo changes.
   */
  static public void initCustomAlgorithms(String customAdders) {
    if (customAdders == null || customAdders.isEmpty()) {
      return;
    }
    Long dummyLong = 0L;
    String dummyName = "dummyName"; // so we can build one and ensure constructor works
    String list[] = customAdders.split(",");
    int errors = 0;
    String algorithm = null;

    for (String s: list) {
      if (algorithm == null) {
        algorithm = s;
        continue;
      }
      String className = s;
      RandomSetter randomSet = classCreator.createClass(className, dummyLong, dummyName);
      if (randomSet == null || !addType(typeHash, algorithm, className)) { // cc or addType already complained
        errors++;
      }
      algorithm = null; // so pick up next algorithm
    }
    if (errors > 0) {
      throw new RuntimeException("Problem in initCustomAdders");
    }
  }


  /**
   * This just ensures we give a good error if an adder with the given name is already in the list
   */
  private static boolean addType(HashMap<String,String> typeMap, String algorithm, String className) {
    String old = typeMap.get(algorithm);
    if (old != null) {
      String msg = "Already have a RandomSet mapping "+algorithm+" -> "+old.getClass().getCanonicalName();
      if (old.equals(className)) {
        logger.warn(msg);  // tolerable...
        return true;
      } else {
        logger.error(msg+", ignoring mapping to " + className);
        return false;
      }
    }
    typeMap.put(algorithm, className);
    return true;
  }


  /**
   * Creates the hash of RandomSets for a SiteInfo from the hash of JSons in appconfig.
   */
  public HashMap<String,RandomSetter> createRandomSets(Supplier<Database> dbp, HashMap<String,String> jsons) {
    if (siteInfo.getSiteId().longValue() == 1 || jsons.size() > 0) {
      logger.debug(siteInfo.getIdString()+"Creating randomsets: "+jsons.size());
    }
    HashMap<String,RandomSetter> hash = new HashMap<String,RandomSetter>(jsons.size());
    for (Entry<String, String> entry: jsons.entrySet()) {
      String name = entry.getKey();
      RandomSetter r = createRandomSet(name, entry.getValue());
      if (r != null) {
        if (dbp != null) { // is null during testing
          r.initState(dbp);
        }
        hash.put(name, r);
      }
    }
    return hash;
  }


  /**
   * Creates a single named RandomSet from a jsonString.
   *
   * It creates the JSON object, gets the algorithm from it, makes the object and gives it
   * the JSON to initialize itself.
   */
  private RandomSetter createRandomSet(String name, String jsonString) {
    JSONObject j;
    try {
      j = new JSONObject(jsonString);
    } catch (org.json.JSONException e) {
      logger.error(siteInfo.getIdString()+"Can't create RandomSet '"+name+"' from json: "+jsonString, e);
      return null;
    }
    if (!name.equals(j.getString("name"))) {
      logger.error(siteInfo.getIdString()+"Config RandomSet '"+name+"' has internal name: "+j.getString("name"));
      return null;
    }
    String alg = j.getString("alg");
    try {
      RandomSetter r = create(alg, siteInfo.getSiteId(), name);
      return r.init(j);
    } catch (Throwable t) {
      logger.error(siteInfo.getIdString()+"Can't create "+alg+" RandomSet, name: "+name, t);
      return null;
    }
  }

}
