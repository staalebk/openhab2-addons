/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.alexa.internal.speechaction;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class UnknownSpeechAction extends SpeechAction {

    public UnknownSpeechAction(ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(itemRegistry, eventPublisher);
    }

    @Override
    public SpeechletResponse getSpeechletResponse(Intent intent, Session session) {
        return buildSpeechResponse("I don't understand that command");
    }
}
