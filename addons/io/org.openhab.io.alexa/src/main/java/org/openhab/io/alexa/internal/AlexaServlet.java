/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.alexa.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.alexa.AlexaConstants;
import org.openhab.io.alexa.internal.config.ConfigServlet;
import org.openhab.io.alexa.internal.speechaction.CancelSpeechAction;
import org.openhab.io.alexa.internal.speechaction.CommandSpeechAction;
import org.openhab.io.alexa.internal.speechaction.DefaultSpeechAction;
import org.openhab.io.alexa.internal.speechaction.GetDeviceStatus;
import org.openhab.io.alexa.internal.speechaction.GetDevicesAction;
import org.openhab.io.alexa.internal.speechaction.NumberSpeechAction;
import org.openhab.io.alexa.internal.speechaction.SpeechAction;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;

/**
 * Amazon Alexa Skills Servlet
 *
 * @author Dan Cunningham
 *
 */
public class AlexaServlet implements Speechlet {
    private Logger logger = LoggerFactory.getLogger(AlexaServlet.class);
    private HttpService httpService;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;

    private Map<String, SpeechAction> actions = new HashMap<String, SpeechAction>();

    protected void activate(Map<String, Object> config) {
        Dictionary<String, String> servletParams = new Hashtable<String, String>();
        System.setProperty("com.amazon.speech.speechlet.servlet.timestampTolerance", "150");
        SpeechletServlet servlet = new SpeechletServlet();
        servlet.setSpeechlet(this);

        try {
            httpService.registerServlet(AlexaConstants.API_PATH, servlet, servletParams,
                    httpService.createDefaultHttpContext());
            httpService.registerServlet(AlexaConstants.BASE_PATH, new ConfigServlet(itemRegistry), servletParams,
                    httpService.createDefaultHttpContext());
        } catch (ServletException e) {
            logger.error("Could not start servlet", e);
        } catch (NamespaceException e) {
            logger.error("Could not start servlet", e);
        }

        actions.put(AlexaConstants.INTENT_COMMAND, new CommandSpeechAction(itemRegistry, eventPublisher));
        actions.put(AlexaConstants.INTENT_NUMBER, new NumberSpeechAction(itemRegistry, eventPublisher));
        actions.put(AlexaConstants.INTENT_GET_DEVICES, new GetDevicesAction(itemRegistry, eventPublisher));
        actions.put(AlexaConstants.INTENT_GET_DEVICE_STATUS, new GetDeviceStatus(itemRegistry, eventPublisher));
        DefaultSpeechAction defaultAction = new DefaultSpeechAction(itemRegistry, eventPublisher);
        actions.put(AlexaConstants.INTENT_DEFAULT, defaultAction);
        actions.put(AlexaConstants.INTENT_HELP, defaultAction);
        CancelSpeechAction cancelAction = new CancelSpeechAction(itemRegistry, eventPublisher);
        actions.put(AlexaConstants.INTENT_CANCEL, cancelAction);
        actions.put(AlexaConstants.INTENT_STOP, cancelAction);
    }

    protected void modified(Map<String, ?> config) {
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            httpService.unregister(AlexaConstants.BASE_PATH);
            httpService.unregister(AlexaConstants.BASE_PATH + "/config");
        } catch (IllegalArgumentException ignored) {
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
        logger.debug("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
        logger.debug("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        SpeechAction action = actions.get(AlexaConstants.INTENT_DEFAULT);
        return action.getSpeechletResponse(null, session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        logger.debug("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        SpeechAction action = actions.get(intentName);
        if (action == null) {
            action = actions.get(AlexaConstants.INTENT_UNKNOWN);
        }
        return action.getSpeechletResponse(intent, session);
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
        logger.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }
}
