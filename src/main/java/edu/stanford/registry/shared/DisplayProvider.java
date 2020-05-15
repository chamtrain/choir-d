package edu.stanford.registry.shared;

/**
 * Created by tpacht on 6/15/2015.
 */
public class DisplayProvider extends Provider {
  private static final long serialVersionUID = 1L;

  private String username;
  private String displayName;

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
