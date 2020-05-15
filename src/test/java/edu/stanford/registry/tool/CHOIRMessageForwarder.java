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
package edu.stanford.registry.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a copy of the HL7 project's MessageForwarder for local testing
 * @author Sanjay Malunjkar (smalunjk@stanford.edu)
 *
 * modified for CHOIR
 * @since 09/2019 tpacht@stanford.edu
 */
public class CHOIRMessageForwarder {
  private static final Logger logger = LoggerFactory.getLogger(CHOIRMessageForwarder.class);
  private static final String BLACK_LIST = "ignore_mrn_file";
  private static final String PROPS = "message.properties";


  private static void ensureExists(Properties p, String propertyName) {
    if (!p.containsKey(propertyName)) {
      logger.error("Error: " + propertyName, " is not defined.");
      System.exit(1);
    }
  }

  public static void main(String[] args) {
    Properties execProps;
    try {
      execProps = PropUtil.loadProperties(System.getProperty("prop.location", PROPS), true);//Hl7Utils.loadProperties( System.getProperty("prop.location", PROPS));
      ensureExists(execProps, BLACK_LIST);

      List<CHOIRhl7FileGobbler> workers = new ArrayList<>();

      for (int i = 0; i < 10; i++) {
        String feed = "feed_" + i;
        String fileName = feed + "_file_name";
        String locationFileName = feed + "_file_location";
        String retries = feed + "_max_retries";
        String ports = feed + "_forwarder_ports";
        String ips = feed + "_forwarder_ips";
        String paths = feed + "_forwarder_path";
        String filterSegmentName = feed + "_filter_name";
        String filterSegmentValue = feed + "_filter_value";

        if (execProps.containsKey(fileName)) {
          ensureExists(execProps, locationFileName);
          ensureExists(execProps, ips);
          ensureExists(execProps, ports);
          ensureExists(execProps, paths);
          ensureExists(execProps, retries);


          if (execProps.containsKey(filterSegmentName)) {
            ensureExists(execProps, filterSegmentValue);
          }

          CHOIRhl7FileGobbler rdr = new CHOIRhl7FileGobbler(execProps.getProperty(fileName),
              execProps.getProperty(locationFileName), execProps.getProperty(BLACK_LIST), execProps.getProperty(ips),
              execProps.getProperty(ports), execProps.getProperty(paths), Integer.parseInt(execProps.getProperty(retries)),
              execProps.getProperty(filterSegmentName), execProps.getProperty(filterSegmentValue));

          rdr.start();
          workers.add(rdr);
        }
      }

      for (CHOIRhl7FileGobbler rdr : workers) {
        rdr.join();
      }
    } catch (Exception e) {
      logger.error("Error forwarding messages", e);
    }
    logger.info("CHOIRMessageForwarder terminating");
  }
}

