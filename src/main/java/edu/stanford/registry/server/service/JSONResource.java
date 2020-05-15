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

package edu.stanford.registry.server.service;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class JSONResource extends ServerResource {

  @Get
  public Representation doGet(Representation entity) {
    Representation result = null;

    if (entity != null) {
      if (entity.getMediaType() != null) {
        if (entity.getMediaType().equals(MediaType.APPLICATION_JSON)) {

          /*
           * JSONObject jsonIn = new JSONObject(entity); try { if (jsonIn !=
           * null) { Iterator<String> it = jsonIn.keys(); while (it.hasNext()) {
           * String key = it.next(); System.out.println(key + " = " +
           * jsonIn.getString(key)); } } } catch (JSONException e) { // TODO
           * Auto-generated catch block e.printStackTrace(); } }
           */
        }
      }
    }
    boolean testing = true;
    // Register the new item if one is not already registered.
    if (testing) {
      // Set the response's status and entity
      setStatus(Status.SUCCESS_CREATED);
      // JSONObject jsonOut = new JSONObject();
      try {
        // jsonOut.put("id", "12345");
        // jsonOut.put("name", "Me");
      } catch (JSONException e) {
        e.printStackTrace();
      }

      // Representation rep = new JsonRepresentation(jsonOut);

      // result = rep;
    } else { // Item is already registered.
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      // result = generateErrorRepresentation("testing failed"
      // +, "1");
    }

    return result;

  }
}
