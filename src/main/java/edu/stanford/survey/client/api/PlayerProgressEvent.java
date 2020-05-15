package edu.stanford.survey.client.api;

import com.google.gwt.event.shared.GwtEvent;


public class PlayerProgressEvent extends GwtEvent<PlayerProgressHandler> {

  private final String targetId;
  private final String action;
  private final String time;
  /**
   * Handler type.
   */
  private static Type<PlayerProgressHandler> TYPE;
  
  /*
   *  Create the event
   */
  public PlayerProgressEvent(String targetId, String action, String time) {
    this.targetId = targetId;
    this.action = action;
    this.time = time;
  }
 
  /**
   * Gets the type associated with this event.
   * 
   * @return returns the handler type
   */
  public static com.google.gwt.event.shared.GwtEvent.Type<PlayerProgressHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<PlayerProgressHandler> getAssociatedType() {
    return (Type) TYPE;
  }

  /**
   * Gets the targetId.
   * 
   * @return the player id 
   */
  public String getTargetId() {
    return targetId;
  }
  
  /**
   * Gets the action performed on the video
   */
  public String getAction() {
    return action;
  }
  
  /**
   * Gets the time value
   */
  public String getTime() {
    return time;
  }
  
  @Override
  public java.lang.String toDebugString() {
    return super.toDebugString() + getTargetId() + ";" + getAction() + ";" + getTime();
  }

  @Override
  protected void dispatch(PlayerProgressHandler handler) {
    handler.onProgress(this);
  }
  
  public static <T> void fire(HasPlayerProgressHandlers source, String targetId, String action, String time) {
    if (TYPE != null) {
      PlayerProgressEvent event = new PlayerProgressEvent(targetId, action, time);
      source.fireEvent(event);
    }
  }
  
  public void fire(HasPlayerProgressHandlers source) {
    source.fireEvent(this);
  }
}
