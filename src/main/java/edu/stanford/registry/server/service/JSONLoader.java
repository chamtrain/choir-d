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

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class JSONLoader extends ServerResource {
  private static Logger logger = Logger.getLogger(JSONLoader.class);
  private Variant variantAll = new Variant(MediaType.ALL);

  public JSONLoader() { // A public default construct is required- TomCat creates this as a restlet
    super();
    getVariants().add(variantAll);
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.APPLICATION_ALL));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    List<Variant> variants = getVariants();
    for (Variant v : variants) {
      logger.debug("Variant: " + v.getMediaType().getName());
    }
  }

  @Override
  public Representation doHandle(Variant variant) {
    logger.debug("in doHandle(variant)");
    logger.debug(variant.getMediaType().getName());
    if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
      logger.debug("is type application_json");
      // Read in the data
      org.restlet.Request req = getRequest();
      Representation repIn = req.getEntity();
      if (repIn != null) {
        try {
          @SuppressWarnings("unused")
	      JsonRepresentation jsonIn = new JsonRepresentation(repIn.getText());
          logger.debug("Contents are" + repIn.getText());

        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    JsonRepresentation result = null;
    JSONObject jsonObj = null;
    try {
      jsonObj = new JSONObject("{ post : Json }");
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    result = new JsonRepresentation(jsonObj);
    return result;

  }

  @Override
  public Representation doHandle() {
    logger.debug("in doHandle()");
    Representation rep = new StringRepresentation("Item created", MediaType.TEXT_PLAIN);
    return rep;

  }

  public Representation doHandle(Object obj) {
    logger.debug("in doHandle(obj)");
    Representation rep = new StringRepresentation("Item created", MediaType.TEXT_PLAIN);
    return rep;
  }

  @Post
  public void doPost() {
    logger.debug("in doPost");
  }
}
