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

import ca.uhn.hl7v2.util.Terser;

public class ProviderFinder {

  public static Provider getProviderId(TerserLocationIntf terserLocationIntf, Terser terser) throws Exception {

    if (Provider.findProvider(terser, terserLocationIntf.getLocation(TerserLocations.AIP_PROVIDER_MSO))) {
      return new AipProvider(terserLocationIntf, terser);
    } else if (Provider.findProvider(terser, terserLocationIntf.getLocation(TerserLocations.ATTENDING_PROVIDER_MSO))) {
      return new AttendingProvider(terserLocationIntf, terser);
    } else if (Provider.findProvider(terser, terserLocationIntf.getLocation(TerserLocations.REFERRING_PROVIDER_MSO))) {
      return new ReferringProvider(terserLocationIntf, terser);
    } else if (Provider.findProvider(terser, terserLocationIntf.getLocation(TerserLocations.ADMITTING_PROVIDER_MSO))) {
      return new AdmittingProvider(terserLocationIntf, terser);
    } else if (Provider.findProvider(terser, terserLocationIntf.getLocation(TerserLocations.CONSULTING_PROVIDER_MSO))) {
      return new ConsultingProvider(terserLocationIntf, terser);
    } else {
      // no physician info
      return null;
    }

  }
}
