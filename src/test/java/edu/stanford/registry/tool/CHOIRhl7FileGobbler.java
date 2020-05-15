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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.hoh.api.IAuthorizationClientCallback;
import ca.uhn.hl7v2.hoh.api.IReceivable;
import ca.uhn.hl7v2.hoh.auth.SingleCredentialClientCallback;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.hoh.llp.Hl7OverHttpLowerLayerProtocol;
import ca.uhn.hl7v2.hoh.util.ServerRoleEnum;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

/**
 * This is a copy of the HL7 project's HL7FileGobbler for local testing
 * @author  Sanjay Malunjkar (smalunjk@stanford.edu)
 *
 * Modified to use HAPI HL7 over HTTP (HoH) to send the hl7 messages to a CHOIR servlet
 * @since 09/2019 tpacht@stanford.edu
 */
public class CHOIRhl7FileGobbler extends Thread {
  private static final String FILE_PTR = "file.pointer";

  private static final Logger logger = LoggerFactory.getLogger(CHOIRhl7FileGobbler.class);
  private Properties ptrProps;
  private String ipFileName;
  private String locationPtr;
  private String filterSegmentName, filterSegmentValue;
  private String ignoreMrnListFile;
  private ArrayList<String> ignoreMrnList;
  private long ignoreMrnListFileModificationTime = 0;
  private HapiContext context = null;
  private PipeParser pp = null;
  private HohClientSimple[] clients;
  private Parser[] parsers;
  private String[] portList;
  private String[] ipList;
  private String[] pathList;
  private int[] retries;
  private int maxTries = 100;

  private boolean stop = false;

  /**
   * Class constructor.
   *
   * @param ipFileName
   *            input filename
   * @param locationPtr
   *            name of the file that stores last processed byte number in the
   *            input file
   * @param ignoreMrnListFile
   *            name of the files containing MRNs to NOT alert on.
   * @param ips
   *            comma separated list of IP addresses to forward the messages to
   * @param ports
   *            comma separated list of ports to forward the messages to.
   * @param paths
   *            comma separated list of servlet paths to forward the messages to.
   * @param maxTries
   *            maximum number of times it retries to send a message before giving up.
   * @param filterSegmentName
   *            name of the hl7 segment to send (restricts to only this type)
   * @param filterSegmentValue
   *            value of the hl7 segment
   */
   CHOIRhl7FileGobbler(String ipFileName, String locationPtr, String ignoreMrnListFile, String ips, String ports,
                       String paths, int maxTries, String filterSegmentName, String filterSegmentValue) throws Exception {
    this(ipFileName, locationPtr, ignoreMrnListFile);
    this.maxTries = maxTries;
    this.filterSegmentName = filterSegmentName;
    this.filterSegmentValue = filterSegmentValue;

    portList = ports.split("\\s*,\\s*");
    ipList = ips.split("\\s*,\\s*");
    pathList = paths.split("\\s*,\\s*");
    if (portList.length != ipList.length) {
      throw new Exception("Number of ips and ports don't match, number of ips=" + ipList.length + " , number of ports="
          + portList.length);
    }
    // Create a HapiContext
    this.context = new DefaultHapiContext();
    this.pp = this.context.getPipeParser();
    this.context.getParserConfiguration().setValidating(false);

    /*
     * Change to use Hl7 over http
     */
    LowerLayerProtocol llp = new Hl7OverHttpLowerLayerProtocol(ServerRoleEnum.CLIENT);
    context.setLowerLayerProtocol(llp);

    // Open clients to connect to remote HL7 receivers
    this.clients = new HohClientSimple[ipList.length];
    this.parsers = new Parser[ipList.length];
    this.retries = new int[ipList.length];
    for (int i = 0; i < ipList.length; i++) {
      this.parsers[i] = PipeParser.getInstanceWithNoValidation();
      this.clients[i] = new HohClientSimple(ipList[i], Integer.parseInt( portList[i]), pathList[i], parsers[i]);
      IAuthorizationClientCallback authCalback = new SingleCredentialClientCallback("someusername", "apassword");
      this.clients[i].setAuthorizationCallback(authCalback);
      this.retries[i] = 0;
    }
  }

  public CHOIRhl7FileGobbler(String ipFileName, String locationPtr, String ignoreMrnListFile) throws Exception {
    this.ignoreMrnListFile = ignoreMrnListFile;
    initBlacklist();
    this.ipFileName = ipFileName;
    this.locationPtr = locationPtr;
    this.ptrProps =  PropUtil.loadProperties(locationPtr, false);
  }

  public void run() {
    File f = new File(this.ipFileName);
    long filePointer = 0;

    //Start to read the feed from saved location(if available)
    if (ptrProps.containsKey(FILE_PTR)) {
      filePointer = Long.parseLong((String) ptrProps.get(FILE_PTR));
    }
    logger.info("Started processing file " + f.getAbsolutePath() + " at byte:" + filePointer);

    try {
      StringBuffer buf = new StringBuffer();
      char[] tbuf = new char[100000];
      int rc;
      char prev = ' ';

      //Stop forwarding when all connections/remote HL7 servers are down.
      while (!this.stop) {
        Thread.sleep(1000);
        long len = f.length();
        if (len < filePointer) {
          logger.info("Log file was reset. Restarting logging from start of file.");
          filePointer = 0;
        }

        BufferedReader console = new BufferedReader(new FileReader(f));
        console.skip(filePointer);

        //Read from HL7 file until there are no more bytes
        while (!stop && ((rc = console.read(tbuf)) != -1)) {

          //Process chars from buffer just read until there are none left in the buffer
          for (int c = 0; !stop && c < rc; c++) {
            String orgStr = "UNSET";

            //Found the end of HL7 message (\r\n)
            if (tbuf[c] == 0x0a && prev == 0x0d) {
              buf.append(tbuf[c]);
              try {
                orgStr = buf.toString();
                boolean hasNulls = false;

                //Some messages have illegible characters that mess up pipe parser. ignore'em.
                for (int j = 0; j < orgStr.length(); j++) {
                  if (orgStr.charAt(j) == 0) {
                    hasNulls = true;
                  }
                }

                if (!hasNulls && !isMt(orgStr)) {
                  //log.info("Hl7msg=" + orgStr);
                  Message next = this.pp.parse(orgStr);
                  Terser tmsg = new Terser(next);
                  String msgId = tmsg.get("/MSH-10");
                  String mrn = tmsg.get("/.PID-3");
                  String providerId = tmsg.get("/SCH-12");

                  logger.info("msgid: {} MRN: {} ProviderId: {}", msgId, mrn, providerId);

                  if (isBlacklisted(mrn)) {
                    logger.info("Ignoring the above message for black listed MRN " + mrn);
                  } else if (!isMt(this.filterSegmentName)
                      && !this.filterSegmentValue.equalsIgnoreCase(tmsg.get((this.filterSegmentName)))) {
                    logger.info("Ignoring the above message because  " + filterSegmentName + " !=" + filterSegmentValue);
                  } else {
                    //forward the message to all the IPs
                    for (int z = 0; z < ipList.length; z++) {
                      //check to see if the number of retries to send the message has exceeded max allowed limit
                      if (this.retries[z] >= this.maxTries) {

                        //check if all connections are down by comparing max retries on each connection.
                        int downConnections = 0;
                        for (int i = 0; i < retries.length; i++) {
                          if (retries[i] >= maxTries) {
                            downConnections++;
                          }
                        }

                        //If that's the case, no point in continuing.
                        if (downConnections >= retries.length) {
                          logger.error("All connections down, message forwarder exiting");
                          stop = true;
                          break;
                        }
                        continue;
                      }

                      try {
                        if (this.clients[z] == null) {
                          this.clients[z] = new HohClientSimple(ipList[z], Integer.parseInt( portList[z]), pathList[z], parsers[z]);
                          IAuthorizationClientCallback authCalback = new SingleCredentialClientCallback("someusername", "apassword");
                          this.clients[z].setAuthorizationCallback(authCalback);
                        }

                        logger.debug("Connectiong to server {} at port {} path {} ", ipList[z], portList[z], pathList[z]);

                        IReceivable<Message> receivable = clients[z].sendAndReceiveMessage(next);

                        // receivavle.getRawMessage() provides the response
                        Message response = receivable.getMessage();

                        logger.debug("Received response:");
                        logger.debug(response.encode());
                        this.retries[z] = 0; //reset retry count
                      } catch (Exception e) {
                        logger.error("Unable to send message {} to {} : {}" + msgId ,ipList[z],  portList[z],e);
                        if (this.retries[z] >= this.maxTries) {
                          logger.error("{} retries exceeded allowable limit {}", this.retries[z], this.maxTries);
                        }
                        if (this.clients[z] != null) {
                          this.clients[z].close();
                        }
                        this.clients[z] = null;
                        this.retries[z]++;
                      }
                    }
                  }
                }
                if (hasNulls) {
                  logger.info("Garbled HL7=" + orgStr);
                }
                hasNulls = false;
              } catch (Exception e) {
                logger.error("Error processing file {} encoding message {}", this.ipFileName, orgStr, e);
              }
              buf = new StringBuffer();
            } else if (!(tbuf[c] == 0x0a && prev == 0x0a)) // ignore
            // consecutive
            // newlines
            {
              buf.append(tbuf[c]);
            }
            prev = tbuf[c];
          }
          filePointer += rc;
        }
        console.close();

        // save the position of the processed Hl7 message
        ptrProps.setProperty(FILE_PTR, Long.toString(filePointer));
        //Hl7Utils.saveProperties(this.locationPtr, ptrProps);
        PropUtil.saveProperties(ptrProps, this.locationPtr);
        initBlacklist();

      }
    } catch (Exception e) {
      logger.error("error processing file {}", this.ipFileName, e);
    } finally {
      try {
        disconnect();
        if (this.context != null) {
          context.close();
        }
      } catch (Exception ex) {
        logger.error("ERROR on disconnect", ex);
      }
    }
  }

  private void disconnect() {
    if (this.clients != null) {
      for (HohClientSimple clientSimple : this.clients) {
        if (clientSimple != null) {
          clientSimple.close();
        }
      }
    }
  }

  /**
   * Is the given mrn blacklisted? (Truncates to first 7 digits)
   *
   * @param str
   * @return boolean
   */
  public boolean isBlacklisted(String str) {
    if (ignoreMrnList == null || ignoreMrnList.size() <= 0 || str == null || str.trim().length() < 7)
      return false;
    String mrn = str.trim();
    String s = mrn.substring(0, 7);
    String s1 = mrn.replace("-", "");

    // mrns can come in at least 3 formats, with - and a checksum, checksum
    // but no hyphen, without checksum and hyphen
    return ignoreMrnList.contains(mrn) || ignoreMrnList.contains(s) || ignoreMrnList.contains(s1);
  }

  private void initBlacklist() {
    File f = null;
    BufferedReader r = null;
    if (!isMt(ignoreMrnListFile)) {
      try {
        f = new File(ignoreMrnListFile);
        if (f == null || !f.exists()) {
          logger.info("Error reading the mrn blacklist file:" + ignoreMrnListFile);
          return;
        }
        if (f.lastModified() > ignoreMrnListFileModificationTime) {
          logger.info("reloading " + ignoreMrnListFile);
          ignoreMrnListFileModificationTime = f.lastModified();
          r = new BufferedReader(new FileReader(f));
          String line = r.readLine();
          ignoreMrnList = new ArrayList<>();

          while (line != null) {
            String s = line.trim();
            if (s.length() > 0) {
              ignoreMrnList.add(s);
            }
            line = r.readLine();
          }
          logger.info("Number of black listed MRNs: " + ignoreMrnList.size());
        }
      } catch (Exception ioe) {
        logger.info("Error reading the mrn blacklist file:" + ignoreMrnListFile + ioe.getMessage(), ioe);
      } finally {
        try {
          if (r != null) {
            r.close();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  // Returns true if the String is empty
  static boolean isMt(String s) {
    return s == null || "".equals(s.trim()) || s.trim().length() == 0;
  }

}
