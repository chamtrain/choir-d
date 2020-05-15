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

import java.io.File;
import java.util.List;

/**
 * This interface represents a service that can send email.
 *
 * @author garricko
 */
public interface Mailer {
  /**
   * Create and send a text email.
   *
   * @return true if the mail was or is guaranteed to be sent; false otherwise
   */

  boolean sendText(String to, String cc, String bcc, String subject,
                   String content);


  boolean sendHtml(String to, String cc, String bcc, String subject, String content);

  /**
   * Create and send a text email with attachments
   *
   * @return true if the mail was or is guaranteed to be sent; false otherwise
   */
  boolean sendTextWithAttachment(String to, String cc, String bcc, String subject,
                                 String content, List<File> attachments);
}
