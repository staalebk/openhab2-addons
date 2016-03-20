/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.alexa.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.alexa.AlexaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ConfigServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(ConfigServlet.class);
    private static final String JETTY_KEYSTORE_PATH_PROPERTY = "jetty.keystore.path";
    private static final String KEYSTORE_PASSWORD = "openhab";
    private static final String KEYSTORE_ENTRY_ALIAS = "mykey";
    private static final String KEYSTORE_JKS_TYPE = "JKS";
    private static final String[] SWITCH_PREFIX = { "", "turn", "to turn", "set", "to set", "flip", "to flip" };
    private static final String[] NUMBER_PREFIX = { "", "turn", "to turn", "set", "to set", "dim", "to dim" };
    private static final String[] POSTFIX = { "", " lights" };
    private ItemRegistry registery;
    private String configHtml;
    private String intentsJson;

    public ConfigServlet(ItemRegistry registery) {
        this.registery = registery;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (configHtml == null) {
            configHtml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("config.html"), "UTF-8");
        }
        if (intentsJson == null) {
            intentsJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("intents.json"), "UTF-8");
        }

        String utterances = getUtterances();
        logger.debug("Utterances\n{} ", utterances);

        StringBuilder cmdSlots = new StringBuilder();
        for (String slot : AlexaConstants.CUSTOM_CMD_SLOTS) {
            cmdSlots.append(slot).append("\n");
        }
        logger.debug("cmdSlots\n{} ", cmdSlots.toString());

        String labelSlots = getLabelSlots();
        logger.debug("labelSlots\n{} ", labelSlots.toString());

        String key = getPublicKey();
        if (key == null) {
            key = "Could not retrieve public certificate";
        }
        logger.debug("Key\n{} ", key);

        PrintWriter out = resp.getWriter();
        // path, utterances, cmd slot, label slot, intents, cert
        out.println(String.format(configHtml, AlexaConstants.API_PATH, utterances, cmdSlots.toString(), labelSlots,
                intentsJson, key));
        out.close();
    }

    private String getUtterances() {
        StringBuilder sb = new StringBuilder();

        for (String prefix : SWITCH_PREFIX) {
            for (String postfix : POSTFIX) {
                // tell openhab to turn kicthen lights on
                sb.append(String.format("CommandIntent %s {Label}%s {Command}\n", prefix, postfix));
                // tell openhab to turn on kicthen lights
                sb.append(String.format("CommandIntent %s {Command} {Label}%s\n", prefix, postfix));
            }
        }

        for (String prefix : NUMBER_PREFIX) {
            for (String postfix : POSTFIX) {
                // tell openhab to set thermostats to 72
                sb.append(String.format("NumberIntent %s {Label}%s to {Number}\n", prefix, postfix));
            }
        }

        for (String prefix : NUMBER_PREFIX) {
            for (String postfix : POSTFIX) {
                // tell openhab to set lights to 100 percent
                sb.append(String.format("NumberIntent %s {Label}%s to {Number} percent\n", prefix, postfix));
            }
        }

        // get device status
        for (String postfix : POSTFIX) {
            sb.append(String.format("GetDeviceStatusIntent what is the status of {Label}%s\n", postfix));
            sb.append(String.format("GetDeviceStatusIntent to get the status of {Label}%s \n", postfix));
            sb.append(String.format("GetDeviceStatusIntent get {Label}%s status\n", postfix));
            sb.append(String.format("GetDeviceStatusIntent to get {Label}%s status\n", postfix));
        }

        // list devices
        sb.append("GetDevicesIntent get devices\n");
        sb.append("GetDevicesIntent to get devices\n");
        sb.append("GetDevicesIntent list devices\n");
        sb.append("GetDevicesIntent to list devices\n");

        return sb.toString();
    }

    public String getLabelSlots() {
        StringBuilder sb = new StringBuilder();
        // item List is to avoid duplicate labels
        List<String> items = new ArrayList<String>();
        for (Item item : registery.getItems()) {
            for (String tag : item.getTags()) {
                if (tag.startsWith(AlexaConstants.HOMEKIT_PREFIX)) {
                    String label = item.getLabel();
                    if (!items.contains(label)) {
                        sb.append(label).append("\n");
                        items.add(label);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * This reads the local OH keystore and tries to get the public x509 key in Base64 format
     *
     * @return
     */
    private String getPublicKey() {
        try {
            String keystorePath = System.getProperty(JETTY_KEYSTORE_PATH_PROPERTY);
            File keystoreFile = new File(keystorePath);
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_JKS_TYPE);
            if (keystoreFile.exists()) {
                InputStream keystoreStream = new FileInputStream(keystoreFile);
                logger.debug("Keystore found. Trying to load {}", keystoreFile.getAbsolutePath());
                keyStore.load(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
                Certificate cert = keyStore.getCertificate(KEYSTORE_ENTRY_ALIAS);
                PublicKey publicKey = cert.getPublicKey();
                logger.debug("Key format {}", publicKey.getFormat());
                return Base64.encodeBase64String(publicKey.getEncoded());
            } else {
                logger.error("keystore not found");
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
                | NullPointerException e) {
            logger.error("Could not load keystore", e);
        }
        return null;
    }
}
