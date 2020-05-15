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

package edu.stanford.registry.server.plugin;

import edu.stanford.registry.server.utils.StringUtils;
import edu.stanford.registry.shared.PatientStudyExtendedData;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ScoreService {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ScoreService.class);

  public static Document getDocument(PatientStudyExtendedData patientData) throws ParserConfigurationException,
      SAXException, IOException {
    if (patientData == null || patientData.getContents() == null) {
      return null;
    }
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    // Using factory get an instance of document builder
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();

    // first clean the xml to take care of any html embedded in quoted strings
    is.setCharacterStream(new StringReader(StringUtils.cleanXmlString(patientData.getContents())));

    return db.parse(is);
  }
}