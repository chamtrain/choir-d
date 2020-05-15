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

import edu.stanford.registry.shared.survey.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlFormatter {

  public static String format(Document doc) throws TransformerException {
    DOMSource source = new DOMSource(doc);

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    StreamResult streamResult = new StreamResult(new StringWriter());
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, streamResult);
    String xmlFormattedString = streamResult.getWriter().toString();
    return xmlFormattedString;
  }

  public static String format(String xmlString) throws TransformerFactoryConfigurationError, TransformerException {
    StreamSource streamSource = new StreamSource(new StringReader(xmlString));

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    StreamResult streamResult = new StreamResult(new StringWriter());
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(streamSource, streamResult);
    String xmlFormattedString = streamResult.getWriter().toString();
    return xmlFormattedString;
  }

  public static String format(Element element) throws TransformerFactoryConfigurationError, TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    StreamResult result = new StreamResult(new StringWriter());
    DOMSource source = new DOMSource(element);
    transformer.transform(source, result);

    return result.getWriter().toString();

  }

  public static NodeList getNodeList(String xmlData, String nodeName) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    db = dbf.newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(xmlData));
    Document messageDom = db.parse(is);
    Element docElement = messageDom.getDocumentElement();
    if (docElement.getTagName().equals(Constants.FORM)) {
      return docElement.getElementsByTagName(nodeName);
    }
    return null;
  }
}
