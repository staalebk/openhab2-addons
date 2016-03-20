/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.alexa;

public class AlexaConstants {
    public static final String BASE_PATH = "/alexa";
    public static final String API_PATH = BASE_PATH + "/api";

    public static final String HOMEKIT_PREFIX = "homekit:";

    public static final String INTENT_DEFAULT = "DefaultIntent";
    public static final String INTENT_UNKNOWN = "UnknowIntent";
    public static final String INTENT_COMMAND = "CommandIntent";
    public static final String INTENT_NUMBER = "NumberIntent";
    public static final String INTENT_ROLLERSHUTTER = "RollerShutterIntent";
    public static final String INTENT_GET_DEVICES = "GetDevicesIntent";
    public static final String INTENT_GET_DEVICE_STATUS = "GetDeviceStatusIntent";
    public static final String INTENT_CANCEL = "AMAZON.CancelIntent";
    public static final String INTENT_STOP = "AMAZON.StopIntent";
    public static final String INTENT_HELP = "AMAZON.HelpIntent";

    public static final String SLOT_NAME_LABEL = "Label";
    public static final String SLOT_NAME_COMMAND = "Command";
    public static final String SLOT_NAME_NUMBER = "Number";

    public static final String SLOT_TYPE_NUMBER = "AMAZON.NUMBER";
    public static final String SLOT_TYPE_LITERAL = "LITERAL";

    public static final String CUSTOM_CMD_SLOT_ON = "ON";
    public static final String CUSTOM_CMD_SLOT_OFF = "OFF";
    public static final String CUSTOM_CMD_SLOT_UP = "UP";
    public static final String CUSTOM_CMD_SLOT_DOWN = "DOWN";
    public static final String CUSTOM_CMD_SLOT_OPEN = "OPEN";
    public static final String CUSTOM_CMD_SLOT_CLOSE = "CLOSE";
    public static final String CUSTOM_CMD_SLOT_INCREASE = "INCREASE";
    public static final String CUSTOM_CMD_SLOT_DECREASE = "DECREASE";
    public static final String CUSTOM_CMD_SLOT_DIM = "DIM";
    public static final String CUSTOM_CMD_SLOT_BRIGHTEN = "BRIGHTEN";

    public static final String[] CUSTOM_CMD_SLOTS = { CUSTOM_CMD_SLOT_ON, CUSTOM_CMD_SLOT_OFF, CUSTOM_CMD_SLOT_UP,
            CUSTOM_CMD_SLOT_DOWN, CUSTOM_CMD_SLOT_OPEN, CUSTOM_CMD_SLOT_CLOSE, CUSTOM_CMD_SLOT_INCREASE,
            CUSTOM_CMD_SLOT_DECREASE, CUSTOM_CMD_SLOT_DIM, CUSTOM_CMD_SLOT_BRIGHTEN };

}
