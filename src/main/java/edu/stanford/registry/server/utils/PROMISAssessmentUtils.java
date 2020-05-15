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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.PromisAnswer;
import edu.stanford.registry.shared.survey.PromisQuestion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.github.susom.database.Database;

public class PROMISAssessmentUtils {
  public static final String DESCRIPTION = "Description";
  public static final String[] SURVEY_SYSTEM_NAME = { "0", "PROMIS", "PROMIS.2" };

  private static Logger logger = Logger.getLogger(PROMISAssessmentUtils.class);
  private SurveySystem surveySystem = null;
  protected final SiteInfo siteInfo;
  protected final Long siteId;
  

  private static final String encodeScheme = "UTF-8";
  Database database = null;
  int version = 1;

  public PROMISAssessmentUtils(Database db, int version, SiteInfo siteInfo) {
    database = db;
    this.version = version;
    this.siteInfo = siteInfo;
    siteId = siteInfo.getSiteId();
    init();
  }

  private void init() {
    SurveySystDao ssDao = new SurveySystDao(database);
    surveySystem = ssDao.getSurveySystem(SURVEY_SYSTEM_NAME[version]);
  }

  public Properties getHttpHeaders(int version) {
    Properties props = new Properties();

    String value = siteInfo.getProperty("promis.2.registrationOID") + ":"
        + siteInfo.getProperty("promis.2.token");
    logger.debug("sending authorization: " + value);
    String encodedValue;
    try {
      encodedValue = new String(Base64.encodeBase64(value.getBytes(StandardCharsets.UTF_8)), encodeScheme);
      props.put("Authorization", "Basic " + encodedValue.toString());
    } catch (UnsupportedEncodingException e) {
      logger.error("Base 64 encoding failed", e);
    }
    return props;
  }

  /**
   * Calls the PROMIS service to get the list of available assessments
   */
  public String getBanks() throws ServiceUnavailableException, FileNotFoundException {
    return getXML(-1, PROMISProperties.getBankUrl(), "POST", null); // noheaders
  }

  public String getBanks(int version) throws ServiceUnavailableException, FileNotFoundException {

    return getXML(-1, PROMISProperties.getBankUrl(siteInfo, version), "POST", getHttpHeaders(version));
  }

  /**
   * Calls the PROMIS service to register a form
   */

  public String registerAssessment(int version, Element questionaire, String token)
      throws ServiceUnavailableException, FileNotFoundException, UnsupportedEncodingException {
    String formName = questionaire.getAttribute("value");
    logger.debug("registerAssessment: " + formName + " version " + version + " token " + token);
    formName = URLEncoder.encode(formName, encodeScheme);
    String uid = URLEncoder.encode(token.toString(), encodeScheme);
    switch (version) {
    case 2:
      String oid = questionaire.getAttribute("OID");
      if (oid == null) {
        throw new ServiceUnavailableException("OID is missing for version 2 promis assessment " + formName
            + " not adding assessment for token " + token);
      }
      AssessDao assessDao = new AssessDao(database, siteInfo);
      SurveyRegistration survey = assessDao.getRegistration(token);
      String UID = "UID=" + token; // Optional
      // User-defined id
      int expireDays = DateUtils.getDaysAway(siteInfo, survey.getSurveyDt()) + 1;
      String expiration = "Expiration=" + expireDays;
      String urlString = PROMISProperties.getRegisterAssessmentsUrl(siteInfo, version, oid, uid) + "?" + UID + "&" + expiration;
      return getXML(0, urlString, "GET", getHttpHeaders(version));
    default:
      return getXML(0, PROMISProperties.getRegisterAssessmentsUrl(siteInfo, version, formName, uid), "POST", null);
    }
  }

  public String administerAssessment(Integer studyCode, int version, String assessmentName, String oid, String resp)
      throws ServiceUnavailableException, FileNotFoundException {
    try {
      assessmentName = URLEncoder.encode(assessmentName, encodeScheme);
    } catch (Exception e) {
      logger.error("Problem urlencoding the assessmentname: " + e.toString(), e);
    }

    StringBuilder url = new StringBuilder(PROMISProperties.getAdministerAssessmentUrl(siteInfo, version, assessmentName));
    if (oid != null && resp != null) {
      url.append("?ItemResponseOID=").append(oid).append("&Response=").append(resp);
    }
    Properties headers = null;
    if (version == 2) {
      headers = getHttpHeaders(version);
    }
    return getXML(studyCode, url.toString(), "GET", headers);
  }

  // public String getXML(String urlString, String requestType) throws
  // ServiceUnavailableException, FileNotFoundException {
  // return getXML(urlString, requestType, null, null);
  // }

  public PromisQuestion getQuestion(String xmlString, PatientStudy patStudy, boolean allAnswers) {

    PromisQuestion question = new PromisQuestion();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {

      // Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlString));

      Document messageDom = db.parse(is);
      Element docElement = messageDom.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {
        if (version == 2) {
          String finished = docElement.getAttribute("DateFinished");
          if (finished != null && finished.length() > 0) {
            // question.setAnswered(true);
          }
        }
        Element itemNode = null;
        NodeList itemsNodes = messageDom.getElementsByTagName("Items");
        if (itemsNodes != null && itemsNodes.getLength() > 0) {
          itemNode = (Element) itemsNodes.item(0);
        } else {
          NodeList itemNodes = messageDom.getElementsByTagName("Item");
          if (itemNodes != null && itemNodes.getLength() > 0) {
            itemNode = (Element) itemNodes.item(0);
          }
        }
        if (itemNode == null) {
          return question;
        }
        logger.debug(xmlString);
        question = getQuestion(itemNode, allAnswers);

      } else {
        AssessDao assessDao = new AssessDao(database, siteInfo);
        SurveyRegistration survey = assessDao.getRegistration(patStudy.getToken());
        StringBuilder buf = new StringBuilder();
        // let see if it's expired
        int expireDays = DateUtils.getDaysAway(siteInfo, survey.getSurveyDt()) + 1;
        if (expireDays < 0) {
          question.addQuestionLine(" ");
          buf.append("This survey has expired.");
        } else {
          question.addQuestionLine("We're sorry, a problem has occurred.");
          if (docElement.getTagName().equals("Error")) { // end if Form found
            NodeList nodes = docElement.getChildNodes();
            if (nodes != null && nodes.getLength() > 0) {
              for (int n = 0; n < nodes.getLength(); n++) {
                buf.append(nodes.item(n).getNodeValue());
              }
            }
          }
        }
        PromisAnswer answer = new PromisAnswer();
        answer.addText(buf.toString());
        answer.setAttribute("ItemResponseOID", "ERROR");
        answer.setAttribute("Value", buf.toString());
        answer.setSelected(true);
        question.addAnswer(answer);
      }
    } catch (Exception e) {
      logger.error(e.toString(), e);
    }

    return question;
  }

  public PromisQuestion getQuestion(Element itemNode, boolean allResponses) {

    return getQuestion(itemNode, version, true);
  }

  public static PromisQuestion getQuestion(Element itemNode, int vs, boolean allResponses) {
    logger.debug("getQuestion starting for vs " + vs + " with " + allResponses);
    PromisQuestion question = new PromisQuestion();
    // If there is a response then the survey is done!
    String response = itemNode.getAttribute("Response");
    if (response != null && response.trim().length() > 0) {
      logger.debug("Marking question complete");
      question.setAnswered(true);
    }
    NodeList elementList;
    NodeList elementsNodes = itemNode.getElementsByTagName("Elements");
    if (elementsNodes != null && elementsNodes.getLength() > 0) {
      Element elementsNode = (Element) elementsNodes.item(0);
      elementList = elementsNode.getElementsByTagName("Element");
    } else {
      elementList = itemNode.getElementsByTagName("Element");
    }

    for (int elementInx = 0; elementInx < elementList.getLength(); elementInx++) { // Element
      Element el = (Element) elementList.item(elementInx);
      String elementDescription = el.getAttribute(DESCRIPTION);

      if (elementDescription != null && elementDescription.startsWith("ContainerFor")) {
        NodeList mapList = null;
        NodeList mappingsNodes = el.getElementsByTagName("Mappings");
        if (mappingsNodes != null && mappingsNodes.getLength() > 0) {
          Element mappings = (Element) mappingsNodes.item(0);
          // Element mappings = (Element)
          // el.getElementsByTagName("Mappings").item(0);
          mapList = mappings.getElementsByTagName("Map");
        } else {
          mapList = el.getElementsByTagName("Map");
        }
        for (int mapInx = 0; mapInx < mapList.getLength(); mapInx++) { // Map

          Element map = (Element) mapList.item(mapInx);
          String oid = map.getAttribute("ItemResponseOID");
          String val = map.getAttribute("Value");
          String ansText = map.getAttribute(DESCRIPTION);
          if (map.hasChildNodes()) {
            NodeList resourcesNodes = map.getElementsByTagName("Resources");
            if (resourcesNodes != null && resourcesNodes.getLength() > 0) {
              Element resources = (Element) resourcesNodes.item(0);
              Element resource = (Element) resources.getElementsByTagName("Resource").item(0);
              if (resource.hasAttribute(DESCRIPTION)) {
                ansText = resource.getAttribute(DESCRIPTION);
              }
            }
          }

          PromisAnswer answer = new PromisAnswer();
          answer.addText(ansText);
          answer.setAttribute("ItemResponseOID", oid);
          answer.setAttribute("Value", val);
          if (oid != null && oid.length() > 0 && oid.equals(itemNode.getAttribute("ItemResponseOID"))) {
            answer.setSelected(true);
            question.setAnswered(true);
            question.addAnswer(answer);
          } else if (allResponses) {
            question.addAnswer(answer);
          }
          logger.debug("Promis answer value = " + val + " description = " + ansText + " selected = "
              + answer.getSelected());
        } // for each Map
      } else { // its the question
        logger.debug("Promis question = " + elementDescription);
        question.addQuestionLine(elementDescription);
        question.setId(itemNode.getAttribute("Position"));
      }
    }

    // Mark the response
    return question;
  }

  public String getXML(Integer studyCode, String urlString, String requestType, Properties headers) throws ServiceUnavailableException,
      FileNotFoundException {
    Metric metric = new Metric(logger.isDebugEnabled());

    StringBuilder result = new StringBuilder();
    String returnString = null;
    if (urlString == null || urlString.length() < 1 || "nullForms/.xml".equals(urlString)) {
      throw new ServiceUnavailableException("Cannot retrieve the list. 'promis.2.url' parameter is undefined");
    }
    
    try {
      logger.debug("called with " + urlString);
      URL u = new URL(urlString.replace("+", "%20"));
      String proxyHost = siteInfo.getProperty("proxyHost");
      String proxyPort = siteInfo.getProperty("proxyPort");
      HttpURLConnection uc;
      if (proxyHost != null && proxyHost.length() > 0 && proxyPort != null && proxyPort.length() > 0) {
        logger.debug("making httpurlconnection with proxy : " + proxyHost + ":" + proxyPort);
        int port = 80;
        try {
          port = Integer.parseInt(proxyPort);
        } catch (Exception e) {
          logger.error("proxy port " + proxyPort + " is not a valid port #, using 80");
        }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
        uc = (HttpURLConnection) u.openConnection(proxy);
      } else {
        logger.debug("making direct httpurlconnection (no proxyHost/proxyPort configured");
        uc = (HttpURLConnection) u.openConnection();
      }
      uc.setRequestMethod(requestType);
      uc.setRequestProperty("Content-type", "text/xml; charset=utf-8");
      uc.setRequestProperty("Content-Language", "en-US");

      if (headers != null) {
        Enumeration<?> headerNames = headers.propertyNames();
        if (headerNames != null) {
          while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement().toString();
            logger.debug("adding header \"" + headerName + "\", \"" + headers.getProperty(headerName) + "\"");
            uc.setRequestProperty(headerName, headers.getProperty(headerName));
          }
        }
      }

      uc.setUseCaches(false);
      uc.setDoOutput(true);
      uc.setDoInput(true);
      uc.connect();
      metric.checkpoint("connect");
      // write parameters

      java.io.OutputStream reqStream = uc.getOutputStream();
      reqStream.flush();

      InputStreamReader isr = new InputStreamReader(uc.getInputStream(), encodeScheme);
      logger.debug("input stream encoding is " + isr.getEncoding());
      metric.checkpoint("request");
      // get the response back
      BufferedReader response = new BufferedReader(isr);
      String line;

      while ((line = response.readLine()) != null) {
        result.append(URLDecoder.decode(line, encodeScheme));
      }
      response.close();
      logger.debug("RESPONSE:" + result.toString());
      metric.done("response");
      // clean off the junk at the beginning and send only the valid xml
      int beginAt = result.indexOf("<");
      int endAt = result.lastIndexOf(">") + 1;
      if (beginAt >= 0 && endAt <= result.toString().length()) {
        returnString = result.toString().substring(beginAt, endAt);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("HTTP: " + metric.getMessage() + " study=" + studyCode + " URL= " + urlString);
      }
    } catch (SocketException se) {
      logger.error("Socket exception! ", se);
      throw new ServiceUnavailableException("There are unexplained network problems. Please try again later.");
    } catch (FileNotFoundException fnf) {
      logger.error("File not found exception for " + fnf.getMessage(), fnf);
      throw new FileNotFoundException("No more questions for that uid");
    } catch (Exception e) {
      logger.error("Caught exception making http call", e);
      throw new ServiceUnavailableException("Network error. Please try again.");
    }
    return returnString;
  }

  /**
   * Get the internal id for the promis survey system
   *
   * @return
   */
  public int getSystemId() {
    if (surveySystem == null) {
      return 0;
    }
    return surveySystem.getSurveySystemId();
  }

  public boolean checkComplete(int version, PatientStudy patStudy, String xmlDocumentString, String oid) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    boolean isComplete = false;
    try {

      // Using factory get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xmlDocumentString));

      Document messageDom = db.parse(is);
      Element docElement = messageDom.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {
        if (version == 2) {
          String finished = docElement.getAttribute("DateFinished");
          if (finished != null && finished.length() > 0) {
            // get the contents
            logger.debug("Survey is complete, getting /Results/");
            isComplete = true;
            String url = PROMISProperties.getResultsUrl(siteInfo, version, oid);
            String results = getXML(patStudy.getStudyCode(), url, "GET", getHttpHeaders(version));
            xmlDocumentString = StringUtils.cleanXmlString(results);
          }
        } else {
          NodeList itemList = null;

          NodeList itemsNodes = messageDom.getElementsByTagName("Items");
          if (itemsNodes != null && itemsNodes.getLength() > 0) {
            Element itemsNode = (Element) messageDom.getElementsByTagName("Items").item(0);
            itemList = itemsNode.getElementsByTagName("Item");
          } else {
            itemList = messageDom.getElementsByTagName("Item");
          }
          if (itemList != null) {
            for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {

              Element itemNode = (Element) itemList.item(itemInx);

              // If there is a response then the survey is done!
              String itemResponse = itemNode.getAttribute("Response");
              if (itemResponse != null && itemResponse.trim().length() > 0) {
                logger.debug("Survey is complete and should be saved");
                isComplete = true;
              }
            } // end of the list of items
          }
        }
      } // end if Form found
    } catch (Exception e) {
      logger.error(e.toString(), e);
    }

    // if not completed or the dt changed is not null (already saved) then
    // we're done
    if (!isComplete || patStudy.getDtChanged() != null) {
      return false;
    }

    Metric metric = new Metric(logger.isDebugEnabled());
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    patStudy = patStudyDao.setPatientStudyContents(patStudy, xmlDocumentString);
    metric.done("dbwrite");
    return true;
  }

  public static NodeList getDocumentItems(String xmlContents) throws Exception {
    xmlContents = StringUtils.cleanXmlString(xmlContents);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // Using factory get an instance of document builder
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlContents));

    Document messageDom = db.parse(is);
    Element docElement = messageDom.getDocumentElement();

    if (docElement.getTagName().equals("Form")) {
      NodeList itemList = null;
      NodeList itemsList = messageDom.getElementsByTagName("Items");
      if (itemsList != null && itemsList.getLength() > 0) {
        itemList = ((Element) itemsList.item(0)).getElementsByTagName("Item");
      } else {
        itemList = messageDom.getElementsByTagName("Item");
      }
      return itemList;
    } else {
      return null;
    }
  }

}
