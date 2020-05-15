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

package edu.stanford.registry.client.service;

import edu.stanford.registry.client.utils.ErrorHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * Base class to centralize some error handling logic.
 */
public class Callback<T> implements AsyncCallback<T> {
  private static final Logger log = Logger.getLogger(Callback.class.getName());
  private AsyncCallback<T> delegate;
  private final ErrorHandler errorHandler = new ErrorHandler();

  public Callback() {
    // Default
  }

  public Callback(AsyncCallback<T> delegate) {
    this.delegate = delegate;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public final void onFailure(Throwable caught) {
    // The basic idea here is a) handle things ourselves, then b) give it to the delegate
    // (unless the delegate is also of this class, in which case we handle is specially
    // to avoid displaying the same error twice). Meanwhile, catch and gracefully log all
    // the (many) possible problems that may arise along the way.

    try {
      beforeFailure();
    } catch (Throwable t) {
      log.log(Level.SEVERE, "Error in beforeFailure()", t);
    } finally {
      if (delegate != null && delegate instanceof Callback) {
        try {
          ((Callback) delegate).beforeFailure();
        } catch (Throwable t) {
          log.log(Level.SEVERE, "Error delegating to beforeFailure()", t);
        }
      }
    }
    try {
      throw caught;
    } catch (IncompatibleRemoteServiceException e) {
      errorHandler.displayError("New Version Available",
          "The server has been updated with a new version of this application.",
          "Refresh your browser to continue with the latest version.", e);
    } catch (StatusCodeException e) {
      if (e.getStatusCode() == 0) {
        // Can't easily distinguish between network and webauth here...maybe make another call to check?
        errorHandler.displayError("Communication Error",
            "Unable to communicate with the server, or your credentials expired.",
            "Check your network connection and continue, or refresh to login again.", e);
      } else if (e.getStatusCode() == 302) { // TODO this isn't working - actually get 0 for the redirect here
        // WebAuth redirects to weblogin.stanford.edu if your kerberos ticket is out of date,
        // so we assume here that all redirects mean we should reload the page, which will
        // also be redirected, so the user can login again
        Location.reload(); // TODO hit a specific reload/redirect URL that preserves UI state?
        // displayError("Your WebAuth credentials have expired - please refresh your browser", e.getEncodedResponse(), caught);
      } else if (e.getStatusCode() == 401) {
        // Session expiration
        errorHandler.displayError("Login Expired",
            "Your login has expired.",
            "Please reload the page to login again.", e);
      } else if (e.getStatusCode() == 403) {
        errorHandler.displayError("Unauthorized:", e.getEncodedResponse(),
            "You lack permission to use CHOIR at this url.", e);
      } else {
        errorHandler.displayError("Server Error", e.getEncodedResponse(),
            "If this continues, try refreshing this page in your browser.", e);
      }
    } catch (InvocationException e) {
      errorHandler.displayError("Communication Error", "Unable to communicate with the server.",
          "Check that you have network connectivity and are on the Stanford network (or VPN if remote).", e);
    } catch (Throwable e) {
      // By process of elimination it is probably a checked exception
      boolean handled = false;
      try {
        handled = handleCheckedExceptions(caught);
      } catch (Throwable t) {
        log.log(Level.SEVERE, "Error in handleCheckedExceptions()", t);
      }
      if (!handled && delegate != null && delegate instanceof Callback) {
        try {
          handled = ((Callback) delegate).handleCheckedExceptions(caught);
        } catch (Throwable t) {
          log.log(Level.SEVERE, "Error delegating to handleCheckedExceptions()", t);
        }
      }
      if (!handled && delegate != null && !(delegate instanceof Callback)) {
        try {
          delegate.onFailure(caught);
          handled = true;
        } catch (Throwable t) {
          log.log(Level.SEVERE, "Error delegating to onFailure()", t);
        }
      }
      if (!handled) {
        errorHandler.displayError("Unknown Error", "Try refreshing your browser.", e);
      }
    } finally {
      try {
        afterFailure();
      } catch (Throwable t) {
        t.printStackTrace();
        // log.log(Level.INFO, "Error in afterFailure()");
        log.log(Level.SEVERE, "Error in afterFailure()", t);
      } finally {
        if (delegate != null && delegate instanceof Callback) {
          try {
            ((Callback) delegate).afterFailure();
          } catch (Throwable t) {
            log.log(Level.SEVERE, "Error delegating to afterFailure()", t);
          }
        }
      }
    }
  }

  @Override
  public final void onSuccess(T result) {
    try {
      handleSuccess(result);
    } catch (Throwable t) {
      t.printStackTrace();
      // Log.debug("Error in handleSuccess()");
      log.log(Level.SEVERE, "Error in handleSuccess()", t);
      errorHandler.displayError("Error", "The application encountered a problem.", t);
    }
    if (delegate != null) {
      try {
        delegate.onSuccess(result);
      } catch (Throwable t) {
        t.printStackTrace();
        // Log.debug("Error delegating to onSuccess()");
        log.log(Level.SEVERE, "Error delegating to onSuccess()", t);
        errorHandler.displayError("Error", "The application encountered a problem.", t);
      }
    }
  }

  public void handleSuccess(T result) {
    // Nothing by default
  }

  /**
   * Hook for optionally handling exceptions. The afterFailure() method will be called regardless of whether you handle the error here.
   *
   * @return true if you handle it; false if you want someone else to
   */
  protected boolean handleCheckedExceptions(Throwable caught) throws Throwable {
    // Don't handle any checked exceptions by default
    return false;
  }

  /**
   * Optionally override if you need to clean up UI state. This will execute after the error dialog has been displayed to the user.
   */
  protected void afterFailure() {
    // Do nothing by default
  }

  /**
   * Optionally override if you need to clean up UI state. This will execute before the error is displayed to the user. A good place to
   * clear those modal dialogs.
   */
  protected void beforeFailure() {
    // Do nothing by default
  }
}
