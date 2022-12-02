package com.example.config;

import com.example.domain.TaskNotFoundException;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.ViewHandler;

public class DefaultExceptionHandler extends ExceptionHandlerWrapper {

    private final static Logger LOG = Logger.getLogger(DefaultExceptionHandler.class.getName());

    public DefaultExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        LOG.log(Level.INFO, ">>>>>>>>invoking custom ExceptionHandlder...");
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();

        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext context =  event.getContext();
            Throwable t = context.getException();
            LOG.log(Level.INFO, "Exception@{0}", t.getClass().getName());
            LOG.log(Level.INFO, "ExceptionHandlder began.");
            //t.printStackTrace();
            if (t instanceof ViewExpiredException) {
                try {
                    handleViewExpiredException((ViewExpiredException) t);
                } finally {
                    events.remove();
                }
            } else if (t instanceof TaskNotFoundException) {
                try {
                    handleNotFoundException((TaskNotFoundException) t);
                } finally {
                    events.remove();
                }
            } else {
                
            }
            
            LOG.log(Level.INFO, "ExceptionHandlder end.");
        }
        getWrapped().handle();

    }

    private void handleViewExpiredException(ViewExpiredException vee) {
        LOG.log(Level.INFO, " handling viewExpiredException{0}", vee.getMessage());
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = vee.getViewId();
        LOG.log(Level.INFO, "view id @{0}", viewId);
        NavigationHandler nav
                = context.getApplication().getNavigationHandler();
        nav.handleNavigation(context, null, viewId);
        context.renderResponse();
    }

    private void handleNotFoundException(TaskNotFoundException e) {
        LOG.log(Level.INFO, "handling exception:{0}", e.getMessage());
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = "/error.xhtml";
        LOG.log(Level.INFO, "view id @{0}", viewId);

        ViewHandler viewHandler = context.getApplication().getViewHandler();
        context.setViewRoot(viewHandler.createView(context, viewId));
        context.getViewRoot().getViewMap(true).put("ex", e);
        context.getPartialViewContext().setRenderAll(true);
        context.renderResponse();
    }
}
