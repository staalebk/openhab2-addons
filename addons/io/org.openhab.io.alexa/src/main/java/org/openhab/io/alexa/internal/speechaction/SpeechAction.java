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
import org.openhab.io.alexa.AlexaConstants;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

public abstract class SpeechAction {

    protected ItemRegistry itemRegistry;
    protected EventPublisher eventPublisher;

    public SpeechAction(ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
    }

    public abstract SpeechletResponse getSpeechletResponse(Intent intent, Session session);

    protected SpeechletResponse buildSpeechResponse(String speechText) {
        return buildSpeechResponse(speechText, null);
    }

    protected SpeechletResponse buildSpeechResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (repromptText != null) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    protected Item getTagedItem(String label) {
        for (Item item : itemRegistry.getItems()) {
            for (String tag : item.getTags()) {
                if (tag.startsWith(AlexaConstants.HOMEKIT_PREFIX)) {
                    String itemLabel = item.getLabel();
                    if (itemLabel != null && itemLabel.toLowerCase().trim().equals(label.toLowerCase())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }
}
