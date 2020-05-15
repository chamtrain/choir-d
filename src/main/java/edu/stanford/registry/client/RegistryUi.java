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

package edu.stanford.registry.client;

import com.google.gwt.user.client.ui.Label;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.client.widgets.PersonSearch;
import edu.stanford.registry.client.widgets.PopdownButton;
import edu.stanford.registry.shared.Site;
import edu.stanford.registry.shared.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ObjectElement;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class RegistryUi extends ResizeComposite {
  private User user;

  interface RegistryUiUiBinder extends UiBinder<Widget, RegistryUi> {
  }

  public RegistryUi() {
    RegistryUiUiBinder uiBinder = GWT.create(RegistryUiUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
    page.getElement().setAttribute("id", "pageframe");
  }

  // @UiField
  // Image logo;
  @UiField
  DockLayoutPanel page;
  @UiField
  FlowPanel footer;
  @UiField
  DockLayoutPanel header;
  @UiField
  FlowPanel imagePanel;
  @UiField
  HorizontalPanel titlePanel;
  @UiField
  HTML footlink1;
  @UiField
  HTML footlink2;
  @UiField
  HTML footlink3;
  @UiField
  HorizontalPanel searchPanel;
  @UiField
  HorizontalPanel userPanel;

  TitleHandler titleHandler = new TitleHandler();

  public void setMainWidget(Widget widget) {
    for (int i = 0; i < page.getWidgetCount(); i++) {
      if (page.getWidgetDirection(page.getWidget(i)) == Direction.CENTER) {
        page.remove(i);
        break;
      }
    }
    page.add(widget);
  }

  public void setLoggedInUser(User user) {
    this.user = user;
    HTML userHtml = new HTML("<i>Logged in as: </i><STRONG>" + Sanitizer.sanitizeHtml(user.getUsername()).asString() + "</STRONG>");
    userPanel.add(userHtml);
    userPanel.setVisible(true);
    userPanel.setStylePrimaryName("title-Login");
  }

  public void assembleHeader(String title, String siteName) {
    //logo.setStylePrimaryName("logo");
    RegistryCssResource css = RegistryResources.INSTANCE.css();

    // Create an element for logo and set its attributes
    ObjectElement logoSvg = Document.get().createObjectElement();
    logoSvg.setWidth("320px");
    logoSvg.setType("image/svg+xml");
    logoSvg.setData(RegistryResources.INSTANCE.choirLogo().getSafeUri());

    // Attach the element to the document and add it to the panel
    Document.get().getBody().appendChild(logoSvg);
    imagePanel.add(HTMLPanel.wrap(logoSvg));

    if (user != null) { // caller might call with user==null
      titleHandler.initialize(user.getSurveySites(), siteName, css);
    }
  }

  public void assembleFooter(String aboutUsLink, String termsLink, String contactLink) {
    if (aboutUsLink == null && termsLink == null && contactLink == null) {
      footer.setVisible(false);
    } else {
      if (aboutUsLink == null) {
        footlink1.setVisible(false);
      } else {
        footlink1.setHTML("<A HREF=\"" + aboutUsLink + "\">About us</A>");
      }
      if (termsLink == null) {
        footlink2.setVisible(false);
      } else {
        footlink2.setHTML("<A HREF=\"" + termsLink + "\">Terms</A>");
      }
      if (contactLink == null) {
        footlink3.setVisible(false);
      } else {
        footlink3.setHTML("<A HREF=\"" + contactLink + "\">Contact</A>");
      }
    }
  }

  public void setSearchPanel(PersonSearch searchWidget) {
    searchPanel.clear();
    if (searchWidget == null) {
      searchPanel.setVisible(false);
    } else {
      searchPanel.add(searchWidget.getSearchPanel());
      searchPanel.setVisible(true);
    }
  }

  public User getUser() {
    return user;
  }

  /**
   * The Title box contains the site title, plus allows the user to switch to other sites
   */
  class TitleHandler implements PopdownButton.Customizer { // SiteMenu
    Site userSites[];
    Site curSite;
    Site unknownSite = new Site(-1L, "unknown", "Unknown Site (Error)", false);
    RegistryCssResource css;
    PopdownButton button;

    void initialize(Site userSites[], String userSiteUrl, RegistryCssResource css) {
      this.css = css;
      this.userSites = (userSites != null) ? userSites : new Site[0]; // avoid an NPE
      curSite = getSiteWithID(userSiteUrl);  // set in ServiceFilters

      button = new PopdownButton()
          .withMenu(this)
          .withText(curSite.getDisplayName())
          .withStyle(css.siteSelect() + " "+ css.titleText());
      button.addMenuStyle(css.siteSelectMenu() + " " + css.titleLabel());
      initTitleToolTip(button.asButton());
      addToPage(); // add it AFTER it's full
    }

    private void addToPage() {
      /* So the title can still be vertically centered even if it wraps to two lines, we put a thin space
       * before it in a font over twice as big.
       */
      InlineHTML thinspace = new InlineHTML();
      thinspace.setStylePrimaryName("titleSpacer");
      thinspace.setHTML("&thinsp;");
      thinspace.setWidth("0%");

      titlePanel.setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_CENTER);
      titlePanel.setVerticalAlignment(com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_MIDDLE);
      titlePanel.setWidth("100%");
      titlePanel.add(thinspace); //spacer);

      if (userSites.length > 1) {
        titlePanel.add(button); //vertPanel);
      } else {
        Label siteTitle = new Label(curSite.getDisplayName());
        siteTitle.setStyleName(css.siteTitle());
        titlePanel.add(siteTitle);
      }
    }

    @Override
    public void customizePopup(PopdownButton button, Menu menu) {
      initMenuWithSiteTitles(button, menu);
    }

    class SiteCommand implements Command {
      Site site;
      SiteCommand(Site site) {
        this.site = site;
      }
      @Override
      public void execute() {
        if (!curSite.equals(site)) {
          UrlBuilder bldr = Window.Location.createUrlBuilder();
          bldr.setParameter("siteId", site.getUrlParam());
          Window.Location.replace(bldr.buildString());
        }
      }
    }

    void initMenuWithSiteTitles(final PopdownButton button, final Menu siteMenu) {
      if (userSites.length == 0) {
        MenuItem item = new MenuItem(unknownSite.getDisplayName(), new SiteCommand(unknownSite));
        item.setStylePrimaryName(css.titleLabel());
        siteMenu.addItem(item);
        GWT.log("ERROR: You have no allowed sites!");
        return;
      }

      for (Site site: userSites) {
        MenuItem item = new MenuItem(site.getDisplayName(), new SiteCommand(site));
        item.setStylePrimaryName(css.siteSelectMenu() + " " + css.titleLabel());
        siteMenu.addItem(item);
      }
    }

    void initTitleToolTip(final Button button) {
      if (userSites.length == 0) {
        button.setTitle("This contains sites you are permitted to access: none!");
      } else if (userSites.length == 1) {
        button.setTitle("The site you are permitted to access.");
      } else {
        button.setTitle("Use this to switch to the "+userSites.length+" sites you are permitted to access.");
      }
    }

    // ==== utilities

    String getSiteUrlList() {
      StringBuilder sb = new StringBuilder();
      for (Site site: user.getSurveySites()) {
        sb.append(sb.length()==0 ? "" : ",").append(site.getUrlParam());
      }
      return sb.toString();
    }

    Site getSiteWithID(String id) {
      if (id == null || id.isEmpty()) {
        id = "1";  // should never happen
      }

      for (Site site: user.getSurveySites()) {
        if (site.getUrlParam().equals(id)) {
          return site;
        }
      }
      GWT.log("User does not have access to site: "+id);
      return unknownSite;
    }

    // To find the site corresponding to the new title
    Site getSiteWithTitle(String title) {
      if (title == null || title.isEmpty()) {
        GWT.log("ERROR: can not find which site has title: "+title);
        return unknownSite;
      }

      for (Site site: userSites) {
        if (site.getDisplayName().equals(title)) {
          return site;
        }
      }
      GWT.log("Site with selected title '"+title+"' is not in users list: "+getSiteUrlList());
      return unknownSite;
    }
  }
}
