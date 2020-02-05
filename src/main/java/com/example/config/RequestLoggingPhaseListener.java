package com.example.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import javax.inject.Inject;

public class RequestLoggingPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Inject
    Logger LOG;

    @Override
    public void afterPhase(PhaseEvent event) {
        LOG.log(Level.INFO, "after phase:{0}", event.getPhaseId());
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        LOG.log(Level.INFO, "before phase:{0}", event.getPhaseId());
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
