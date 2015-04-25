/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.discovery;

import static org.openhab.binding.squeezebox.SqueezeBoxBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.squeezebox.SqueezeBoxBindingConstants;
import org.openhab.binding.squeezebox.internal.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SqueezeBoxServerDiscoveryService sends a UDP message on port 3483 to the
 * network in order to trigger listening Squeeze Servers to advertise
 * themselves.
 * 
 * @author Dan Cunningham
 *
 */
public class SqueezeBoxServerDiscoveryService extends AbstractDiscoveryService {
	private final Logger logger = LoggerFactory
			.getLogger(SqueezeBoxServerDiscoveryService.class);

	/**
	 * Give us 5 seconds to find servers
	 */
	private static int TIMEOUT = 5;
	/**
	 * Port SqueezeServers listen on for discovery requestd
	 */
	private static final int DISCO_PORT = 3483;
	/**
	 * Discovery packet we send on the network to find Squeeze Servers
	 */
	private String discoPacket = "eIPAD\000NAME\000JSON\000VERS\000UUID\000JVID\006\012\034\056\078\012\034";
	/**
	 * Discovery replay packet containing the name, web port, version and UUID
	 * of a Squeeze Server
	 */
	private Pattern replyPattern = Pattern
			.compile("ENAME(.*?)JSON(.*?)VERS(.*?)UUID\\$(.*?)");
	/**
	 * When true we will keep sending out packets
	 */
	private boolean discoveryRunning;
	/**
	 * our Discovery socket
	 */
	private DatagramSocket socket;

	/**
	 * Looks for Squeeze Servers on the local network
	 */
	public SqueezeBoxServerDiscoveryService() {
		super(Collections.singleton(SQUEEZEBOXSERVER_THING_TYPE), TIMEOUT, true);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return Collections.singleton(SQUEEZEBOXSERVER_THING_TYPE);
	}

	@Override
	protected void startScan() {
		scanServer();
	}

	@Override
	protected void startBackgroundDiscovery() {
		scanServer();
	}

	@Override
	protected void stopScan() {
		super.stopScan();
		discoveryRunning = false;
	}

	@Override
	protected void stopBackgroundDiscovery() {
		discoveryRunning = false;
	}

	/**
	 * Scans for Squeeze Servers
	 */
	private synchronized void scanServer() {
		logger.debug("Scanning Server");
		discoveryRunning = true;
		Thread thread = new Thread("SqueezeBox Sendbroadcast") {
			public void run() {
				sendDiscoveryMessage();
				discoveryRunning = false;
				logger.trace("Done sending broadcast discovery messages.");
			}
		};

		try {
			// setup socket
			startSocket();
			thread.start();
			receiveDiscoveryMessage();
		} catch (Exception e) {
			logger.debug("Problem scanning for Squeeze Server", e);
		} finally {
			// make sure we clean up.
			try {
				stopSocket();
			} catch (Exception ignored) {

			}
		}
	}

	/**
	 * Creates our UDP socket
	 */
	private void startSocket() throws Exception {

		socket = new DatagramSocket();
		socket.setBroadcast(true);
		socket.setSoTimeout(10000);
	}

	/**
	 * Stops and cleans up our UDP socket
	 */
	private void stopSocket() throws Exception {
		if (socket != null)
			socket.close();
	}

	/**
	 * Based off the MaxCube discovery code, thanks! Sends discovery messages on
	 * the network.
	 * 
	 * @param discoverString
	 */
	private void sendDiscoveryMessage() {
		try {
			byte[] sendData = discoPacket.getBytes();
			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces
						.nextElement();
				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}
				for (InterfaceAddress interfaceAddress : networkInterface
						.getInterfaceAddresses()) {
					InetAddress[] broadcast = new InetAddress[3];
					broadcast[0] = InetAddress.getByName("224.0.0.1");
					broadcast[1] = InetAddress.getByName("255.255.255.255");
					broadcast[2] = interfaceAddress.getBroadcast();
					for (InetAddress bc : broadcast) {
						if (bc != null) {
							try {
								logger.trace("Broadcasting {}", sendData);
								DatagramPacket sendPacket = new DatagramPacket(
										sendData, sendData.length, bc,
										DISCO_PORT);
								socket.send(sendPacket);
							} catch (IOException e) {
								logger.debug(
										"IO error during SqueezeBoxServer discovery: {}",
										e.getMessage());
							} catch (Exception e) {
								logger.debug(e.getMessage(), e);
							}
							logger.trace(
									"Request packet sent to: {} Interface: {}",
									bc.getHostAddress(),
									networkInterface.getDisplayName());
						}
					}
				}
			}
			logger.trace("Done looping over all network interfaces. Now waiting for a reply!");

		} catch (IOException e) {
			logger.debug("IO error during SqueezeBoxServer discovery: {}",
					e.getMessage());
		}
	}

	/**
	 * Based off the MaxCube discovery code, thanks! Listens for replies to our
	 * discovery message
	 */
	private void receiveDiscoveryMessage() {
		try {
			while (discoveryRunning) {
				// Wait for a response
				byte[] recvBuf = new byte[1500];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf,
						recvBuf.length);
				logger.trace("Waiting for packets");
				socket.receive(receivePacket);

				String message = new String(receivePacket.getData()).trim();
				logger.trace("Broadcast response from {} : {} '{}'",
						receivePacket.getAddress(), message.length(), message);
				Matcher matcher = replyPattern.matcher(message);
				if (matcher.matches()) {
					// we have a packet
					String ip = receivePacket.getAddress().getHostAddress();
					String name = matcher.group(1).trim();
					String webPort = matcher.group(2).trim();
					String uuid = matcher.group(4).trim().toUpperCase();

					logger.debug("Found server {} at {}:{} with uuid {}", name,
							ip, webPort, uuid);

					try {
						int cliPort = HttpUtils.getCliPort(ip,
								Integer.parseInt(webPort));

						Map<String, Object> properties = new HashMap<>(2);
						properties.put("ipAddress", ip);
						properties.put("webport", new Integer(webPort));
						properties.put("cliPort", new Integer(cliPort));

						ThingUID uid = new ThingUID(
								SqueezeBoxBindingConstants.SQUEEZEBOXSERVER_THING_TYPE,
								uuid);

						DiscoveryResult result = DiscoveryResultBuilder
								.create(uid)
								.withProperties(properties)
								.withLabel(
										"Logitech Media Server [" + name + "]")
								.build();

						thingDiscovered(result);
					} catch (Exception e) {
						logger.trace("Could not discover CLI port of server", e);
					}

				}
			}
		} catch (SocketTimeoutException e) {
			logger.trace("SqueezeBoxServer Failed to respond");
		} catch (Exception e) {
			logger.debug("error during SqueezeBoxServer discovery", e);
		}
	}
}
