package edu.stanford.registry.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This represents a request from the server to construct a GWT menu and the menu items' corresponding commands / logic.
 */
public class MenuDef implements IsSerializable {
  private String menuLabel;
  private String commandName;
  private Map<String, String> parameters;
  private String action;
  private String confirmMsg;

  public MenuDef() {
  }

  public MenuDef(String menuLabel, String commandName) {
    this(menuLabel, commandName, null);
  }

  public MenuDef(String menuLabel, String commandName, String[][] params) {
    this.commandName = commandName;
    this.menuLabel = menuLabel;
    this.action = null;
    this.confirmMsg = null;
    this.parameters = new HashMap<>();
    if (params != null) {
      for(String[] param : params) {
        if ((param == null) || (param.length != 2)) {
          throw new IllegalArgumentException("MenuDef construction params does not contain a pair of Strings");
        }
        if (param[0] == null) {
          throw new IllegalArgumentException("MenuDef construction param name is null");        
        }
        parameters.put(param[0], param[1]);
      }
    }
  }

  public static MenuDef customMenuDef(String menuLabel, String action, String[][] params, String confirmMsg) {
    MenuDef menuDef = new MenuDef(menuLabel, Constants.ACTION_CMD_CUSTOM, params);
    menuDef.setAction(action);
    menuDef.setConfirmMsg(confirmMsg);
    return menuDef;
  }

  public String getMenuLabel() {
    return menuLabel;
  }

  public void setMenuLabel(String menuLabel) {
    this.menuLabel = menuLabel;
  }

  public String getCommandName() {
    return commandName;
  }

  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getConfirmMsg() {
    return confirmMsg;
  }

  public void setConfirmMsg(String confirmMsg) {
    this.confirmMsg = confirmMsg;
  }

}
