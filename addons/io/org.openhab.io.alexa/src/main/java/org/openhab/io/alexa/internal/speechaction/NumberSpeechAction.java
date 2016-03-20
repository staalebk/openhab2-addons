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
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.io.alexa.AlexaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class NumberSpeechAction extends SpeechAction {
    private Logger logger = LoggerFactory.getLogger(NumberSpeechAction.class);

    public NumberSpeechAction(ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(itemRegistry, eventPublisher);
    }

    @Override
    public SpeechletResponse getSpeechletResponse(Intent intent, Session session) {
        Slot labelSlot = intent.getSlot(AlexaConstants.SLOT_NAME_LABEL);
        Slot numberSlot = intent.getSlot(AlexaConstants.SLOT_NAME_NUMBER);

        if (labelSlot == null || labelSlot.getValue() == null) {
            logger.error("Could not retieve label from intent");
            return buildSpeechResponse("I did not recogize that device");
        }
        if (numberSlot == null || numberSlot.getValue() == null) {
            logger.error("Could not retieve number from intent");
            return buildSpeechResponse("I did not recogize that set action");
        }
        String label = labelSlot.getValue();
        String number = numberSlot.getValue();
        Item item = getTagedItem(label);

        if (item != null) {
            logger.debug("Posting command {} to {}", number, item.getName());
            // eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), new DecimalType(number)));
            Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), number);
            eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), command));
            return buildSpeechResponse("OK");
        } else {
            return buildSpeechResponse("I could not find a device named " + label);
        }
    }
}
