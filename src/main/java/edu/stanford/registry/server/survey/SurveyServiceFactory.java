/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.shc.gi.GISQSurveyService;
import edu.stanford.registry.server.shc.pain.AceSurveyService;
import edu.stanford.registry.server.shc.pain.PhysicalTherapyService;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

public class SurveyServiceFactory {
  public static final String CLASSPARAM = "SurveyClassFor";
  protected static final ConcurrentHashMap<Long,SurveyServiceFactory> instanceMap =  new ConcurrentHashMap<Long,SurveyServiceFactory>();
  private static final Logger logger = LoggerFactory.getLogger(SurveyServiceFactory.class);

  // Loaded classes
  protected HashMap<String, SurveyServiceIntf> serviceObjects = new HashMap<>();
  protected SiteInfo siteInfo; // can't be final.  Need to always update this when it changes
  // We do NOT want to store a Database or Supplier<Database> here - connections become closed.
  // Plus we do NOT want to create any Survey services with a database, since we cache them.

  public SurveyServiceFactory(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * This returns a factory for the site.  The factory may have been cached, in which case it's
   * still guaranteed to have an up-to-date siteInfo.
   */
  public static SurveyServiceFactory getFactory(SiteInfo siteInfo) {
    SurveyServiceFactory instance = instanceMap.get(siteInfo.getSiteId());
    if (instance != null && siteInfo.getRevisionNumber() <= instance.siteInfo.getRevisionNumber()) {
      return instance;
    }
    return renewFactory(siteInfo); // once per site and whenever a siteInfo changes
  }

  /**
   * This creates or renews a SurveyServiceFactory for a site.  It's synchronized, but it doesn't
   * happen very often, just once per site and when a siteInfo changes.
   *
   * Each factory caches services it creates, and those services are all made with the siteInfo.
   * So if the siteInfo changes, the factory needs to be discarded or at least clear its cache.
   */
  private static synchronized SurveyServiceFactory renewFactory(SiteInfo siteInfo) {
    SurveyServiceFactory cachedInstance = instanceMap.get(siteInfo.getSiteId());
    if (cachedInstance != null && siteInfo.getRevisionNumber() <= cachedInstance.siteInfo.getRevisionNumber()) {
      return cachedInstance; // someone already handled the renewal
    }
    cachedInstance = new SurveyServiceFactory(siteInfo);
    instanceMap.put(siteInfo.getSiteId(), cachedInstance);
    return cachedInstance;
  }

  public SurveyServiceIntf getSurveyServiceImpl(String serviceName) {
    SurveyServiceIntf service = serviceObjects.get(serviceName);
    if (service != null)  // it was cached
      return service;

    int version = 1;
    String serviceClassName = serviceName;
    if (serviceName != null && serviceName.indexOf(".") > 0) {
      int indx = serviceName.indexOf(".");
      if (serviceName.length() > indx) {
        try {
          version = Integer.valueOf(serviceName.substring(indx + 1));
          serviceClassName = serviceName.substring(0, indx);
        } catch (NumberFormatException nfe) {}
      }
    }
    // Get the class name for the requested service from the config
    logger.debug("Requested " + serviceName + " service not loaded, reading config");
    try {
      if (serviceName == null || serviceName.equals("null")) {
        throw new Exception("null service requested");
      }
    } catch (Exception ex) {
      logger.error("Null service requested!!", ex);
    }
    String surveyServiceClassName = siteInfo.getProperty(CLASSPARAM + serviceClassName);

    if (surveyServiceClassName == null) {
      // Default implementations
      if ("PROMIS".equals(serviceClassName)) {
        service = new PromisSurveyService(siteInfo);
      } else if ("Local".equals(serviceClassName)) {
        service = new RegistryAssessmentsService(siteInfo);
      } else if ("LocalPromis".equals(serviceClassName)) {
        service = new LocalPromisSurveyService(siteInfo);
      } else if (StanfordCatSurveyService.STANFORD_CAT_NO_SKIP.equals(serviceClassName)) {
        service = new StanfordCatSurveyService(siteInfo, false);
      } else if (StanfordCatSurveyService.STANFORD_CAT_ALLOW_SKIP.equals(serviceClassName)) {
        service = new StanfordCatSurveyService(siteInfo, true);
      } else if ("ChronicMigraineSurveyService".equals(serviceClassName)) {
        service = new NamedSurveyService(new edu.stanford.registry.server.survey.ChronicMigraineSurveyService(siteInfo));
      } else if ("ChildrenQuestionService".equals(serviceClassName)) {
        service = new ChildrenQuestionService("children8to12", siteInfo);
      } else if (serviceClassName.equals("PainPsychologyService")) {
        service = new PainPsychologyService("painPsych", siteInfo);
      } else if (serviceClassName.equals("MarloweCrowneService")) {
        service = new MarloweCrowneService(siteInfo);
      } else if (serviceClassName.equals("COPCSService")) {
        service = new COPCSService("COPCS", siteInfo);
      } else if (serviceClassName.equals("GISQSurveyService")) {
        service = new GISQSurveyService("GISQ", siteInfo);
      } else if (serviceClassName.equals("PhysicalTherapyService")) {
        service = new PhysicalTherapyService(siteInfo);
      } else if (serviceClassName.equals("AceSurveyService")) {
        service = new AceSurveyService(siteInfo);
      } else if (AngerService.isMyService(serviceClassName)) {
        service = new AngerService(serviceClassName, siteInfo);
      } else if (QualifyQuestionService.isMyService(serviceClassName)) {
        service =  new QualifyQuestionService(serviceClassName, siteInfo);
      } else if (RepeatingSurveyService.isMyService(serviceClassName)) {
        service = new NamedSurveyService(new RepeatingSurveyService(siteInfo, serviceClassName));
      } else if (MediaService.isMyService(serviceClassName)) {
        service = new MediaService(siteInfo, serviceName);
      } else if (PainPsychMindfulnessService.isMyService(serviceClassName)) {
        service = new PainPsychMindfulnessService(siteInfo);
      } else if (EmpowerStudyService.isMyService(serviceClassName)) {
        service = new EmpowerStudyService(siteInfo);
      } else {
        try {
          service = new NamedSurveyService(siteInfo, serviceName);
        } catch (ServiceUnavailableException se) {
          logger.error("failed to load " + serviceClassName);
        }
      }
      if (service != null) {
        service.setVersion(version);
        serviceObjects.put(serviceName, service);
        return service;
      }
      logger.debug(CLASSPARAM + serviceName + " not found in config.");
      return null;
    }

    if (serviceName == null || serviceName.equals("null")) { // Error plus stack trace
      logger.error("Null service requested!!", new Exception("null service requested"));
    }

    surveyServiceClassName = siteInfo.getProperty(CLASSPARAM + serviceClassName);
    logger.debug("surveyServiceClassName = "+surveyServiceClassName+" for "+CLASSPARAM+"+"+serviceClassName);

    if (surveyServiceClassName == null) {
      return getDefaultSurveyService(serviceName, version, serviceClassName);
    }

    return getSurveyServiceFromClass(serviceName, version, surveyServiceClassName);
  }

  SurveyServiceIntf getSurveyServiceFromClass(String serviceName, int version, String surveyServiceClassName) {
    SurveyServiceIntf service = null;
    Class<?> surveyImplClass;

    // Get the class
    try {
      surveyImplClass = Class.forName(surveyServiceClassName.trim());
    } catch (ClassNotFoundException e) {
      logger.error("Cannot create Survey Service, no class with name: " + CLASSPARAM + serviceName, e);
      return null;
    }

    // Try a constructor(SiteInfo)
    try {
      Constructor<?> constructor = surveyImplClass.getConstructor(SiteInfo.class);
      service = (SurveyServiceIntf) constructor.newInstance(siteInfo);
    } catch (Exception ex) {
      // try one more time
    }

    // Try a constructor(SiteId)
    if (service == null)
    try {
      Constructor<?> constructor = surveyImplClass.getConstructor(Long.class);
      service = (SurveyServiceIntf) constructor.newInstance(siteInfo.getSiteId());
    } catch (Exception ex) {
      logger.error("Cannot create Survey Service using name: " + CLASSPARAM + serviceName, ex);
      return null;
    }

    service.setVersion(version);
    serviceObjects.put(serviceName, service);
    return service;
  }

  SurveyServiceIntf getDefaultSurveyService(String serviceName, int version, String serviceClassName) {
    SurveyServiceIntf service = null;
    
    // Default implementations
    if ("PROMIS".equals(serviceClassName)) {
      service = new PromisSurveyService(siteInfo);

    } else if ("Local".equals(serviceClassName)) {
      service = new RegistryAssessmentsService(siteInfo);
    } else if ("LocalPromis".equals(serviceClassName)) {
      service = new LocalPromisSurveyService(siteInfo);
    } else if (StanfordCatSurveyService.STANFORD_CAT_NO_SKIP.equals(serviceClassName)) {
      service = new StanfordCatSurveyService(siteInfo, false);
    } else if (StanfordCatSurveyService.STANFORD_CAT_ALLOW_SKIP.equals(serviceClassName)) {
      service = new StanfordCatSurveyService(siteInfo, true);
    } else if ("ChronicMigraineSurveyService".equals(serviceClassName)) {
      service = new NamedSurveyService(new ChronicMigraineSurveyService(siteInfo));
    } else if ("ChildrenQuestionService".equals(serviceClassName)) {
      service = new ChildrenQuestionService("children8to12", siteInfo);
    } else if (serviceClassName.equals("PainPsychologyService")) {
      service = new PainPsychologyService("painPsych", siteInfo);
    } else if (serviceClassName.equals("COPCSService")) {
      service = new COPCSService("COPCS", siteInfo);
    }  else if (serviceClassName.equals("GISQSurveyService")) {
      service = new GISQSurveyService("GISQ", siteInfo);
    } else if (QualifyQuestionService.isMyService(serviceClassName)) {
      service =  new QualifyQuestionService(serviceClassName, siteInfo);
    } else if (RepeatingSurveyService.isMyService(serviceClassName)) {
      service = new NamedSurveyService(new RepeatingSurveyService(siteInfo, serviceClassName));
    } else if (PainPsychMindfulnessService.isMyService(serviceClassName)) {
      service = new PainPsychMindfulnessService(siteInfo);
    } else if (EmpowerStudyService.isMyService(serviceClassName)) {
      service = new EmpowerStudyService(siteInfo);
    } else {
      try {
        service = new NamedSurveyService(siteInfo, serviceName);
      } catch (ServiceUnavailableException se) {
        logger.error("failed to load " + serviceClassName);
      }
    }
    if (service != null) {
      service.setVersion(version);
      serviceObjects.put(serviceName, service);
      return service;
    }
    logger.debug(CLASSPARAM + serviceName + " not found in config.");
    return null;
  }

  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String serviceName, String studyName) {
    SurveyServiceIntf surveyService = getSurveyServiceImpl(serviceName);
    if (surveyService != null)
      return surveyService.getScoreProvider(dbp, studyName);

    logger.warn("No scoreProvider because no surveyService found for svc: "+serviceName);
    return null;
  }

  public static Document getDocument(PatientStudyExtendedData patientData) throws ParserConfigurationException,
      SAXException, IOException {
    if (patientData == null || patientData.getContents() == null) {
      return null;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // Using factory get an instance of document builder
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(patientData.getContents()));

    return db.parse(is);
  }

}
