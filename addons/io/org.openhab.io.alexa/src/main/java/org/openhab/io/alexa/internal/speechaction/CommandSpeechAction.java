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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.io.alexa.AlexaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class CommandSpeechAction extends SpeechAction {
    private Logger logger = LoggerFactory.getLogger(CommandSpeechAction.class);

    public CommandSpeechAction(ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(itemRegistry, eventPublisher);
    }

    @Override
    public SpeechletResponse getSpeechletResponse(Intent intent, Session session) {
        Slot labelSlot = intent.getSlot(AlexaConstants.SLOT_NAME_LABEL);
        Slot actionSlot = intent.getSlot(AlexaConstants.SLOT_NAME_COMMAND);
        if (labelSlot == null || labelSlot.getValue() == null) {
            logger.error("Could not retieve label from intent");
            return buildSpeechResponse("I did not recogize that device");
        }
        if (actionSlot == null || actionSlot.getValue() == null) {
            logger.error("Could not retieve action from intent");
            return buildSpeechResponse("I did not recogize that switch action");
        }

        String label = labelSlot.getValue();
        String command = actionSlot.getValue().toUpperCase();

        if (command.equals(AlexaConstants.CUSTOM_CMD_SLOT_BRIGHTEN)) {
            command = AlexaConstants.CUSTOM_CMD_SLOT_INCREASE;
        }
        if (command.equals(AlexaConstants.CUSTOM_CMD_SLOT_DIM)) {
            command = AlexaConstants.CUSTOM_CMD_SLOT_DECREASE;
        }

        Item item = getTagedItem(label);
        if (item != null) {
            State state = TypeParser.parseState(item.getAcceptedDataTypes(), command);
            if (state != null) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(),
                        TypeParser.parseCommand(item.getAcceptedCommandTypes(), command)));
            } else {
                return buildSpeechResponse(String.format("The command %s is not valid for %s", command, label));
            }
            return buildSpeechResponse("OK");
        } else {
            return buildSpeechResponse("I could not find a device named " + label);
        }
    }
}
