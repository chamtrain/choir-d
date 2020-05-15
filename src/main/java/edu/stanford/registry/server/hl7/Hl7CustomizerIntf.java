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

import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;

import com.github.susom.database.Database;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public interface Hl7CustomizerIntf {

  /**
   * Use to customize any of the hl7 content locations
   */
  TerserLocationIntf getTerserLocations();

  /**
   * Customize the acknowledgement message
   */
  Message getScheduleAckMessage(TerserLocationIntf terserLocations, Terser messageTerser) throws HL7Exception;

  /**
   * Return a custom appointment data handler
   */
  Hl7AppointmentIntf getHl7Appointment(Database database);
}
