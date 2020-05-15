package edu.stanford.registry.server.service;

import java.util.Map;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;

import edu.stanford.registry.server.service.rest.ApiStatusException;

public interface ApiCustomHandler {
  
  JSONObject handle(String callString, Map<String, String[]> params, JsonRepresentation jsonRep) 
      throws ApiStatusException;
}
