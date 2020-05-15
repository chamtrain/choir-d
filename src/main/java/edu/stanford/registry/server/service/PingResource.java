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

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class PingResource extends ServerResource {
  // @Get("txt")
  public String toText() {
    StringBuilder sb = new StringBuilder("Restlet server alive. Method: ");
    sb.append(getRequest().getMethod());

    org.restlet.data.ChallengeResponse challengeResponse = getRequest().getChallengeResponse();
    if (challengeResponse != null) {
      sb.append("/ Auth. scheme: ");
      sb.append(challengeResponse.getScheme());
    } else {
      challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
      getRequest().setChallengeResponse(challengeResponse);
    }

    return sb.toString();

  }

  @Post
  public String doPost() {
    return toText();
  }

  @Get
  public Representation doGet(Representation entity) {
    Representation result = null;
    // Parse the given representation and retrieve pairs of "name=value" tokens.
    Form form = new Form(entity);
    String itemName = form.getFirstValue("name");

    @SuppressWarnings("unused")
    String itemDescription = form.getFirstValue("description");

    boolean testing = true;
    // Register the new item if one is not already registered.
    if (testing) {
      // Set the response's status and entity
      setStatus(Status.SUCCESS_CREATED);

      Representation rep = new StringRepresentation("Item created from "
          + getRequest().getResourceRef().getIdentifier() + "/" + itemName, MediaType.TEXT_PLAIN);
      // Indicates where is located the new resource.
      // rep.setIdentifier(getRequest().getResourceRef().getIdentifier() + "/" + itemName);
      result = rep;
    } else { // Item is already registered.
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      // result = generateErrorRepresentation("testing failed"
      // +, "1");
    }

    return result;

  }
}
