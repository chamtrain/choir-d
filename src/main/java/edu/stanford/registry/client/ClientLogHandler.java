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

import edu.stanford.registry.shared.api.ClientServiceAsync;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.google.gwt.logging.client.RemoteLogHandlerBase;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler for client-side logging.
 */
public final class ClientLogHandler extends RemoteLogHandlerBase {
  private ClientServiceAsync service;
  private AsyncCallback<Void> callback;
  private List<LogRecord> logRecords = new ArrayList<>();
  private List<LogRecord> toSend;
  private boolean sending;
  private Timer timer;
  private boolean remoteEnabled;

  public ClientLogHandler() {
    this.callback = new MyCallback();
    this.timer = new MyTimer();
  }

  public void setService(ClientServiceAsync service) {
    assert !remoteEnabled || service != null;

    this.service = service;
  }

  public boolean isRemoteEnabled() {
    return remoteEnabled;
  }

  public void setRemoteEnabled(boolean remoteEnabled) {
    assert service != null;

    this.remoteEnabled = remoteEnabled;
    if (remoteEnabled) {
      timer.scheduleRepeating(5000);
    } else {
      timer.cancel();
    }
  }

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      logRecords.add(record);
    }
  }

  private class MyTimer extends Timer {
    @Override
    public void run() {
      if (sending) {
        return;
      }
      if (remoteEnabled) {
        if (toSend == null && !logRecords.isEmpty()) {
          toSend = logRecords;
          logRecords = new ArrayList<>();
        }
        if (toSend != null && toSend.size() > 0) {
          sending = true;
          service.clientLog(toSend, callback);
        }
      }
    }
  }

  private class MyCallback implements AsyncCallback<Void> {
    @Override
    public void onFailure(Throwable caught) {
      sending = false;
      wireLogger.log(Level.SEVERE, "Remote logging failed: ", caught);
    }

    @Override
    public void onSuccess(Void result) {
      toSend = null;
      sending = false;
    }
  }

}
