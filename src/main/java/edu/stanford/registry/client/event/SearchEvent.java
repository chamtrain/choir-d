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

package edu.stanford.registry.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class SearchEvent extends GwtEvent<SearchEventHandler> {
  private final static com.google.gwt.event.shared.GwtEvent.Type<SearchEventHandler> TYPE = new Type<>();

  private final String searchStr;

  public String getSearchString() {
    return searchStr;
  }

  @Override
  public Type<SearchEventHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SearchEventHandler handler) {
    handler.onChange(this);

  }

  public enum SearchType {
    mrn, name
  }

  private final SearchType searchType;

  public SearchEvent(String searchString, SearchType SearchType) {
    this.searchStr = searchString;
    this.searchType = SearchType;
  }

  public static Type<SearchEventHandler> getType() {
    return TYPE;
  }

  public SearchType getSearchType() {
    return searchType;
  }

}
