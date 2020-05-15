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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.PrintStudyComparator;
import edu.stanford.registry.server.survey.SurveyUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.github.susom.database.Database;

/**
 * These are static and loaded in the first time one's used.
 * If you deploy a new process.xml for any site, restart the server.
 * Note there's a reload() method containing a comment on how to easily update this.
 */
public class XMLFileUtils {
  private static final Logger logger = Logger.getLogger(XMLFileUtils.class);

  public static final String ATTRIBUTE_APPOINTMENT_TEMPLATE = "appointment_template";
  public static final String ATTRIBUTE_SCHEDULE_TEMPLATE = "schedule_template";
  public static final String ATTRIBUTE_EXPIRE_DT = "expiration_date";
  public static final String ATTRIBUTE_START_DT = "start_date";
  private static final String ATTRIBUTE_ORDER = "order";
  private static final String ATTRIBUTE_VISIT_TYPE = "visitType";
  private static final String ATTRIBUTE_SURVEY_NAME = "surveyName";
  private static final String ATTRIBUTE_HIDDEN = "hidden";
  private static final String DEFAULT_SURVEY_NAME = "Default";

  private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  private static final SimpleDateFormat dtFormat = new SimpleDateFormat("MM-dd-yyyy");
  private static final Object utilsLock = new Object();

  private final String processXml;
  private final SiteInfo siteInfo;
  private String xmlPath = null;
  private String resourcePath = null;
  private String fileIdentifierForMsg;

  private HashMap<String, ProcessType> processes;
  private HashMap<String, ArrayList<Element>> processQuestionaires;
  private HashMap<String, ArrayList<ProcessType>> processSubTypes;
  private HashMap<String, NamedNodeMap> patientAttributes;
  private HashMap<String, NamedNodeMap> visitTypeAttributes;
  private HashMap<String, ArrayList<Media>> mediaHash;

  HashMap<String,String> emailTemplateNames = new HashMap<>(10);

  private ConcurrentHashMap<String, String> contentsCache;

  private static final HashMap<Long,XMLFileUtils> instances = new HashMap<>();

  /**
   * If you set xml_dir, the process.xml will be sought there.
   *
   * If you set xml_resource, it'll be sought in that TomCat sub-folder.
   *
   * @param xmlFileName  The cached, standard XMLFileUtils are always named process.xml
   */
  protected XMLFileUtils(SiteInfo siteInfo, String xmlFileName) {
    this.siteInfo = siteInfo;
    this.processXml = xmlFileName;
    init();
  }

  /**
   * Private- standard XMLFileUtils for your site are cached.
   */
  private XMLFileUtils(SiteInfo siteInfo) {
    this(siteInfo, "process.xml");
  }

  public static void fillCache(SiteInfo siteInfo) {
    try {
      getInstance(siteInfo);
    } catch (Throwable t) {
      logger.warn("XMLFileUtils problem: " + t.getMessage());
    }
  }

  public static XMLFileUtils getInstance(SiteInfo siteInfo) {
    XMLFileUtils utils = instances.get(siteInfo.getSiteId());
    if (utils == null) {
      synchronized (utilsLock) {

        long siteId = siteInfo.getSiteId();
        utils = instances.get(siteId);
        if (utils == null) {
            utils = new XMLFileUtils(siteInfo);
            instances.put(siteId, utils);
        }

      }
    }
    return utils;
  }

  public void reload(SiteInfo siteInfo) {
    try {
      XMLFileUtils newUtils = new XMLFileUtils(siteInfo, processXml);
      instances.put(siteInfo.getSiteId(), newUtils);
    } catch (Throwable t) {
      logger.error(t);
    }
  }


  /**
   * Log whenever we store something in the cache, size & number of comments so we can match
   * the survey name to a specific file.
   */
  private void putInCache(String whatCode, String studyName, String contents) {
    contentsCache.put(studyName, contents);
    if (!logger.isDebugEnabled()) {
      return;
    }
    logger.debug(String.format("%s%s, caching: %s size=%d",
        siteInfo.getIdString(), whatCode, studyName, contents.length()));
  }


  public void updateStudyContents(String studyName, String contents) {
    if (contents != null) {
      putInCache("updateStudyContents", studyName, contents);
    } else {
      // it's safe to call remove even if the study isn't in there
      logger.debug(siteInfo.getIdString()+"updateStudyContents, caching: removing "+studyName);
      contentsCache.remove(studyName);
    }
  }


  /**
   * If someone chooses to delete a global study, they'll want to clear it from every cache.
   */
  public void removeStudyFromAllSiteCaches(String studyName) {
    for (XMLFileUtils utils: instances.values()) {
      utils.updateStudyContents(studyName, null);
    }
  }


  private void init() {
    processes = new HashMap<>();
    processQuestionaires = new HashMap<>();
    processSubTypes = new HashMap<>();
    patientAttributes = new HashMap<>();
    visitTypeAttributes = new HashMap<>();
    mediaHash = new HashMap<>();
    contentsCache = new ConcurrentHashMap<>();

    xmlPath = siteInfo.getPathProperty("xml_dir", null);
    // note- xmlPath need not end with a '/'

    resourcePath = siteInfo.getPathProperty("xml_resource", Constants.XML_PATH_DEFAULT);
    if (!resourcePath.endsWith("/")) {
      resourcePath += "/";
    }

    fileIdentifierForMsg = String.format("%s has %s/filename: '%s/%s", siteInfo.getIdString(),
                                         (xmlPath==null ? "xml_resource" : "xml_dir"), resourcePath, processXml);
    // Read the process.xml file and save into the hash tables

    try {
      // Get an input stream for the process.xml file
      StringBuilder sb = new StringBuilder();
      InputStream is;

      // Try the database configparam first
      if (siteInfo.getProperty("process.xml") != null && !siteInfo.getProperty("process.xml").isEmpty()) {
        is = new ByteArrayInputStream(siteInfo.getProperties().get("process.xml").getBytes());
      } else {
        is = getXMLStream(processXml, sb);
      }
      if (is == null) {
        throw new FileNotFoundException(sb.toString());
      }
      logger.debug(siteInfo.getIdString()+"Reading in "+sb.toString());

      // Parse the XML
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(is);
      if (doc == null) {
        throw new DataException("Unable to parse");
      }

      // Root document element must be "Process"
      Element docElement = doc.getDocumentElement();
      if (!docElement.getTagName().equals("Process")) {
        throw new DataException("first docElement is not Process, it is: " + docElement.getTagName());
      }

      // Process must contain ProcessType elements
      NodeList processTypes = doc.getElementsByTagName("ProcessType");
      if (processTypes == null || processTypes.getLength() < 1) {
        throw new DataException("found no ProcessType definitions");
      }

      // Handle each ProcessType
      for (int pt = 0; pt < processTypes.getLength(); pt++) {
        Element processTypeElem = (Element) processTypes.item(pt);
        String processName = processTypeElem.getAttribute("value");
        ProcessType processType = new ProcessType(processName, processTypeElem);
        processes.put(processName, processType);

        // Handle the ProcessType email attributres and child elements
        handleProcessTypeElements(processName, processTypeElem, emailTemplateNames);

        // Handle the ProcessSubType elements
        NodeList subTypeNodes = processTypeElem.getElementsByTagName("ProcessSubType");
        if ((subTypeNodes != null) && (subTypeNodes.getLength() > 0)) {
          ArrayList<ProcessType> subTypeList = new ArrayList<>();
          for (int q = 0; q < subTypeNodes.getLength(); q++) {
            Element element = (Element) subTypeNodes.item(q);
            ProcessType subType = handleProcessSubType(processType, element);
            subTypeList.add(subType);
          }
          processSubTypes.put(processName, subTypeList);
        }
      }

      // Save the VisitType elements under VisitTypes
      NodeList visitTypesNodes = doc.getElementsByTagName("VisitTypes");
      if ((visitTypesNodes != null) && (visitTypesNodes.getLength() > 0)) {
        Element visitTypes = (Element) visitTypesNodes.item(0);
        NodeList visitNodes = visitTypes.getElementsByTagName("VisitType");
        for (int vtIndx = 0; vtIndx < visitNodes.getLength(); vtIndx++) {
          Element visit = (Element) visitNodes.item(vtIndx);
          visitTypeAttributes.put(visit.getAttribute("value"), visit.getAttributes());
        }
      }
    } catch (Throwable e) {
      throw new DataException("Problem reading "+fileIdentifierForMsg+" - " + e.getMessage());
    }
  }

  /**
   * Handle a ProcessSubType element in a ProcessType
   */
  private ProcessType handleProcessSubType(ProcessType parent, Element processSubTypeElem) {
    String parentName = parent.getProcessName();
    String surveyName = processSubTypeElem.getAttribute(ATTRIBUTE_SURVEY_NAME);
    if ((surveyName == null) || (surveyName.equals(""))) {
      throw new DataException("ProcessSubType does not have attribute " + ATTRIBUTE_SURVEY_NAME + " in " + fileIdentifierForMsg);
    }

    // Determine the process name for the sub-type
    String processName;
    int i = parentName.lastIndexOf(".");
    if (i >= 0) {
      processName = parentName.substring(0,i) + surveyName + parentName.substring(i);
    } else {
      processName = parentName + surveyName;
    }

    // The sub type inherits the parent ProcessType attributes
    HashMap<String,String> parentAttrs = parent.getAttributes();
    // Do not show the sub type in the active visit process names
    parentAttrs.put(ATTRIBUTE_HIDDEN, "true");

    ProcessType processType = new ProcessType(processName, processSubTypeElem, parentAttrs);
    processes.put(processName, processType);

    handleProcessTypeElements(processName, processSubTypeElem, null);

    return processType;
  }

  /**
   * Handle a Process element child elements
   */
  private void handleProcessTypeElements(String processName, Element processTypeElem, HashMap<String,String>emails) {
    if (emails != null) {
      collectEmailTemplateNames(processTypeElem, emails);
    }

    // Save the Questionaire elements (direct children only)
    NodeList questionaires = processTypeElem.getElementsByTagName("Questionaire");
    if (questionaires != null) {
      ArrayList<Element> elementList = new ArrayList<>();
      for (int q = 0; q < questionaires.getLength(); q++) {
        Element studyQ = (Element) questionaires.item(q);
        if (processTypeElem.equals(studyQ.getParentNode())) {
          elementList.add(studyQ);
        }
      }
      if (!elementList.isEmpty()) {
        autoOrderQuestionaires(elementList);
        processQuestionaires.put(processName, elementList);
      }
    }

    // Save the attributes of the PatientAttribute element (direct child only)
    NodeList patientAttribNodes = processTypeElem.getElementsByTagName("PatientAttribute");
    if (patientAttribNodes != null) {
      for (int p = 0; p < patientAttribNodes.getLength(); p++) {
        Element patientA = (Element) patientAttribNodes.item(p);
        if (processTypeElem.equals(patientA.getParentNode())) {
          patientAttributes.put(processName, patientA.getAttributes());
        }
      }
    }

    // Save the Media elements (direct children only)
    NodeList mediaLinkNodes = processTypeElem.getElementsByTagName("Media");
    if (mediaLinkNodes != null) {
      ArrayList<Media> mediaArray = new ArrayList<>();
      for (int p = 0; p < mediaLinkNodes.getLength(); p++) {
        Element mediaLinkElement = (Element) mediaLinkNodes.item(p);
        if (processTypeElem.equals(mediaLinkElement.getParentNode())) {
          mediaArray.add(new Media(mediaLinkElement));
        }
      }
      if (!mediaArray.isEmpty()) {
        mediaHash.put(processName, mediaArray);
      }
    }
  }


  private void collectEmailTemplateNames(Element processTypeElem, HashMap<String, String> emails) {
    String attr1 = processTypeElem.getAttribute(ATTRIBUTE_APPOINTMENT_TEMPLATE);
    attr1 = (attr1 == null) ? "" : attr1.trim();
    String attr2 = processTypeElem.getAttribute(ATTRIBUTE_SCHEDULE_TEMPLATE);
    attr2 = (attr2 == null) ? "" : attr2.trim();
    if (!attr1.isEmpty() || !attr2.isEmpty()) {
      String expireDateStr = processTypeElem.getAttribute(ATTRIBUTE_EXPIRE_DT);
      if (expireDateStr != null) {
        expireDateStr = expireDateStr.trim();
        if (!expireDateStr.isEmpty()) {
          try {
            Date expirationDate = dtFormat.parse(expireDateStr);
            if (!expirationDate.after(new Date())) {
              return;  // expires today or earlier- no point worrying if template might be missing...
            }
          } catch (ParseException pe) {
            // if error, fall through, as if there'sno expiration date
          }
        }
      }
      String processName = processTypeElem.getAttribute("value");
      addEmailTemplateName(attr1, processName, emails);
      addEmailTemplateName(attr1, processName, emails);
    }
  }

  private void addEmailTemplateName(String templateName, String processName, HashMap<String, String> emails) {
    if (templateName.isEmpty()) {
      return;
    }
    emails.put(templateName, processName); // just keep the last process that used it
  }


  /**
   * The implementations of SurveyServiceIntf.registerAssessment assume that each
   * Questionaire element has an order attribute. This method attempts to automatically
   * assign an order attribute to each Questionaire element based on the order in which
   * it appears in the ProcessType. The auto ordering will not be done if any of the
   * Questionaire elements in the ProcessType specifies an order attribute.
   */
  private void autoOrderQuestionaires(ArrayList<Element> questionaires) {
    // Determine if any of the questionaire elements specify an order attribute
    boolean hasOrder = false;
    for(Element questionaire : questionaires) {
      String qOrder = questionaire.getAttribute(edu.stanford.registry.shared.survey.Constants.XFORM_ORDER);
      if (!qOrder.equals("")) {
        hasOrder = true;
      }
    }

    // If none of the questionaire elements specify an order attribute then we can
    // automatically assign an order attribute based on their order in the list.
    if (!hasOrder) {
      int order = 1;
      for(Element questionaire : questionaires) {
        questionaire.setAttribute(edu.stanford.registry.shared.survey.Constants.XFORM_ORDER, Integer.toString(order));
        order += 1;
      }
    }
  }

  public HashMap<String,String> getReferencedEmailTemplateNames() {
    return emailTemplateNames;
  }

  /**
   * Gets the value of the attribute for the process type
   */
  public String getAttribute(String processName, String attributeName) throws DataException {
    ProcessType processType = processes.get(processName);
    if (processType != null) {
      HashMap<String,String> attributes = processType.getAttributes();
      return attributes.get(attributeName);
    }
    return null;
  }

  /**
   * Get the attribute name/value pairs for the process type
   */
  public HashMap<String, String> getAttributes(String processName) {
    ProcessType processType = processes.get(processName);
    if (processType != null) {
      return processType.getAttributes();
    }
    return new HashMap<>();
  }

  /**
   * Get the value of the attribute as a date for the process type
   */
  public Date getAttributeDate(String processName, String attributeName) throws DataException {
    String dateString = getAttribute(processName, attributeName);
    try {
      if (dateString != null && dateString.trim().length() > 0) {
        return dtFormat.parse(dateString);
      }
    } catch (ParseException e) {
      logger.error("Invalid date attribute " + attributeName + " value of " + dateString + " in " + fileIdentifierForMsg);
    }
    return null;
  }

  /**
   * Returns the name of the first process type that contains this attribute=value pair
   */
  public String getProcess(String attributeName, String attributeValue) {
    List<String> processes = getProcesses(attributeName, attributeValue);
    if (processes.isEmpty()) {
      return null;
    } else {
      return processes.get(0);
    }
  }

  /**
   * Returns the name of the all process types that contains this attribute=value pair
   */
  public List<String> getProcesses(String attributeName, String attributeValue) {
    List<String> result = new ArrayList<>();
    for(ProcessType processType : processes.values()) {
      HashMap<String,String> attributes = processType.getAttributes();
      String value = attributes.get(attributeName);
      if ((value != null) && value.equals(attributeValue)) {
        result.add(processType.getProcessName());
      }
    }
    return result;
  }

  /**
   * Get all process type names
   */
  public ArrayList<String> getProcessNames() {
    ArrayList<String> names = new ArrayList<>();
    for(String processName : processes.keySet()) {
      names.add(processName);
    }
    return names;
  }

  /**
   * Get active visit process type names. Active visit process types are
   * those that have a visitType attribute and which are not expired.
   */
  public ArrayList<String> getActiveVisitProcessNames() {
    ArrayList<ProcessType> activeProcesses = new ArrayList<>();
    for (ProcessType processType : processes.values()) {
      if (processType.isActiveVisitProcess()) {
        activeProcesses.add(processType);
      }
    }

    Collections.sort(activeProcesses, new ProcessTypeComparator());

    ArrayList<String> names = new ArrayList<>();
    for (ProcessType activeProcess : activeProcesses) {
      names.add(activeProcess.getProcessName());
    }
    return names;
  }

  /**
   * Get a list of process names from the process.xml file that are surveys
   */
  public ArrayList<String> getSurveyProcessNames() {
    ArrayList<String> names = new ArrayList<>();
    for(String processName : processes.keySet()) {
      List<String> surveyNames = getProcessSurveyNames(processName);
      for(String surveyName : surveyNames) {
        String surveyProcessName = getProcessSurveyType(processName, surveyName);
        ArrayList<Element> questionaires = getProcessQuestionaires(surveyProcessName);
        if ((questionaires != null) && (questionaires.size() > 0)) {
          if (!names.contains(processName)) {
            names.add(processName);
          }
        }
      }
    }
    return names;
  }

  /**
   * Get the active process name for the given name and date. This
   * looks for an active process on the given date which starts
   * with the name followed a "." and a version.
   */
  public String getActiveProcessForName(String name, Date surveyDate) {
    for(ProcessType process : processes.values()) {
      String processName = process.getProcessName();
      Date starts = process.getStartDate();
      Date expires = process.getExpirationDate();

      boolean processActive = true;
      if ((starts != null) && surveyDate.before(starts)) {
        processActive = false;
      }
      if ((expires != null) && surveyDate.after(expires)) {
        processActive = false;
      }

      if (processName.equals(name) && processActive) {
        return processName;
      } else if (processName.startsWith(name + ".") && processActive) {
        return processName;
      }
    }
    return null;
  }

  /**
   * Get the process names with a particular category attribute value
   */
  public ArrayList<String> getProcessNamesByCategory(String category) {
    ArrayList<String> processNames = new ArrayList<>();
    for (ProcessType processType : processes.values()) {
      if (processType.getCategory().equals(category)) {
        processNames.add(processType.getProcessName());
      }
    }
    return processNames;
  }

  /**
   * Returns the list of questionaire elements for the process type
   */
  public ArrayList<Element> getProcessQuestionaires(String processName) {
    if (processQuestionaires.get(processName) != null) {
      return processQuestionaires.get(processName);
    }
    return null;
  }

  /**
   * Returns the list of process survey names for the process type.
   * Survey names are defined by ProcessSubType elements. If the Process
   * does not contain any ProcessSubType elements then the survey name
   * "Default" is returned.
   */
  public List<String> getProcessSurveyNames(String processName) {
    List<String> surveyNames = new ArrayList<>();
    List<ProcessType> subTypes = processSubTypes.get(processName);
    if ((subTypes == null) || (subTypes.size() == 0)) {
      surveyNames.add(DEFAULT_SURVEY_NAME);
    } else {
      for(ProcessType subType : subTypes) {
        surveyNames.add(subType.getSurveyName());
      }
    }
    return surveyNames;
  }

  /**
   * Gets the resolved process type for the process type and process survey name.
   * If there are no surveys for the process type then this returns
   * the process type.
   */
  public String getProcessSurveyType(String processName, String surveyName) {
    List<ProcessType> subTypes = processSubTypes.get(processName);
    if ((subTypes == null) || (subTypes.size() == 0)) {
      return processName;
    } else {
      for(ProcessType subType : subTypes) {
        if (surveyName.equals(subType.getSurveyName())) {
          String surveyProcessName = subType.getProcessName();
          if ((surveyProcessName != null) && !surveyProcessName.equals("")) {
            return surveyProcessName;
          }
          return processName;
        }
      }
    }
    return processName;
  }

  /**
   * Get all patient attributes for all process types
   */
  public HashMap<String, ArrayList<PatientAttribute>> getAllPatientAttributes() {
    HashMap<String, ArrayList<PatientAttribute>> attributes = new HashMap<>();
    ArrayList<String> processNames = getProcessNames();
    for (String processName : processNames) {
      attributes.put(processName, getPatientAttributes(processName));
    }
    return attributes;
  }

  /**
   * Get the patient attributes for the process type
   */
  private ArrayList<PatientAttribute> getPatientAttributes(String processName) {
    ArrayList<PatientAttribute> attributes = new ArrayList<>();
    NamedNodeMap nodeMap = patientAttributes.get(processName);
    if (nodeMap != null) {
      for (int n = 0; n < nodeMap.getLength(); n++) {

        Node nameNode = nodeMap.getNamedItem("data_name");
        Node valuNode = nodeMap.getNamedItem("data_value");
        Node typeNode = nodeMap.getNamedItem("data_type");

        if (nameNode != null && valuNode != null) {
          PatientAttribute pattrib = new PatientAttribute();
          pattrib.setDataName(nameNode.getNodeValue());
          pattrib.setDataValue(valuNode.getNodeValue());
          pattrib.setDataType(typeNode.getNodeValue());
          attributes.add(pattrib);
        }
      }
    }
    return attributes;
  }

  public ArrayList<String> getMediaNames(String processName, String page) {

    ArrayList<Media> mediaArray = mediaHash.get(processName);
    if (mediaArray == null) {
        mediaArray = new ArrayList<>();
    }
    Collections.sort(mediaArray, new MediaComparator());
    ArrayList<String> mediaNames = new ArrayList<>();
    for (Media media : mediaArray) {
      mediaNames.add(media.getName());
   }
    return mediaNames;
  }

  public HashMap<String, String> getMediaAttributes(String processName, String page, String mediaName) {
    ArrayList<Media> mediaArray = mediaHash.get(processName);
    if (mediaArray == null) {
        mediaArray = new ArrayList<>();
    }
    for (Media media : mediaArray) {
      if (media.getName().equals(mediaName)) {
        return media.getMediaAttributes();
      }
    }
    return null;
  }

  public ArrayList<String> getVisitTypesInGroup(String group) {
    ArrayList<String> typeNames = new ArrayList<>();
    if (group == null) {
      return typeNames;
    }

    for(String visitType : visitTypeAttributes.keySet()) {
      if (group.equals(getVisitAttribute(visitType, "group"))) {
        typeNames.add(visitType);
      }
    }
    return typeNames;
  }

  public ArrayList<String> getExcludeFromSurveyCntVisits() {
    ArrayList<String> excludeVisitTypes = new ArrayList<>();
    for(String visitType : visitTypeAttributes.keySet()) {
      if ((getVisitAttribute(visitType, "excludeFromSurveyCnt") != null) &&
          (getVisitAttribute(visitType, "excludeFromSurveyCnt").equals("true"))) {
          excludeVisitTypes.add(visitType);
      }
    }
    return excludeVisitTypes;
  }

// --Commented out 12/7/17, not used
//  public HashMap<String, String> getVisitTypeGroups() {
//    HashMap<String, String> visitGroupHash = new HashMap<>();
//    for(String visitType : visitTypeAttributes.keySet()) {
//      String group = getVisitAttribute(visitType, "group");
//      if (group == null) {
//        group = Constants.VISIT_GROUP_DEFAULT;
//      }
//      visitGroupHash.put(visitType, group);
//    }
//    return visitGroupHash;
//  }
// --Commented out STOP

  private String getVisitAttribute(String visitType, String attributeName) {
    NamedNodeMap nodeMap = visitTypeAttributes.get(visitType);
    Node nodeAttribute = nodeMap.getNamedItem(attributeName);
    if (nodeAttribute != null) {
      return nodeAttribute.getNodeValue();
    }
    return null;
  }

  /**
   *  Get the list of PrintStudies for the process type
   */
  public ArrayList<PrintStudy> getPrintStudies(String processName,
        ArrayList<SurveySystem> surveySystems, ArrayList<Study> studies, boolean includeOptionals) {
    logger.debug("getProcessessOrdered: starting for processType: " + processName + " includeOptionals: " + includeOptionals);

    ArrayList<PrintStudy> printStudies = new ArrayList<>();

    if ((processName == null) || (surveySystems == null) || (studies == null)) {
      logger.error(siteInfo.getIdString()+": getProcessessOrdered called with null object");
      return printStudies;
    }

    // Get the questionaires for all of the process type surveys
    ArrayList<Element> questionaires = new ArrayList<>();
    List<String> surveyNames = getProcessSurveyNames(processName);
    for(String surveyName : surveyNames) {
      String surveyProcessName = getProcessSurveyType(processName, surveyName);
      ArrayList<Element> processQuestionaires = getProcessQuestionaires(surveyProcessName);
      if (processQuestionaires != null) {
        questionaires.addAll(processQuestionaires);
      }
      String optionalType = getAttribute(surveyProcessName, "optional_questionnaires");
      if (includeOptionals && optionalType != null && !optionalType.isEmpty()) {
        ArrayList<Element> optionalQuestionnaires = getProcessQuestionaires(optionalType);
        if (optionalQuestionnaires != null) {
          questionaires.addAll(optionalQuestionnaires);
        }
      }
    }

    if (questionaires.size() == 0) {
      logger.error(siteInfo.getIdString()+": getProcessessOrdered found no questionnaires for process type " + processName);
      return printStudies;
    }

    logger.debug(siteInfo.getIdString()+": There are " + questionaires.size() + " questionaires and " + studies.size()
        + " studies for processType " + processName);

    // For each questionaire
    for(Element questionaire : questionaires) {
      // Get the print order
      if (!questionaire.hasAttribute(Constants.XML_PROCESS_ORDER_PRINT)) {
        continue; // skip the questionnaire
      }
      String orderString = questionaire.getAttribute(Constants.XML_PROCESS_ORDER_PRINT);

      // Get the system name, study name and order
      String systemName = questionaire.getAttribute("type");
      String studyName = questionaire.getAttribute("value");
      if (systemName.equals("") || studyName.equals("")) {
        logger.debug(siteInfo.getIdString()+": Not including Questionaire in process " + processName
            + ". Missing type or value, type=" + systemName + ", value=" + studyName);
        continue; // skip the questionnaire
      }

      // Get study name and system name for RepeatingSurveyService
      if (systemName.startsWith("RepeatingSurveyService") && (systemName.length() > 23)) {
        studyName = systemName.substring(23);
        systemName = "RepeatingSurveyService";
      }

      // Get the survey system and study

      SurveySystem surveySystem = null;
      Study study = null;
      for (SurveySystem surveySystem1 : surveySystems) {
        if (surveySystem1 != null
            && systemName.toLowerCase().equals(surveySystem1.getSurveySystemName().toLowerCase())) {
          surveySystem = surveySystem1;
          study = getStudyForName(studies, surveySystem, studyName);
          if (study == null && studyName.contains("@")) {
            study = getStudyForName(studies, surveySystem, studyName.substring(0, studyName.indexOf("@")));

          }
        }
      }
      if (study == null) {
        logger.error("study null for " + studyName);
        if (studyName.contains("@")) {
          logger.error("study null for " + studyName.substring(0, studyName.indexOf("@")));
        }
        continue; // skip the questionaire
      }
      if (surveySystem == null) {
        logger.error(siteInfo.getIdString()+": surveySystem is null for " + systemName);
        continue; // skip the questionaire
      }

       // Don't want duplicates so lets check if we already have this study

      boolean found = false;
      for (PrintStudy process : printStudies) {
        if ((process.getStudyCode().intValue() == study.getStudyCode().intValue())
            && (surveySystem.getSurveySystemName().equals(process.getSurveySystemName()))) {
          found = true;
        }
      }

      if (found) {
        continue; // skip the questionaire
      }

      // Add a PrintStudy for the questionaire

      try {
        logger.debug(siteInfo.getIdString()+": Adding system " + systemName + "systemId: " + surveySystem.getSurveySystemId()
            + " study " + study.getStudyDescription() + " studyCode: " + study.getStudyCode());

        PrintStudy pStudy = new PrintStudy(siteInfo, study, surveySystem.getSurveySystemName());

        // Set the print order
        String printOrderValue[] = orderString.split("\\.");
        if (printOrderValue.length > 0) {
          try {
            pStudy.setPrintOrder(Integer.parseInt(printOrderValue[0]));
          } catch (NumberFormatException nfe) {
            logger.error(siteInfo.getIdString()+": Invalid print order value of " + orderString + " in questionaire " + studyName
                + " in in process " + processName);
            continue; // skip the questionaire
          }
        }
        if (printOrderValue.length > 1) {
          try {
            pStudy.setPrintOrderSub(Integer.parseInt(printOrderValue[1]));
          } catch (NumberFormatException nfe) {
            logger.error(siteInfo.getIdString()+": Invalid print order value of " + orderString + " in questionaire " + studyName
                + " in in process " + processName);
            continue; // skip the questionaire
          }
        }

        // Set the print types
        if (questionaire.hasAttribute(Constants.XML_PROCESS_PRINT_TYPE)) {
          String[] printTypeAttrib = questionaire.getAttribute(Constants.XML_PROCESS_PRINT_TYPE).split(",");
          pStudy.setPrintTypes(printTypeAttrib);
        }
        logger.debug(siteInfo.getIdString()+": study " + pStudy.getStudyCode() + ": " + pStudy.getStudyDescription()
            + " is print type chart:" + pStudy.hasPrintType(Constants.XML_PROCESS_PRINT_TYPE_CHART));

        // Set the print version for non-english questionnaires that have an english equivalant
        if (questionaire.hasAttribute(Constants.XML_PROCESS_PRINT_VERSION)) {
          String printVersion = questionaire.getAttribute(Constants.XML_PROCESS_PRINT_VERSION);
          if (printVersion != null && printVersion.trim().length() > 0) {
            pStudy.setPrintVersion(printVersion.trim());
          }
        }
        // Set invert
        if (questionaire.hasAttribute(Constants.XML_INVERT)) {
          String invertStr = questionaire.getAttribute(Constants.XML_INVERT);
          if (invertStr != null && "true".equals(invertStr.trim().toLowerCase())) {
            pStudy.setInvert(true);
          }
        }

        // Add the print study
        printStudies.add(pStudy);
      } catch (Exception de) {
        logger.error(siteInfo.getIdString()+": Error creating PrintStudy object for study " + study.getStudyDescription(), de);
      }
    }

    // Sort the print studies by print order
    Collections.sort(printStudies, new PrintStudyComparator());

    return printStudies;
  }

  private Study getStudyForName(ArrayList<Study> studies, SurveySystem surveySystem, String studyName) {
    // Get the study
    for (Study study1 : studies) {
      if (study1 != null
          && study1.getStudyDescription() != null
          && study1.getStudyDescription().toLowerCase().equals(studyName.toLowerCase())
          && study1.getSurveySystemId().intValue() == surveySystem.getSurveySystemId()
          .intValue()) {
        return study1;
      }
    }
    return null;
  }


  /**
   * Efficiently reads a stream to a string, preserving ends of lines so the resulting
   * string has the same size as the file, except for effects of multi-byte characters.
   */
  private String inputStreamToString(InputStream stream) throws IOException {
    BufferedReader bufferedReader = null;
    try {
      InputStreamReader streamReader = new InputStreamReader(stream,"UTF-8");
      bufferedReader = new BufferedReader(streamReader);
      char cbuf[] = new char[8192];  // a standard buffer size

      StringBuilder buffer = new StringBuilder();
      while (bufferedReader.ready()) {
        int n = bufferedReader.read(cbuf);
        buffer.append(cbuf, 0, n);
      }
      return buffer.toString();

    } finally {
      if (bufferedReader != null) {
        bufferedReader.close();
      }
    }
  }


  /**
   * Get the contents of the "file" from
   * <br>1. the XML directory
   * <br>2. the AppConfig table for the site
   * <br>3. the global AppConfig table.
   */
   public String getXML(Database database, String fileName) {
    StringBuilder sb = new StringBuilder();
    String contents = contentsCache.get(fileName);

    if (contents != null) {
      return contents;
    }

    InputStream stream = getXMLStream(fileName, sb);
    if (stream != null) {
      try {
        contents = inputStreamToString(stream);
        putInCache("getXMLStream", fileName, contents);
      } catch (Exception e) {
        logger.error("Error reading xml file "+sb.toString()+"; " + e.toString());
      }
      return contents;
    }

    // not found on the file system, look in the database
    AppConfigDao configDao = new AppConfigDao(database, ServerUtils.getAdminUser(database));
    // Survey content is always global, so far
    AppConfigEntry configEntry;
    String what = "getXML-fromDB/site";
    configEntry = configDao.findAppConfigEntry(siteInfo.getSiteId(), "surveycontent", fileName);
    if (configEntry == null) {
      what = "getXML-fromDB/global";
      configEntry = configDao.findAppConfigEntry(0L, "surveycontent", fileName);
    }
    if (configEntry != null) {
      contents = SurveyUtils.convertToXmlString(configEntry.getConfigValue());
      // found in the database, cache and return
      if (contents != null) {
        putInCache(what, fileName, contents);
        return contents;
      }
    }

    // not found in the database, give not found in file and database errors
    logger.error(sb.toString());
    logger.error("Could not find XML resource in the database: "+fileName);
    return contents;
  }

  /**
   * Get the contents of the file from the XML directory as a stream.
   * @param errOrName If return null, a message is put in here, else just the file or resource name
   */
  private InputStream getXMLStream(String fileName, StringBuilder errOrName) {

    if (fileName != null && !fileName.endsWith(".xml")) {
      fileName = fileName + ".xml";
    }
    InputStream inputStream;
    if (xmlPath != null) {
      File xmlDir = ServerUtils.getInstance().getDirectory(xmlPath);
      File xmlFile = new File(xmlDir, fileName);
      try {
        inputStream = new FileInputStream(xmlFile);
        errOrName.append("file: ").append(xmlFile.getPath());
        return inputStream;
      } catch(FileNotFoundException e) {
        errOrName.append(siteInfo.getIdString()).append(": getXML: Could not file XML file: ").append(xmlFile.getPath());
        return null;
      }
    }
    // get from resource path
    inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath + fileName);
    if (inputStream == null) {
      errOrName.append(siteInfo.getIdString()).append(": Could not find XML resource: ").append(resourcePath).append(fileName);
      logger.debug(errOrName.toString());
      return null;
    }

    errOrName.append("resource: ").append(resourcePath).append(fileName);
    return inputStream;
  }

// --Commented out 12/7/17, not used
//  /**
//   * Convenience method for server side document parsing
//   *
//   */
//  public static String convertXML(Document doc) throws TransformerException {
//    StreamResult result = new StreamResult(strWriter);
//    DOMSource source = new DOMSource(doc);
//    transfac.newTransformer().transform(source, result);
//    return strWriter.toString();
//  }
// --Commented out STOP

// --Commented out 12/7/17, not used
//  public static Document convertXML(String xmlString) throws SAXException, IOException, ParserConfigurationException {
//    DocumentBuilder db = dbf.newDocumentBuilder();
//    InputSource is = new InputSource();
//    is.setCharacterStream(new StringReader(xmlString));
//    return db.parse(is);
//  }
// --Commented out STOP

  /**
   * Run an XPath query on an XML document and return the result
   */
  public static String xPathQuery(String xmlDocument, String xpathQuery) {
    // Setup the XPath lookup
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlDocument));

    String value;
    try {
      // Run the xpath query
      value = xpath.evaluate(xpathQuery, is);
      // xpath returns an empty string if not found
      if ((value != null) && value.equals("")) {
        value = null;
      }
    } catch (XPathExpressionException e) {
      logger.error("Error doing XPath lookup, query="+xpathQuery, e);
      return null;
    }
    return value;
  }

  /**
   * Class to represent a ProcessType element
   */
  static private class ProcessType {
    final String processName;
    String surveyName = DEFAULT_SURVEY_NAME;
    Date startDate = null;
    Date expirationDate = null;
    int order = 0;
    String visitType = null;
    boolean hidden = false;
    String category = "patient";
    final HashMap<String,String> attributes;

    ProcessType(String processName, Element elem) {
      this(processName, elem, new HashMap<>());
    }

    void sayError(String processName, Element elem, String text, String other) {
      logger.error(String.format("For %s, element %s: %s %s", processName, elem.getTagName(), text, other));
    }

    ProcessType(String processName, Element elem, HashMap<String, String> attrs) {
      this.processName = processName;
      this.attributes = attrs;

      // Get the attributes
      NamedNodeMap nodeMap = elem.getAttributes();
      if (nodeMap != null) {
        for (int n = 0; n < nodeMap.getLength(); n++) {
          Node attribute = nodeMap.item(n);
          String name = attribute.getNodeName();
          String value = attribute.getNodeValue();
          attributes.put(name, value);
        }
      }

      String attString = attributes.get(ATTRIBUTE_SURVEY_NAME);
      if ((attString != null) && (attString.trim().length() > 0)) {
        surveyName = attString;
      }

      attString = attributes.get(ATTRIBUTE_VISIT_TYPE);
      if ((attString != null) && (attString.trim().length() > 0)) {
        visitType = attString;
      }

      attString = attributes.get(ATTRIBUTE_HIDDEN);
      if ((attString != null) && (attString.trim().length() > 0)) {
        hidden = Boolean.parseBoolean(attString);
      }

      attString = attributes.get(ATTRIBUTE_START_DT);
      if ((attString != null) && (attString.trim().length() > 0)) {
        try {
          startDate = dtFormat.parse(attString);
        } catch (ParseException pe) {
          sayError(processName, elem, "Parse exception reading attribute: ", ATTRIBUTE_START_DT);
        }
      }

      attString = attributes.get(ATTRIBUTE_EXPIRE_DT);
      if ((attString != null) && (attString.trim().length() > 0)) {
        try {
          expirationDate = dtFormat.parse(attString);
        } catch (ParseException pe) {
          sayError(processName, elem, "Parse exception reading attribute:", ATTRIBUTE_EXPIRE_DT);
        }
      }

      attString = attributes.get(ATTRIBUTE_ORDER);
      if ((attString != null) && (attString.trim().length() > 0)) {
        try {
          order = Integer.parseInt(attString);
        } catch (NumberFormatException nfe) {
          sayError(processName, elem, "Invalid numeric order value ", attString);
        }
      }

      attString = attributes.get(Constants.ATTRIBUTE_CATEGORY);
      if ((attString != null) && (attString.trim().length() > 0)) {
        category = attString;
      }
    }

    String getProcessName() {
      return processName;
    }
    String getSurveyName() {
      return surveyName;
    }
    Date getExpirationDate() {
      return expirationDate;
    }
    Date getStartDate() {
      return startDate;
    }
    int getOrder() {
      return order;
    }
    String getCategory() {
      return category;
    }
    boolean isActiveVisitProcess() {
      boolean activeVisitProcess =
          (visitType != null) &&
          ((expirationDate == null) || expirationDate.after(new Date())) &&
          (!hidden);
      return activeVisitProcess;
    }
    HashMap<String,String> getAttributes() {
      return attributes;
    }
  }

  /**
   * Comparator class for ProcessType
   */
  static private class ProcessTypeComparator implements Comparator<ProcessType> {
    final static int SORT_BY_ORDER = 0;
    final static int SORT_BY_NAME = 1;
    final static int SORT_BY_START_DATE = 2;
    final static int SORT_BY_EXPIRATION_DATE = 3;
    private int sortBy = 0;
    ProcessTypeComparator() {
      this(SORT_BY_ORDER);
    }
    ProcessTypeComparator(int sortOption) {
      sortBy = sortOption;
    }
    @Override
    public int compare(ProcessType p1, ProcessType p2) {
      if (p1 == null || p2 == null) {
        return 0;
      }
      switch (sortBy) {
      case SORT_BY_NAME:
        return byName(p1, p2);
      case SORT_BY_EXPIRATION_DATE:
        return byExpirationDate(p1, p2);
      case SORT_BY_START_DATE:
        return byStartDate(p1, p2);
      default:
        return byOrder(p1, p2);
      }
    }
    private int byName(ProcessType p1, ProcessType p2) {
      if (p1.getProcessName() == null || p2.getProcessName() == null) {
        return 0;
      }
      return p1.getProcessName().compareTo(p2.getProcessName());
    }
    private int byOrder(ProcessType p1, ProcessType p2) {
      if (p1.getOrder() > p2.getOrder()) {
        return 1;
      }
      if (p1.getOrder() < p2.getOrder()) {
        return -1;
      }
      return 0;
    }
    private int byStartDate(ProcessType p1, ProcessType p2) {
      if (p1.getStartDate() == null && p2.getStartDate() == null) {
        return 0;
      }
      if (p1.getStartDate() == null) {
        return -1;
      }
      if (p2.getStartDate() == null) {
        return 1;
      }
      if (p1.getStartDate().after(p2.getStartDate())) {
        return 1;
      } else if (p1.getStartDate().before(p2.getStartDate())) {
        return -1;
      }
      return 0;
    }
    private int byExpirationDate(ProcessType p1, ProcessType p2) {
      if (p1.getExpirationDate() == null && p2.getExpirationDate() == null) {
        return 0;
      }
      if (p1.getExpirationDate() == null) {
        return -1;
      }
      if (p2.getExpirationDate() == null) {
        return 1;
      }
      if (p1.getExpirationDate().after(p2.getExpirationDate())) {
        return 1;
      } else if (p1.getExpirationDate().before(p2.getExpirationDate())) {
        return -1;
      }
      return 0;
    }
  }

  /**
   * Class to represent a Media element
   */
  static private class Media {
    final String name;
    final String page;
    int order;
    final NodeList mediaAttribNodes;

    Media(Element elem) {
      name = elem.getAttribute("name");
      page = elem.getAttribute("page");
      order = 0;
      String orderString = elem.getAttribute(ATTRIBUTE_ORDER);
      if (orderString != null) {
        try {
          order = Integer.parseInt(orderString);
        } catch (NumberFormatException nfe) {
          logger.error("Invalid numeric order value '" + orderString + "' for mediaLink " + name);
        }
      }
      mediaAttribNodes = elem.getElementsByTagName("MediaAttribute");

    }

    String getName() {
      return name;
    }

    @SuppressWarnings("unused")
    public String getPage() {
      return page;
    }
    int getOrder() {
      return order;
    }

    HashMap<String, String> getMediaAttributes() {
      if (mediaAttribNodes != null) {
        HashMap<String, String> attributes = new HashMap<>();
        for (int p = 0; p < mediaAttribNodes.getLength(); p++) {
          Element patientA = (Element) mediaAttribNodes.item(p);
          attributes.put(patientA.getAttribute("data_name"), patientA.getAttribute("data_value"));
        }
        return attributes;
      }
      return null;
    }
  }

  /**
   * Comparator class for Media
   */
  static private class MediaComparator implements Comparator<Media> {
    final static int SORT_BY_ORDER = 0;
    final static int SORT_BY_NAME = 1;

    private int sortBy = 0;
    MediaComparator() {
      this(SORT_BY_ORDER);
    }
    MediaComparator(int sortOption) {
      sortBy = sortOption;
    }
    @Override
    public int compare(Media v1, Media v2) {
      if (v1 == null || v2 == null) {
        return 0;
      }
      switch (sortBy) {
      case SORT_BY_NAME:
        return Media(v1, v2);
      default:
        return byOrder(v1, v2);
      }
    }
    private int Media(Media v1, Media v2) {
      if (v1.getName() == null || v2.getName() == null) {
        return 0;
      }
      return v1.getName().compareTo(v2.getName());
    }
    private int byOrder(Media v1, Media v2) {
     return (Integer.valueOf(v1.getOrder())).compareTo(v2.getOrder());
    }

  }
}
