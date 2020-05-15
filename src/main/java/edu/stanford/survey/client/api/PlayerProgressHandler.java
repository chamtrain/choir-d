package edu.stanford.survey.client.api;
import com.google.gwt.event.shared.EventHandler;
public interface PlayerProgressHandler extends EventHandler {

  /**
   * Called when {@link PlayerProgressEvent} is fired.
   * 
   * @param event the {@link PlayerProgressEvent} that was fired
   */
  void onProgress(PlayerProgressEvent videoProgressEvent);
}
