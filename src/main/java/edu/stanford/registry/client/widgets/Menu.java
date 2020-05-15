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

package edu.stanford.registry.client.widgets;

import java.util.ArrayList;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;

/**
 * Simplified MenuBar interface allowing us to put wrappers around Commands
 * to make sure our popup gets closed.
 */
@SuppressWarnings("deprecation")
public class Menu {
  private MenuBar menuBar = new MenuBar(true);
  private ArrayList<MenuItem> itemList = new ArrayList<MenuItem>(7);
  private PopupRelative popup;

  public MenuBar getMenuBar() {
    return menuBar;
  }

  /**
   * Removes from the menu the item with the given labelText.
   */
  public void removeItem(String labelText) {
    for (MenuItem item: itemList) {
      if (labelText.equals(item.getText())) {
        itemList.remove(item);
        return;
      }
    }
  }

  public MenuItem addItem(String text, Command command) {
    MenuItem item = menuBar.addItem(text, new CommandWrapper(command));
    return item;
  }

  public MenuItem addItem(String text, String cssStyleName, Command command) {
    return addItem(text, cssStyleName, true, command);
  }

  public MenuItem addItem(String text, String cssStyleName, boolean enabled, Command command) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    if (WidgetResources.INSTANCE.css().checkedMenuItem().equals(cssStyleName) ||
        WidgetResources.INSTANCE.css().uncheckedMenuItem().equals(cssStyleName)) {
      // Explicitly include check mark because :before isn't reliable in IE8
      builder.appendHtmlConstant(
          "<span class=\"" + WidgetResources.INSTANCE.css().menuItemCheck() + "\">\u2713</span>");
    }
    if (text != null) {
      builder.appendEscaped(text);
    }
    MenuItem item = new MenuItem(builder.toSafeHtml(), new CommandWrapper(command));
    if (cssStyleName != null && cssStyleName.length() > 0) {
      item.addStyleName(cssStyleName);
    }
    item.setEnabled(enabled);
    return menuBar.addItem(item);
  }

  public MenuItem addItemChecked(String text, boolean checked, Command command) {
    return addItem(text, checked ? WidgetResources.INSTANCE.css().checkedMenuItem() :
        WidgetResources.INSTANCE.css().uncheckedMenuItem(), true, new CommandWrapper(command));
  }

  public MenuItem addItemChecked(String text, boolean checked, boolean enabled, Command command) {
    return addItem(text, checked ? WidgetResources.INSTANCE.css().checkedMenuItem() :
        WidgetResources.INSTANCE.css().uncheckedMenuItem(), enabled, new CommandWrapper(command));
  }

  public MenuItem addItem(MenuItem item) {
    Command command = item.getCommand();

    // Unwrap the command in case people have been reusing instanc
    while (command instanceof CommandWrapper) {
      command = ((CommandWrapper) command).command;
    }
    // Now wrap it back up for this invocation
    item.setCommand(new CommandWrapper(command));

    return menuBar.addItem(item);
  }

  public void addSeparator() {
    menuBar.addSeparator();
  }

  public void addSeparatorWithLabel(String label) {
    menuBar.addSeparator(new LabeledSeparator(label));
  }

  private static class LabeledSeparator extends MenuItemSeparator {
    public LabeledSeparator(String label) {
      super();
      // Add an inner element for styling purposes
      Element div = DOM.createDiv();
      DOM.appendChild(getElement(), div);
      div.setInnerText(SafeHtmlUtils.htmlEscape(label));
      setStyleName(div, WidgetResources.INSTANCE.css().menuSeparatorLabel());
    }
  }

  public void addSeparator(String styleName) {
    MenuItemSeparator separator = new MenuItemSeparator();
    separator.addStyleName(styleName);
    menuBar.addSeparator(separator);
  }

  public void addStyleName(String styleName) {
    menuBar.addStyleName(styleName);
  }

  public void closePopupBeforeCommand(PopupRelative popup) {
    this.popup = popup;
  }

  // Wrapper for a command that closes our popup before delegating the command
  private class CommandWrapper implements Command {
    Command command;

    CommandWrapper(Command command) {
      this.command = command;
    }

    @Override
    public void execute() {
      if (popup != null) {
        popup.close();
      }
      command.execute();
    }
  }

  public interface CheckedCommand extends Command {
    boolean isChecked();
  }
}
