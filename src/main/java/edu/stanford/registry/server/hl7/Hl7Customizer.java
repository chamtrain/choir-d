/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

public class Hl7Customizer implements Hl7CustomizerIntf {
  private static final SimpleDateFormat msgDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
  private final static Logger logger = LoggerFactory.getLogger(Hl7Customizer.class);
  private final SiteInfo siteInfo;
  private Hl7Appointment hl7Appointment;

  public Hl7Customizer(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * Get the class used for locating the data in the hl7 message
   *
   * @return Returns the default TerserLocationIntf implementation
   */
  @Override
  public TerserLocationIntf getTerserLocations() {
    return new DefaultTerserLocations();
  }

  /**
   * Create an acknowlegement message to be returned to the sender
   *
   * @param messageTerser Terser for the incoming message
   * @return ACK return message
   * @throws HL7Exception thrown when there is a parsing problem
   */
  @Override
  public Message getScheduleAckMessage(TerserLocationIntf terserLocations, Terser messageTerser) throws HL7Exception {
    String msgId = "0";
    String procId = "";
    String vs = "";
    String sendApp = "epic";
    if (terserLocations != null) {
      try {
        sendApp = messageTerser.get(terserLocations.getLocation(TerserLocations.SENDING_APPLICATION));
        msgId = messageTerser.get(terserLocations.getLocation(TerserLocations.MESSAGE_CONTROL_ID));
        procId = messageTerser.get(terserLocations.getLocation(TerserLocations.MESSAGE_PROCESSING_ID));
        vs = messageTerser.get(terserLocations.getLocation(TerserLocations.HL7_VERSION_ID));
      } catch (HL7Exception hl7e) {
        logger.warn("error creating return message", hl7e.toString());
      }
    }

    Date now = new Date();
    String messageString =
        "MSH|^~\\&|" + sendApp + "|SHC|CHOIR|Hl7HohServlet|" +
            msgDateFormatter.format(now) +
            "||ACK|" + msgId + "|" + procId + "|" +
            vs + "||||||\r\n" + "MSA|AA|" + msgId + "|Success||";

    PipeParser ourPipeParser = new PipeParser();
    return ourPipeParser.parse(messageString);
  }

  /**
   * Returns the default HL7Appointment implementation
   */
  @Override
  public Hl7AppointmentIntf getHl7Appointment(Database database) {
    if (hl7Appointment == null) {
      hl7Appointment = new Hl7Appointment(database, siteInfo);
    }
    return hl7Appointment;
  }
}
