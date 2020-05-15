package edu.stanford.survey.client.ui;


import edu.stanford.survey.client.api.PlayerProgressEvent;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.sksamuel.jqm4gwt.form.JQMFieldContainer;
import com.sksamuel.jqm4gwt.form.elements.JQMFormWidget;
public class VideoLink extends JQMFieldContainer implements JQMFormWidget {
  String partnerId;
  String targetId;
  String wid;
  String uiConfId;
  String entryId;
  String videoHeight;
  String videoWidth;
  String frameHeight;
  String frameWidth;

  Callback<PlayerProgressEvent, Throwable> actionCallback = null;
  private VideoLink() {
  }
  
  public VideoLink(Integer key, final String partnerId, final String targetId, final String wid, final String uiConfId, final String entryId,
      final String videoHeight, final String videoWidth, String frameHeight, final String frameWidth,  Callback<PlayerProgressEvent, Throwable> callback) {

    this.partnerId = partnerId;
    this.targetId = targetId;
    this.wid = wid;
    this.uiConfId = uiConfId;
    this.entryId = entryId;
    this.videoHeight = videoHeight;
    this.videoWidth = videoWidth;
    this.frameHeight = frameHeight;
    this.frameWidth = frameWidth;
    this.actionCallback = callback;

    /** 
     * Add the javascript methods that the player frame content calls to initialize 
     * the kaltura player and to send back the player events 
     */
    JavaScriptInjector.injectBodyScript(getStateChangedScript(key));
    JavaScriptInjector.injectBodyScript( " function getPartnerId" + key.toString() + "() { return  '" + this.partnerId + "'; }");
    JavaScriptInjector.injectBodyScript( " function getTargetId" + key.toString() + "() { return '" + this.targetId + "'; }");
    JavaScriptInjector.injectBodyScript( " function getWid" + key.toString() + "() { return '" + this.wid + "'; }");
    JavaScriptInjector.injectBodyScript( " function getUiconfId" + key.toString() + "() { return '" + this.uiConfId + "'; }");
    JavaScriptInjector.injectBodyScript( " function getEntryId" + key.toString() + "() { return '" + this.entryId + "'; }");
    JavaScriptInjector.injectBodyScript( " function getPlayerStyle" + key.toString() + "() { return 'width: " + this.videoWidth + "px; height: " + this.videoHeight + "px;' }");
    exportSendVideoData();
    
    /**
     * Load the frame with the kaltura video player 
     */
    final Frame newFrame = new Frame();
    newFrame.setUrl(GWT.getModuleBaseURL() + "videos/kaltura_player" + key.toString() +".html ");
    newFrame.getElement().setAttribute("style",
        "width: " + frameWidth + "px; height: " + frameHeight + "px; padding: 0px; margin: 0px; border: none;");
       newFrame.addLoadHandler(new LoadHandler() {
      
      @Override
      public void onLoad(LoadEvent event) {
        newFrame.setVisible(true);
      }
    });
    add(newFrame);
    
  }
   
  @Override
  public String getValue() {
    return "{ " + partnerId + "," + targetId + "," + wid + "," + uiConfId + "," + entryId + "}";
  }

  @Override
  public void setValue(String value) {
    targetId = value;
  }
  
  @Override
  public void setValue(String value, boolean fireEvents) {
    targetId = value;
  }

  private String getStateChangedScript(Integer key) {
    return
   " function playerStateChangeHandler" + key.toString() + "( id, action, data ){" +
//   " console.log(\"playerStateChangeHandler state changed \"); " +
      " sendVideoData(id, action, data);  } ";
    }
  
  @Override
  protected void onLoad() {
     super.onLoad();
  }

  public String sendVideoData(String id, String action, String data) {
      if (id == null) {
        id = this.targetId;
      }
      if (actionCallback != null) {
        actionCallback.onSuccess(new PlayerProgressEvent(id, action, data));
      } else {
        consoleLog("no actionCallback!");
      }
      return (data + " send to callback");
    }

    public native void exportSendVideoData() /*-{
        var videoLinkInstance = this;
        $wnd.sendVideoData = $entry(function(id, action, data) {
          videoLinkInstance.@edu.stanford.survey.client.ui.VideoLink::sendVideoData(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id, action, data);
        });
    }-*/;

    
    native void consoleLog( String message) /*-{
      console.log( "VideoLink:" + message );
    }-*/;

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return null;
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
      return null;
    }

    @Override
    public Label addErrorLabel() {
      return null;
    }


 }