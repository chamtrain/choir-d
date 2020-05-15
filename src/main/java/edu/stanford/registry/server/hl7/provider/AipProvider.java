/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7.provider;

import edu.stanford.registry.server.hl7.TerserLocationIntf;
import edu.stanford.registry.server.hl7.TerserLocations;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.hl7v2.util.Terser;


public class AipProvider extends Provider {

  private static final List<String> alternateIdLocations = new ArrayList<>();


  public AipProvider(TerserLocationIntf terserLocations, Terser terser) throws Exception {
    alternateIdLocations.add(terserLocations.getLocation(TerserLocations.ATTENDING_PROVIDER_MSO));
    alternateIdLocations.add(terserLocations.getLocation(TerserLocations.ATTENDING_PROVIDER_ID));
    setFields(
      terser,
      terserLocations.getLocation(TerserLocations.AIP_PROVIDER_MSO),
        terserLocations.getLocation(TerserLocations.AIP_PROVIDER_ID),
      alternateIdLocations
    );
  }
}
