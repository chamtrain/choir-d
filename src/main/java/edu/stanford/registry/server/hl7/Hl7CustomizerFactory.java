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

import ca.uhn.hl7v2.hoh.api.IAuthorizationServerCallback;
import ca.uhn.hl7v2.hoh.auth.SingleCredentialServerCallback;

public class Hl7CustomizerFactory implements Hl7CustomizerFactoryIntf {

  public Hl7CustomizerFactory() {
  }

  /**
   * Returns an hl7 customizer for the site
   *
   * @return the Custom Hl7Customizer from the sites RegistryCustomizer class
   * Defaults to HL7Customizer.class
   */
  @Override
  public Hl7CustomizerIntf create(SiteInfo siteInfo) {
    if (siteInfo.getRegistryCustomizer() == null) {
      return new Hl7Customizer(siteInfo);
    }
    Hl7CustomizerIntf hl7CustomizerIntf = siteInfo.getRegistryCustomizer().getHl7Customizer();

    if (hl7CustomizerIntf == null) {
      hl7CustomizerIntf = new Hl7Customizer(siteInfo);
    }
    return hl7CustomizerIntf;

  }

  @Override
  public IAuthorizationServerCallback getServerCallback() {
    return new SingleCredentialServerCallback("someusername", "apassword");
  }

  @Override
  public String getDepartmentTerserLocation() {
    return "/.RGS-3-2";
  }

}

