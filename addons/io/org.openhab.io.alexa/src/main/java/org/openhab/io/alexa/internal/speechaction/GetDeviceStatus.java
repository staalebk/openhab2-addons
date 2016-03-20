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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.io.alexa.AlexaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class GetDeviceStatus extends SpeechAction {
    private Logger logger = LoggerFactory.getLogger(GetDeviceStatus.class);

    public GetDeviceStatus(ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(itemRegistry, eventPublisher);
    }

    @Override
    public SpeechletResponse getSpeechletResponse(Intent intent, Session session) {
        Slot labelSlot = intent.getSlot(AlexaConstants.SLOT_NAME_LABEL);

        if (labelSlot == null || labelSlot.getValue() == null) {
            logger.error("Could not retieve label from intent");
            return buildSpeechResponse("I did not hear the device name");
        }

        String label = labelSlot.getValue();
        Item item = getTagedItem(label);
        if (item != null) {
            State state = item.getState();
            String resp = null;
            if (state == null || state instanceof UnDefType) {
                resp = String.format("%s is not initialized", item.getLabel());
            } else {
                StateDescription desc = item.getStateDescription();
                if (desc != null) {
                    String pattern = desc.getPattern();
                    if (pattern != null && pattern.length() > 0) {
                        // all of this is to avoid 'd != java.lang.String'
                        if (pattern.contains("%d")) {
                            state = item.getState();
                            if (!(state instanceof DecimalType)) {
                                state = item.getStateAs(DecimalType.class);
                            }
                        } else {
                            state = item.getState();
                        }
                        resp = String.format("%s is %s", item.getLabel(), state.format(pattern));
                    }
                }
                if (resp == null) {
                    resp = String.format("%s is %s", item.getLabel(), item.getState().toString());
                }
            }
            return buildSpeechResponse(resp);
        } else {
            return buildSpeechResponse("I could not find a device named " + label);
        }
    }
}
