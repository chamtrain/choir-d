package edu.stanford.registry.client.service;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.RandomSetParticipant;

public interface PhysicianServiceAsync {


  void createSurvey(String patientId, String processName, AsyncCallback<String> callback);

  void getProcessNames(String patientId, AsyncCallback<ArrayList<String>> callback);

  void getSurveyJson(ApptId apptId, AsyncCallback<String> callback);

  void getPhysicianSurveyPath(AsyncCallback<String> callback);

  void isFinished(String token, AsyncCallback<Boolean> async);

  void updateRandomSetParticipant(RandomSetParticipant rsp, AsyncCallback<RandomSetParticipant> async);
}
