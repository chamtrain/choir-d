package edu.stanford.survey.client.api;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasPlayerProgressHandlers extends HasHandlers {
       
    HandlerRegistration addPlayerProgressHandler(PlayerProgressHandler handler);

}
