/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcul.internal;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.maxcul.MaxCulBindingProvider;
import org.openhab.binding.maxcul.internal.message.sequencers.PairingInitialisationSequence;
import org.openhab.binding.maxcul.internal.message.sequencers.TimeUpdateRequestSequence;
import org.openhab.binding.maxcul.internal.messages.BaseMsg;
import org.openhab.binding.maxcul.internal.messages.MaxCulBindingMessageProcessor;
import org.openhab.binding.maxcul.internal.messages.MaxCulMsgType;
import org.openhab.binding.maxcul.internal.messages.PairPingMsg;
import org.openhab.binding.maxcul.internal.messages.SetTemperatureMsg;
import org.openhab.binding.maxcul.internal.messages.ThermostatStateMsg;
import org.openhab.binding.maxcul.internal.messages.TimeInfoMsg;
import org.openhab.binding.maxcul.internal.messages.WallThermostatControlMsg;
import org.openhab.binding.maxcul.internal.messages.WallThermostatStateMsg;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.io.transport.cul.CULDeviceException;
import org.openhab.io.transport.cul.CULHandler;
import org.openhab.io.transport.cul.CULManager;
import org.openhab.io.transport.cul.CULMode;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 *
 * @author Paul Hampson (cyclingengineer)
 * @since 1.6.0
 */
public class MaxCulBinding extends AbstractActiveBinding<MaxCulBindingProvider>
		implements ManagedService, MaxCulBindingMessageProcessor {

	private static final Logger logger = LoggerFactory
			.getLogger(MaxCulBinding.class);

	/**
	 * the refresh interval which is used to poll values from the MaxCul server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	/**
	 * The device that is used to access the CUL hardware
	 */
	private String accessDevice;

	/**
	 * This provides access to the CULFW device (e.g. USB stick)
	 */
	private CULHandler cul;

	/**
	 * This sets the address of the controller i.e. us!
	 */
	private final String srcAddr = "010203";

	/**
	 * Set default group ID
	 */
	private final byte DEFAULT_GROUP_ID = 0x1;

	/**
	 * Flag to indicate if we are in pairing mode. Default timeout is 60
	 * seconds.
	 */
	private boolean pairMode = false;
	private int pairModeTimeout = 60000;
	private int PACED_TRANSMIT_TIME = 10000;

	private Map<String, Timer> timers = new HashMap<String, Timer>();
	private Map<MaxCulBindingConfig, Timer> pacedBindingTransmitTimers = new HashMap<MaxCulBindingConfig, Timer>();

	MaxCulMsgHandler messageHandler;

	private String tzStr;

	public MaxCulBinding() {
	}

	public void activate() {
		super.activate();
		setProperlyConfigured(false);
		logger.debug("Activating MaxCul binding");
	}

	public void deactivate() {
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
		logger.debug("De-Activating MaxCul binding");
		if (cul != null) {
			cul.unregisterListener(messageHandler);
			CULManager.close(cul);
			logger.debug("CUL IO should now be closed");
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "MaxCul Refresh Service";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		messageHandler.checkPendingAcks();
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(final String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		Timer pairModeTimer = null;

		MaxCulBindingConfig bindingConfig = null;
		for (MaxCulBindingProvider provider : super.providers) {
			bindingConfig = provider.getConfigForItemName(itemName);
			if (bindingConfig != null) {
				break;
			}
		}
		logger.debug("Received command " + command.toString() + " for item "
				+ itemName);
		if (bindingConfig != null) {
			logger.debug("Found config for " + itemName);

			switch (bindingConfig.deviceType) {
			case PAIR_MODE:
				if ((command instanceof OnOffType)) {
					switch ((OnOffType) command) {
					case ON:
						/*
						 * turn on pair mode and schedule disabling of pairing
						 * mode
						 */
						pairMode = true;
						TimerTask task = new TimerTask() {
							public void run() {
								logger.debug(itemName
										+ " pairMode time out executed");
								pairMode = false;
								eventPublisher.postUpdate(itemName,
										OnOffType.OFF);
							}
						};
						pairModeTimer = timers.get(itemName);
						if (pairModeTimer != null) {
							pairModeTimer.cancel();
							timers.remove(itemName);
						}
						pairModeTimer = new Timer();
						timers.put(itemName, pairModeTimer);
						pairModeTimer.schedule(task, pairModeTimeout);
						logger.debug(itemName
								+ " pairMode enabled & timeout scheduled");
						break;
					case OFF:
						/*
						 * we are manually disabling, so clear the timer and the
						 * flag
						 */
						pairMode = false;
						pairModeTimer = timers.get(itemName);
						if (pairModeTimer != null) {
							logger.debug(itemName + " pairMode timer cancelled");
							pairModeTimer.cancel();
							timers.remove(itemName);
						}
						logger.debug(itemName + " pairMode cleared");
						break;
					}
				} else
					logger.warn("Command not handled for "
							+ bindingConfig.deviceType
							+ " that is not OnOffType");
				break;
			case LISTEN_MODE:
				if (command instanceof OnOffType) {
					this.messageHandler
							.setListenMode(((OnOffType) command == OnOffType.ON));
				} else
					logger.warn("Command not handled for "
							+ bindingConfig.deviceType
							+ " that is not OnOffType");
				break;
			case RADIATOR_THERMOSTAT:
			case RADIATOR_THERMOSTAT_PLUS:
			case WALL_THERMOSTAT:
				if (bindingConfig.feature == MaxCulFeature.THERMOSTAT) {
					/* clear out old pacing timer */
					if (pacedBindingTransmitTimers.containsKey(bindingConfig))
					{
						pacedBindingTransmitTimers.get(bindingConfig).cancel();
						pacedBindingTransmitTimers.remove(bindingConfig);
					}
					/* schedule new timer */
					Timer pacingTimer = new Timer();
					pacedBindingTransmitTimers.put(bindingConfig, pacingTimer);
					pacingTimer.schedule(new MaxCulPacedThermostatTransmitTask(command, bindingConfig, messageHandler), PACED_TRANSMIT_TIME);
				} else
					logger.warn("Command not handled for "
							+ bindingConfig.deviceType
							+ " that is not OnOffType or DecimalType");
				break;
			default:
				logger.warn("Command not handled for "
						+ bindingConfig.deviceType);
				break;
			}
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		logger.debug("MaxCUL Reading config");
		if (config != null) {

			// to override the default refresh interval one has to add a
			// parameter to openhab.cfg like maxcul:refresh=<intervalInMs>
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}

			// handle timezone configuration
			String timezoneString = (String) config.get("timezone");
			if (StringUtils.isNotBlank(timezoneString)) {
				this.tzStr = timezoneString;
			} else {
				this.tzStr = "Europe/London";
			}

			// handle device config
			String deviceString = (String) config.get("device");
			if (StringUtils.isNotBlank(deviceString)) {
				logger.debug("Setting up device " + deviceString);
				setupDevice(deviceString);
				if (cul == null)
					throw new ConfigurationException("device",
							"Configuration failed. Unable to access CUL device "
									+ deviceString);
			} else {
				setProperlyConfigured(false);
				throw new ConfigurationException("device",
						"No device set - please set one");
			}

			setProperlyConfigured(true);
		}
	}

	private void setupDevice(String device) {
		if (cul != null) {
			CULManager.close(cul);
		}
		try {
			accessDevice = device;
			logger.debug("Opening CUL device on " + accessDevice);
			cul = CULManager.getOpenCULHandler(accessDevice, CULMode.MAX);
			messageHandler = new MaxCulMsgHandler(this.srcAddr, cul);
			messageHandler.registerMaxCulBindingMessageProcessor(this);
		} catch (CULDeviceException e) {
			logger.error("Cannot open CUL device", e);
			cul = null;
			accessDevice = null;
		}
	}

	private Collection<MaxCulBindingConfig> getBindingsBySerial(String serial) {
		Collection<MaxCulBindingConfig> bindingConfigs = null;
		for (MaxCulBindingProvider provider : super.providers) {
			bindingConfigs = provider.getConfigsForSerialNumber(serial);
			if (bindingConfigs != null)
				break;
		}
		if (bindingConfigs == null) {
			logger.error("Unable to find configuration for serial " + serial
					+ ". Do you have a binding for it?");
			return null;
		}
		return bindingConfigs;
	}

	@Override
	public void MaxCulMsgReceived(String data, boolean isBroadcast) {
		logger.debug("Received data from CUL: " + data);

		MaxCulMsgType msgType = BaseMsg.getMsgType(data);
		/*
		 * Check if it's broadcast and we're in pair mode or a PAIR_PING message
		 * directly for us
		 */
		if (((pairMode && isBroadcast) || BaseMsg.isForUs(data, this.srcAddr))
				&& msgType == MaxCulMsgType.PAIR_PING) {
			logger.debug("Got PAIR_PING message");
			/* process packet */
			PairPingMsg pkt = new PairPingMsg(data);

			/* Match serial number to binding configuration */
			Collection<MaxCulBindingConfig> bindingConfigs = getBindingsBySerial(pkt.serial);

			/* Set pairing information */
			for (MaxCulBindingConfig bc : bindingConfigs)
				bc.setPairedInfo(pkt.srcAddrStr); /* where it came from gives the addr of the device */

			/* start the initialisation sequence */
			PairingInitialisationSequence ps = new PairingInitialisationSequence(
					this.DEFAULT_GROUP_ID, this.tzStr, messageHandler);
			messageHandler.startSequence(ps, pkt);
		} else {
			switch (msgType) {
			/* TODO handle all other incoming messages */
			case WALL_THERMOSTAT_CONTROL:
				WallThermostatControlMsg wallThermCtrlMsg = new WallThermostatControlMsg(
						data);
				wallThermCtrlMsg.printMessage();
				for (MaxCulBindingProvider provider : super.providers) {
					Collection<MaxCulBindingConfig> bindingConfigs = provider
							.getConfigsForRadioAddr(wallThermCtrlMsg.srcAddrStr);
					for (MaxCulBindingConfig bc : bindingConfigs) {
						if (bc.feature == MaxCulFeature.THERMOSTAT && wallThermCtrlMsg.getDesiredTemperature() != null) {
							String itemName = provider.getItemNameForConfig(bc);
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(wallThermCtrlMsg.getDesiredTemperature()));
						} else if (bc.feature == MaxCulFeature.TEMPERATURE && wallThermCtrlMsg.getMeasuredTemperature() != null ) {
							String itemName = provider.getItemNameForConfig(bc);
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(wallThermCtrlMsg.getMeasuredTemperature()));
						}
					}
				}

				/* reply only if not broadcast */
				if (isBroadcast == false)
					this.messageHandler.sendAck(wallThermCtrlMsg);
				break;
			case SET_TEMPERATURE:
				SetTemperatureMsg setTempMsg = new SetTemperatureMsg(data);
				setTempMsg.printMessage();
				for (MaxCulBindingProvider provider : super.providers) {
					Collection<MaxCulBindingConfig> bindingConfigs = provider
							.getConfigsForRadioAddr(setTempMsg.srcAddrStr);
					for (MaxCulBindingConfig bc : bindingConfigs) {
						if (bc.feature == MaxCulFeature.THERMOSTAT) {
							String itemName = provider.getItemNameForConfig(bc);
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(setTempMsg.getDesiredTemperature()));
						}
					}
				}
				/* respond to device */
				if (isBroadcast == false)
					this.messageHandler.sendAck(setTempMsg);
				break;
			case THERMOSTAT_STATE:
				ThermostatStateMsg thermStateMsg = new ThermostatStateMsg(data);
				thermStateMsg.printMessage();
				for (MaxCulBindingProvider provider : super.providers) {
					Collection<MaxCulBindingConfig> bindingConfigs = provider
							.getConfigsForRadioAddr(thermStateMsg.srcAddrStr);
					for (MaxCulBindingConfig bc : bindingConfigs) {
						String itemName = provider.getItemNameForConfig(bc);
						if (bc.feature == MaxCulFeature.THERMOSTAT && thermStateMsg.getDesiredTemperature() != null) {
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(thermStateMsg.getDesiredTemperature()));
						} else if (bc.feature == MaxCulFeature.TEMPERATURE && thermStateMsg.getMeasuredTemperature() != null)
						{
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(thermStateMsg.getMeasuredTemperature()));
						} else if (bc.feature == MaxCulFeature.BATTERY) {
							eventPublisher.postUpdate(
									itemName,
									thermStateMsg.getBatteryLow()?OnOffType.ON:OnOffType.OFF);
						} else if (bc.feature == MaxCulFeature.MODE) {
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(thermStateMsg.getControlMode().toInt()));
						} else if (bc.feature == MaxCulFeature.VALVE_POS) {
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(thermStateMsg.getValvePos()));
						}
					}
				}
				/* respond to device */
				if (isBroadcast == false)
					this.messageHandler.sendAck(thermStateMsg);
				break;
			case WALL_THERMOSTAT_STATE:
				WallThermostatStateMsg wallThermStateMsg = new WallThermostatStateMsg(data);
				wallThermStateMsg.printMessage();
				for (MaxCulBindingProvider provider : super.providers) {
					Collection<MaxCulBindingConfig> bindingConfigs = provider
							.getConfigsForRadioAddr(wallThermStateMsg.srcAddrStr);
					for (MaxCulBindingConfig bc : bindingConfigs) {
						String itemName = provider.getItemNameForConfig(bc);
						if (bc.feature == MaxCulFeature.THERMOSTAT && wallThermStateMsg.getDesiredTemperature()!=null) {
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(wallThermStateMsg.getDesiredTemperature()));
						} else if (bc.feature == MaxCulFeature.TEMPERATURE && wallThermStateMsg.getMeasuredTemperature()!=null)
						{
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(wallThermStateMsg.getMeasuredTemperature()));
						} else if (bc.feature == MaxCulFeature.BATTERY) {
							eventPublisher.postUpdate(
									itemName,
									wallThermStateMsg.getBatteryLow()?OnOffType.ON:OnOffType.OFF);
						} else if (bc.feature == MaxCulFeature.MODE) {
							eventPublisher.postUpdate(
									itemName,
									new DecimalType(wallThermStateMsg.getControlMode().toInt()));
						}
					}
				}
				/* respond to device */
				if (isBroadcast == false)
					this.messageHandler.sendAck(wallThermStateMsg);
				break;
			case TIME_INFO:
				TimeInfoMsg timeMsg = new TimeInfoMsg(data);
				timeMsg.printMessage();
				TimeUpdateRequestSequence timeSeq = new TimeUpdateRequestSequence(this.tzStr, messageHandler);
				messageHandler.startSequence(timeSeq, timeMsg);
				break;
			default:
				logger.debug("Unhandled message type " + msgType.toString());
				break;

			}
		}
	}
}