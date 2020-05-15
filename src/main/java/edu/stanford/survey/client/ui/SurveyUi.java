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

package edu.stanford.survey.client.ui;


import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.BodyMapQuestion;
import edu.stanford.survey.client.api.CollapsibleRadiosetQuestion;
import edu.stanford.survey.client.api.DisplayStatus;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.PlayerProgressEvent;
import edu.stanford.survey.client.api.PlayerProgressHandler;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.RadiosetQuestion;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SliderQuestion;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveyServiceAsync;
import edu.stanford.survey.client.api.TextInputAnswer;
import edu.stanford.survey.client.api.TextInputQuestion;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.sksamuel.jqm4gwt.JQMContext;
import com.sksamuel.jqm4gwt.Mobile;
import com.sksamuel.jqm4gwt.ScriptUtils;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;

/**
 * Top-level user interface for surveying.
 */
public class SurveyUi {
  private final SurveyServiceAsync surveyService;
  private final SurveyFactory factory = GWT.create(SurveyFactory.class);
  private TextInputPage textInputPage;
  private FormPage formPage;
  private String surveyToken;
  private Storage localStorage;
  private SubmitStatus submitStatus;
  private String surveySystem;
  private boolean isClinicTablet;
  private String styleSheetName;

  public SurveyUi(SurveyServiceAsync surveyService) {
    this.surveyService = surveyService;

    // If we have a survey token, pass it as a header for tracking purposes
    if (surveyService instanceof ServiceDefTarget) {
      ((ServiceDefTarget) surveyService).setRpcRequestBuilder(new RpcRequestBuilder() {
        @Override
        protected void doFinish(RequestBuilder rb) {
          super.doFinish(rb);
          if (surveySystem != null) {
            rb.setHeader("X-SURVEY-SYSTEM", surveySystem);
          }
          if (surveyToken != null) {
            rb.setHeader("X-SURVEY-TOKEN", surveyToken);
          }
          // TODO add support for setting/reading device tokens
          rb.setHeader("X-DEVICE-TOKEN", "None");
        }
      });
    }
  }

  public void show() {
    ScriptUtils.waitJqmLoaded(new Callback<Void, Throwable>() {
      @Override
      public void onSuccess(Void result) {
        showLater();
      }

      @Override
      public void onFailure(Throwable reason) {
        Window.alert(reason.getMessage());
      }
    });
  }

  private void showLater() {
    Mobile.showLoadingDialog("");
    JQMContext.disableHashListening();

    surveySystem = Location.getParameter("s");
    surveyToken = Window.Location.getParameter("tk");

    if (surveyToken == null) {
      // No survey token in URL ==> clinic tablet
      isClinicTablet = true;
    }

    final long startTime = System.currentTimeMillis();
    localStorage = Storage.getLocalStorageIfSupported();

    if (localStorage != null && isClinicTablet
        && hasValue(localStorage.getItem("resume"))
        && inFuture(localStorage.getItem("resumeExpires"))) {
      if ("0".equals(surveySystem) && isClinicTablet) {
        showMenu();
      } else {
        surveyService.resumeSurvey(localStorage.getItem("resume"), new AsyncCallback<String[]>() {
          @Override
          public void onFailure(Throwable t) {
            GWT.log("Server call failed", t);
          }

          @Override
          public void onSuccess(final String[] json) {
            dispatch(json, System.currentTimeMillis() - startTime);
          }
        });
      }
    } else {
      clearLocalStorage();
      if ("0".equals(surveySystem)) {
        showMenu();
      } else {
        surveyService.startSurvey(surveySystem, surveyToken, new AsyncCallback<String[]>() {
          @Override
          public void onFailure(Throwable t) {
            GWT.log("Server call failed", t);
          }

          @Override
          public void onSuccess(final String[] json) {
            dispatch(json, System.currentTimeMillis() - startTime);
          }
        });
      }
    }
  }

  private void showMenu() {
    GWT.log("showMenu called");
    surveyService.getSurveySites(new AsyncCallback<String[]>() {
      @Override
      public void onFailure(Throwable t) {
        GWT.log("Server call failed", t);
      }

      @Override
      public void onSuccess(String[] sites) {
        showSitesPage(sites);
      }
    });
  }
  private void clearLocalStorage() {
    try {
      if (localStorage != null) {
        localStorage.clear();
      }
    } catch (Exception e) {
      GWT.log("Unable to clear local storage", e);
    }
  }

  private void setLocalStorage(String key, String value) {
    try {
      if (localStorage != null) {
        localStorage.setItem(key, value);
      }
    } catch (Exception e) {
      GWT.log("Unable to set item in local storage", e);
    }
  }

  private boolean hasValue(String s) {
    return s != null && s.length() > 0;
  }

  private boolean inFuture(String millisStr) {
    return System.currentTimeMillis() < Long.parseLong(millisStr);
  }

  private void dispatch(final String[] json, long callTimeMillis) {
    try {
      final long startRenderTime = System.currentTimeMillis();
      final DisplayStatus displayStatus = AutoBeanCodex.decode(factory, DisplayStatus.class, json[0]).as();

      if (displayStatus.getCompatLevel() > SurveyFactory.compatibilityLevel) {
        Window.alert("A new version is available. Click Ok to load the new version");
        Location.reload();
      }

      submitStatus = factory.submitStatus().as();
      submitStatus.setCompatLevel(SurveyFactory.compatibilityLevel);
      surveyToken = displayStatus.getSurveyToken();
      if (displayStatus.getSurveySystemName() != null) {
        submitStatus.setSurveySystemName(displayStatus.getSurveySystemName());
      }
      submitStatus.setStepNumber(displayStatus.getStepNumber());
      submitStatus.setCallTimeMillis(callTimeMillis);

      if (localStorage != null) {
        // Update local storage as appropriate if the server gave us resume information
        if (displayStatus.getResumeToken() != null) {
          setLocalStorage("resume", displayStatus.getResumeToken());
        }
        if (displayStatus.getResumeTimeoutMillis() != null) {
          long timeout = displayStatus.getResumeTimeoutMillis();
          if (timeout < 1) {
            clearLocalStorage();
          } else {
            setLocalStorage("resumeExpires", Long.toString(System.currentTimeMillis() + timeout));
          }
        }
        // Clear session and resume tokens if server requests it
        if (displayStatus.getSessionStatus() == SessionStatus.clearSession) {
          clearLocalStorage();
        }
      }

      submitStatus.setQuestionType(displayStatus.getQuestionType());
      submitStatus.setQuestionId(displayStatus.getQuestionId());
      submitStatus.setSurveyProviderId(displayStatus.getSurveyProviderId());
      submitStatus.setSurveySectionId(displayStatus.getSurveySectionId());
      submitStatus.setSessionToken(displayStatus.getSessionToken());

      if (displayStatus.getStyleSheetName() != null && !displayStatus.getStyleSheetName().equals(styleSheetName)) {
        // load a new theme
        styleSheetName = displayStatus.getStyleSheetName();
        String styleSheetFileName = displayStatus.getStyleSheetName();
        String styleLoadingRef  = styleSheetFileName;
        if (styleSheetFileName.indexOf(".") > 0) {
          styleLoadingRef = styleSheetFileName.substring(0,styleSheetFileName.indexOf("."));
        }
        StyleLoader.loadStyleSheet(displayStatus.getStyleSheetName(), styleLoadingRef, new ScheduledCommand() {
          // DeferredCommand.addCommand(new IncrementalCommand() {
          @Override
          public void execute() {
            showPage(json, displayStatus, startRenderTime);
          }
        });
      } else {
        showPage(json, displayStatus, startRenderTime);
      }
    } catch (Throwable t) {
      GWT.log("Failed to create question page", t);
    }
  }

  private void showPage(String[] json, DisplayStatus displayStatus, final long startRenderTime) {
    boolean confirmRestart = displayStatus.getSessionStatus() != SessionStatus.clearSession;
    TapHandler surveyRestart = null;
    if (isClinicTablet) {
      surveyRestart = new TapHandler() {
        @Override
        public void onTap(TapEvent event) {
          GWT.log("tap handler called");
          clearLocalStorage();
          if (event.getSource() instanceof JQMButton) {
            if (((JQMButton) event.getSource()).getText().equals("End Survey")) {
              Location.reload();
            } else {
              UrlBuilder builder = Window.Location.createUrlBuilder();
              builder.removeParameter("s");
              builder.setParameter("s", "0");
              Window.Location.replace(builder.buildString());
            }
          }
        }
      };
    }

    QuestionType questionType = displayStatus.getQuestionType();
    if (questionType == QuestionType.radioset) {
      RadiosetQuestion question = AutoBeanCodex.decode(factory, RadiosetQuestion.class, json[1]).as();
      RadiosetAnswer answer = factory.radiosetAnswer().as();

      RadiosetPage radioPage = (new RadiosetPage(question, answer, new RadiosetPage.Submit() {
        @Override
        public void submit(RadiosetAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      }));
      radioPage.setHeader(displayStatus);
      JQMContext.changePage(radioPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.buttonList) {
      RadiosetQuestion question = AutoBeanCodex.decode(factory, RadiosetQuestion.class, json[1]).as();
      RadiosetAnswer answer = factory.radiosetAnswer().as();

      ButtonListPage buttonPage = new ButtonListPage(question, answer, new ButtonListPage.Submit() {
        @Override
        public void submit(RadiosetAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      buttonPage.setHeader(displayStatus);
      JQMContext.changePage(buttonPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.slider) {
      SliderQuestion question = AutoBeanCodex.decode(factory, SliderQuestion.class, json[1]).as();
      NumericAnswer answer = factory.numericAnswer().as();

      SliderPage sliderPage = new SliderPage(question, answer, new SliderPage.Submit() {
        @Override
        public void submit(NumericAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      sliderPage.setHeader(displayStatus);
      JQMContext.changePage(sliderPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.numericScale) {
      SliderQuestion question = AutoBeanCodex.decode(factory, SliderQuestion.class, json[1]).as();
      NumericAnswer answer = factory.numericAnswer().as();

      NumericScalePage numScalePage = new NumericScalePage(question, answer, new NumericScalePage.Submit() {
        @Override
        public void submit(NumericAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      numScalePage.setHeader(displayStatus);
      JQMContext.changePage(numScalePage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.bodyMap) {
      BodyMapQuestion question = AutoBeanCodex.decode(factory, BodyMapQuestion.class, json[1]).as();
      BodyMapAnswer answer = factory.bodyMapAnswer().as();

      BodyMapPage bodyMapPage = new BodyMapPage(question, answer, new BodyMapPage.Submit() {
        @Override
        public void submit(BodyMapAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      bodyMapPage.setHeader(displayStatus);
      JQMContext.changePage(bodyMapPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.thanks) {
      RadiosetQuestion question = AutoBeanCodex.decode(factory, RadiosetQuestion.class, json[1]).as();
      RadiosetAnswer answer = factory.radiosetAnswer().as();

      ButtonListPage buttonListPage = new ButtonListPage(question, answer, new ButtonListPage.Submit() {
        @Override
        public void submit(RadiosetAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      buttonListPage.setHeader(displayStatus);
      JQMContext.changePage(buttonListPage.withSurveyRestart(surveyRestart, confirmRestart));

    } else if (questionType == QuestionType.form) {
      FormQuestion question = AutoBeanCodex.decode(factory, FormQuestion.class, json[1]).as();

      if ((displayStatus.getSessionStatus() == SessionStatus.questionInvalid
          || displayStatus.getSessionStatus() == SessionStatus.tokenLookupInvalid)
          && formPage != null) {
        formPage.serverValidationFailed(question, displayStatus.getServerValidationMessage());
        formPage.setHeader(displayStatus);
        submitStatus.setRenderTimeMillis(System.currentTimeMillis() - startRenderTime);

        return;
      }

      FormAnswer answer = factory.formAnswer().as();

      formPage = new FormPage(factory, question, answer, new FormPage.Submit() {
        @Override
        public void submit(FormAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      formPage.setHeader(displayStatus);
      formPage.addPlayerProgressHandler(
          new PlayerProgressHandler() {
            @Override
            public void onProgress(PlayerProgressEvent event) {
              addVideoProgress(submitStatus, event.getTargetId(), event.getAction(), event.getTime());
            }
          });
      JQMContext.changePage(formPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.textList) {
      TextInputQuestion question = AutoBeanCodex.decode(factory, TextInputQuestion.class, json[1]).as();

      if (displayStatus.getSessionStatus() == SessionStatus.questionInvalid && textInputPage != null) {
        textInputPage.serverValidationFailed(question, displayStatus.getServerValidationMessage());
        textInputPage.setHeader(displayStatus);
        submitStatus.setRenderTimeMillis(System.currentTimeMillis() - startRenderTime);
        return;
      }

      TextInputAnswer answer = factory.textInputAnswer().as();

      textInputPage = new TextInputPage(question, answer, new TextInputPage.Submit() {
        @Override
        public void submit(TextInputAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      textInputPage.setHeader(displayStatus);
      JQMContext.changePage(textInputPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.collapsibleRadioset) {
      CollapsibleRadiosetQuestion question = AutoBeanCodex.decode(factory, CollapsibleRadiosetQuestion.class, json[1]).as();
      RadiosetAnswer answer = factory.radiosetAnswer().as();

      CollapsibleRadiosetPage radioPage = (new CollapsibleRadiosetPage(question, answer, new CollapsibleRadiosetPage.Submit() {
        @Override
        public void submit(RadiosetAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      }));
      radioPage.setHeader(displayStatus);
      JQMContext.changePage(radioPage.withSurveyRestart(surveyRestart, confirmRestart));
    } else if (questionType == QuestionType.close) {
      RadiosetQuestion question = AutoBeanCodex.decode(factory, RadiosetQuestion.class, json[1]).as();
      RadiosetAnswer answer = factory.radiosetAnswer().as();
      ButtonListPage buttonListPage = new ButtonListPage(question, answer, new ButtonListPage.Submit() {
        @Override
        public void submit(RadiosetAnswer answer) {
          advance(submitStatus, AutoBeanUtils.getAutoBean(answer), startRenderTime);
        }
      });
      buttonListPage.setHeader(displayStatus);
      buttonListPage.getHeader().setBackButton(false);
      JQMContext.changePage(buttonListPage);

    } else {
      GWT.log("Unknown question type: " + questionType, new Exception());
      Window.alert("Unable to display the next question (type is " + (questionType == null ? "missing" : questionType)
          + ")\n\nTry reloading this page.");
    }
    submitStatus.setRenderTimeMillis(System.currentTimeMillis() - startRenderTime);
  }

  private void showSitesPage(String[] strings) {
    LinkListPage linkListPage = new LinkListPage(strings);
    JQMContext.changePage(linkListPage);
  }

  private void advance(SubmitStatus status, AutoBean<?> answer, long startRenderTime) {
    blockUi();
    Mobile.showLoadingDialog("");

    submitStatus.setThinkTimeMillis(System.currentTimeMillis() - startRenderTime - submitStatus.getRenderTimeMillis());

    String statusJson = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();
    String answerJson = AutoBeanCodex.encode(answer).getPayload();

    long startCallTime = System.currentTimeMillis();
    surveyService.continueSurvey(statusJson, answerJson, new TimeoutCallback(status, answerJson, startCallTime, 1));
  }
  
  private void addVideoProgress(SubmitStatus status, String targetId, String action, String time) {
     
    String statusJson = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();
    long  milliseconds = 0;
    if (time != null) {
      time = time.trim();
      try {
        Float floatTime = Float.valueOf(time);
        floatTime = Float.valueOf((float)(floatTime * 1000.0));
        milliseconds = floatTime.longValue();
      } catch (NumberFormatException nfe) {
        consoleLog("NumberFormatException converting '" + time + "' to float");
      }
    }
    GWT.log("Video progressed: " + action + " " + milliseconds + "ms");
    //consoleLog( "addVideoProgress("+ action+ ", "+ milliseconds + " ms)");
    surveyService.addPlayerProgress(statusJson, targetId, action, milliseconds, new AsyncCallback<Void>() {
    
      @Override
      public void onFailure(Throwable reason) {
        Window.alert(reason.getMessage());
      }

      @Override
      public void onSuccess(Void result) {
      }      
    });
  }
  
  native void consoleLog( String message) /*-{
    console.log( "SurveyUi:" + message );
  }-*/;

  private native void blockUi() /*-{
    $wnd.$.blockUI({
      message: null,
      overlayCSS: {
        backgroundColor: 'transparent'
      }
    });
  }-*/;

  private native void unblockUi() /*-{
    $wnd.$.unblockUI();
  }-*/;

  private class TimeoutCallback implements AsyncCallback<String[]> {
    final Timer timer;
    boolean hasCompleted;
    boolean shouldIgnoreResult;
    private long startCallTime;

    public TimeoutCallback(final SubmitStatus status, final String answerJson, final long startCallTime, final long retries) {
      this.startCallTime = startCallTime;
      timer = new Timer() {
        @Override
        public void run() {
          if (!hasCompleted) {
            Mobile.showLoadingDialog("Having trouble reaching the server (retry " + retries + ")...");
            shouldIgnoreResult = true;

            // For now the retry sends a null answer, which the server interprets as a restart request,
            // which can have the effect of discarding what is currently on the screen (if it hung on the
            // way to the server rather than the way back). Once we make the server idempotent we can
            // avoid that situation, but currently resending answers can break things (especially PROMIS)
            status.setRetryCount(retries);
            String statusJson = AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();

            long startCallTime = System.currentTimeMillis();
            surveyService.continueSurvey(statusJson, answerJson, new TimeoutCallback(status, answerJson,
                startCallTime, retries + 1));
          }
        }
      };
      timer.schedule(30000);
    }

    @Override
    public void onFailure(Throwable t) {
      t.printStackTrace();
      hasCompleted = true;
      if (shouldIgnoreResult) {
        return;
      }
      Mobile.hideLoadingDialog();
      unblockUi();
    }

    @Override
    public void onSuccess(final String[] json) {
      try {
        long callTimeMillis = System.currentTimeMillis() - startCallTime;
        hasCompleted = true;
        if (shouldIgnoreResult) {
          return;
        }
        Mobile.hideLoadingDialog();
        unblockUi();
        dispatch(json, callTimeMillis);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
