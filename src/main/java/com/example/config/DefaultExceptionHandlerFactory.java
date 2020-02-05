package com.example.config;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class DefaultExceptionHandlerFactory extends ExceptionHandlerFactory {

    public DefaultExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        super(wrapped);
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler parentHandler = getWrapped().getExceptionHandler();
        return new DefaultExceptionHandler(parentHandler);
    }

}
