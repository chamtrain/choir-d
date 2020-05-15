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

package edu.stanford.registry.server.utils;

public class PDFArea {

  float xFrom, xTo, yFrom, yTo, nextY;

  public PDFArea() {
    xFrom = 0f;
    xTo = 0f;
    yFrom = 0f;
    yTo = 0f;
    nextY = 0f;
  }

  public float getXfrom() {
    return xFrom;
  }

  public void setXfrom(float from) {
    xFrom = from;
  }

  public float getXto() {
    return xTo;
  }

  public void setXto(Float to) {
    xTo = to;
  }

  public float getYfrom() {
    return yFrom;
  }

  public void setYfrom(float from) {
    yFrom = from;
  }

  public float getYto() {
    return yTo;
  }

  public void setYto(Float to) {
    yTo = to;
  }

  public float getNextY() {
    return nextY;
  }

  public void setNextY(float next) {
    nextY = next;
  }
}
