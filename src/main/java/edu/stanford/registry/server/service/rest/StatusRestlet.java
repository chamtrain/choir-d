package edu.stanford.registry.server.service.rest;

import edu.stanford.registry.server.RegistryServletRequest;
import edu.stanford.registry.server.service.AdministrativeServices;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;

/**
 * Created by the RestletServiceManager, so can't be constructed with a site
 * Created by tpacht on 3/22/2016.
 */
public class StatusRestlet extends CustomRestlet {
  private static Logger logger = Logger.getLogger(StatusRestlet.class);

  public StatusRestlet() {
    // A public default construct is required- TomCat creates this as a restlet
  }

  @Get
  public Representation doGet(Representation entity) {
    return doHandle(entity);
  }
  @Override
  public Representation doHandle() {
    return doHandle(null);
  }

  @Override
  public Representation doHandle(Variant variant) {
    if (variant != null && variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
      // Read in the data
      org.restlet.Request req = getRequest();
      Representation repIn = req.getEntity();
      if (repIn != null) {
        try {
          @SuppressWarnings("unused")
          JsonRepresentation jsonIn = new JsonRepresentation(repIn.getText());
          logger.debug("Json request is " + repIn.getText());
        } catch (IOException e) {
          logger.error("Error reading json in request ", e);
        }
      }
    }

    // Set the response's status and entity
    RegistryServletRequest req = (RegistryServletRequest) ServletUtils.getRequest(getRequest());
    String pathInfo = req.getPathInfo();

    try {
      JsonRepresentation json = getJsonResponse(req);
      if (pathInfo.contains("text")) {
        String jsonString = "";
        try {
          jsonString = json.getText();
        } catch (IOException e) {
          jsonString = e.toString();
        }
        return new StringRepresentation("RESPONSE:" + jsonString);
      }
      return json;
    } catch (JSONException jse) {
      return new StringRepresentation("FAILURE:" + jse.toString());
    }
  }

  private JsonRepresentation getJsonResponse(RegistryServletRequest req ) throws JSONException {
    String status = "OK";
    String message = "OVERALL STATUS: OK";

    // Access the database
    try {
      AdministrativeServices adminService = (AdministrativeServices) req.getService();
      adminService.getTableData("SurveySystem");
      setStatus(Status.SUCCESS_OK);
      setName("status");
    } catch (Exception e) {
      status="ERROR";
      message="OVERALL STATUS: ERROR Unable to access the CHOIR database!";
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("id", "CHOIRService");
    jsonObj.put("message", message);
    jsonObj.put("status", status);
    jsonObj.put("timestamp", (new Date()).toString());

    JsonRepresentation result = new JsonRepresentation(jsonObj);
    result.setMediaType(MediaType.APPLICATION_JSON);
    return result;
  }
}
