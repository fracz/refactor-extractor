/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.maxcube.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.maxcube.MaxCubeBindingProvider;
import org.openhab.binding.maxcube.internal.message.C_Message;
import org.openhab.binding.maxcube.internal.message.Configuration;
import org.openhab.binding.maxcube.internal.message.H_Message;
import org.openhab.binding.maxcube.internal.message.L_Message;
import org.openhab.binding.maxcube.internal.message.M_Message;
import org.openhab.binding.maxcube.internal.message.Message;
import org.openhab.binding.maxcube.internal.message.MessageType;
import org.openhab.core.binding.AbstractActiveBinding;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RefreshService polls the MAX!Cube frequently and updates
 * the list of configurations and devices.
 * The interval is  can be changed via openhab.cfg.
 *
 * @author Andreas Heil
 * @since 1.4.0
 */
public class MaxCubeBinding extends
		AbstractActiveBinding<MaxCubeBindingProvider> implements ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(MaxCubeBinding.class);

	/** The IP address of the MAX!Cube LAN gateway */
	private static String ip;

	/**
	 * The port of the MAX!Cube LAN gateway as provided at
	 * http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484
	 */
	private static int port;

	/** the port to use for connecting to a given host (defaults to 5000) */
	// TODO: private int timeout = 5000;

	/** the refresh interval which is used to poll given MAX!Cube */
	private static long refreshInterval;

	@Override
	protected String getName() {
		return "MAX!Cube Refresh Service";
	}

	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	@Override
	public void activate() {
		super.activate();
		setProperlyConfigured(true);
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void execute() {

		ArrayList<Configuration> configurations = new ArrayList<Configuration>();

		Socket socket = null;
		BufferedReader reader = null;

		try {
			String raw = null;

			socket = new Socket(ip, port);

			reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			boolean cont = true;
			while (cont) {
				raw = reader.readLine();
				if (raw == null) {
					cont = false;
					continue;
				}

				Message message;
				try {
					message = processRawMessage(raw);

					if (message != null) {

						// the L message is the last one, while the reader would
						// hang trying to read a new line
						// and eventually the cube will fail to establish new
						// connections for some time
						if (message.getType() == MessageType.C) {
							configurations.add(Configuration.create(message));
						}
						else if (message.getType() == MessageType.L) {
							cont = false;
						}

						message.debug();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			socket.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}

		// for (MaxCubeBindingProvider provider : providers) {
		// for (String itemName : provider.getItemNames()) {
		//
		// String hostname = provider.getHostname(itemName);
		// int port = provider.getPort(itemName);
		//
		// if (provider.getTimeout(itemName) > 0) {
		// timeout = provider.getTimeout(itemName);
		// }
		//
		// boolean success = false;
		//
		// try {
		// success = Ping.checkVitality(hostname, port, timeout);
		//
		// logger.debug("established connection [host '{}' port '{}' timeout '{}']",
		// new Object[] {hostname, port, timeout});
		// }
		// catch (SocketTimeoutException se) {
		// logger.debug("timed out while connecting to host '{}' port '{}' timeout '{}'",
		// new Object[] {hostname, port, timeout});
		// }
		// catch (IOException ioe) {
		// logger.debug("couldn't establish network connection [host '{}' port '{}' timeout '{}']",
		// new Object[] {hostname, port, timeout});
		// }
		// if(eventPublisher!=null) {
		// eventPublisher.postUpdate(itemName, success ? OnOffType.ON :
		// OnOffType.OFF);
		// }
		// }
		// }
	}

	private Message processRawMessage(String raw) throws Exception {

		if (raw.startsWith("H:")) {
			return new H_Message(raw);
		} else if (raw.startsWith("M:")) {
			return new M_Message(raw);
		} else if (raw.startsWith("C:")) {
			return new C_Message(raw);
		}
		if (raw.startsWith("L:")) {
			return new L_Message(raw);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		if (config != null) {
			//String timeoutString = (String) config.get("timeout");
			// if (timeoutString != null && !timeoutString.isEmpty()) {
			// timeout = Integer.parseInt(timeoutString);
			// }

			ip = (String) config.get("ip");
			if (StringUtils.isBlank(ip)) {
				throw new ConfigurationException("maxcube:ip",
						"IP address for MAX!Cube must be set");
			}

			// TODO: proper exception handling, test for negative ports, above
			// high ports and non-integers
			// TODO make paramter optional
			port = Integer.parseInt((String) config.get("port"));

			// TODO: proper exception handling, test for negative ports, above
			// high ports and non-integers
			// TODO: make refreshinterval optional
			//			String refreshIntervalString = (String) config.get("refresh");
			//			if (refreshIntervalString != null
			//					&& !refreshIntervalString.isEmpty()) {
			//				refreshInterval = Long.parseLong(refreshIntervalString);
			//			}
			// TODO make paramter optional
			refreshInterval = Integer.parseInt((String) config
					.get("refreshIntervall"));

		}
	}
}