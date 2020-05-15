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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a copy of the HL7 project class for local testing
 * <code>PropUtil</code> is a helper class to manipulate properties.
 * See the <a href="{@docRoot}/copyright.txt">Copyright</a>.
 *
 * @author      Sanjay Malunjkar (smalunjk@stanford.edu)
 * @version     %I%, %G%
 * @since       2nd Nov 2009
 */
public class PropUtil {
  private static final Logger log = LoggerFactory.getLogger(PropUtil.class);

  public static Properties loadProperties(String fileName) {
    return loadProperties(fileName, true);
  }

  public static Properties loadProperties(String fileName, boolean mustExist) {
    File f = new File(fileName);
    FileInputStream fis = null;

    Properties props = new Properties();
    if (f.exists()) {
      try {
        fis = new FileInputStream(f);
        props.load(fis);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          if (fis != null) {
            fis.close();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    } else if (mustExist) {
      log.error("Error: " + fileName + " does not exist.");
      System.exit(1);
    }
    return props;
  }

  //save the properties file for later re-use.
  public static void saveProperties(Properties props, String fileName) {
    FileOutputStream fo = null;
    try {
      fo = new FileOutputStream(fileName);
      props.store(fo, "save properties for later use");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fo != null) {
          fo.close();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
