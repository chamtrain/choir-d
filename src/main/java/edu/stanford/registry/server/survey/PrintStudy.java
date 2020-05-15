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
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintStudy extends Study {
  private static final long serialVersionUID = 1L;

  private int printOrder = 0;
  private int printSub = 0;
  private int[] printTypes = { Constants.XML_PROCESS_PRINT_TYPE_CHART };
  private String surveySystemName;
  private boolean invert = false;
  private String printVersion = null;
  private static final Logger logger = LoggerFactory.getLogger(PrintStudy.class);
  final SiteInfo siteInfo;

  public PrintStudy(SiteInfo siteInfo, Study study, String surveySystemName) throws DataException, InvalidDataElementException {
    this.siteInfo = siteInfo;
    this.setData(study.getData(siteInfo.getDateFormatter()));
    this.surveySystemName = surveySystemName;
  }

  public void setPrintOrder(int printOrder) {
    this.printOrder = printOrder;
  }

  public void setPrintOrderSub(int printSub) {
    this.printSub = printSub;
  }

  public int getPrintOrder() {
    return printOrder;
  }

  public int getPrintOrderSub() {
    return printSub;
  }

  public void setPrintTypes(int[] printTypes) {
    this.printTypes = printTypes;
  }

  public void setPrintTypes(String[] printTypeStr) {
    if (printTypeStr == null || printTypeStr.length == 0) {
      return;
    }
    printTypes = new int[printTypeStr.length];
    for (int inx = 0; inx < printTypeStr.length; inx++) {
      int pType = 0;
      if (printTypeStr[inx] != null) {
        for (int i = 0; i < Constants.XML_PROCESS_PRINT_TYPES.length; i++) {
          if (Constants.XML_PROCESS_PRINT_TYPES[i].equals(printTypeStr[inx].trim().toLowerCase())) {
            pType = i;
          }
        }
      }
      printTypes[inx] = pType;
    }
  }

  public boolean hasPrintType(int typ) {
    if (printTypes == null || printTypes.length == 0) {
      printTypes = new int[1];
      printTypes[0] = Constants.XML_PROCESS_PRINT_TYPE_CHART;
    }

    for (int printType : printTypes) {
      if (printType == typ) {
        return true;
      }

    }
    return false;
  }

  public void setPrintVersion(String printVersion) {
    this.printVersion = printVersion;
  }

  public String getPrintVersion() {
    if (this.printVersion == null) {
      return getStudyDescription();
    }
    return this.printVersion;
  }

  public String getSurveySystemName() {
    return surveySystemName;
  }

  public boolean getInvert() {
    return invert;
  }

  public void setInvert(boolean invert) {
    this.invert = invert;
  }

  @Override
  public PrintStudy clone() {
    Study newStudy = new Study();
    PrintStudy newPrintStudy = null;
    try {
      newStudy.setData(this.getData(siteInfo.getDateFormatter()));
    } catch (DataException e) {
      logger.debug("Failed to set study data from getData. DataException error {}", e.getMessage(), e);
    } catch (InvalidDataElementException e) {
      logger.debug("Failed to set study data from getData. InvalidDataElementException {}", e.getMessage(), e);
    }
    try {
      newPrintStudy = new PrintStudy(siteInfo, newStudy, this.getSurveySystemName());
      newPrintStudy.setPrintOrder(this.getPrintOrder());
      newPrintStudy.setPrintOrderSub(this.getPrintOrderSub());
      newPrintStudy.setPrintTypes(this.printTypes);
      newPrintStudy.setInvert(this.getInvert());
      newPrintStudy.setPrintVersion(this.printVersion);
    } catch (DataException e) {
      logger.debug("Failed to create new PrintStudy. DataException error {}", e.getMessage(), e);
    } catch (InvalidDataElementException e) {
      logger.debug("Failed to create new PrintStudy. InvalidDataElementException error {}", e.getMessage(), e);
    }
    return newPrintStudy;
  }
}
