/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcul.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openhab.binding.maxcul.MaxCulBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for parsing the binding configuration
 * and registering the {@link MaxCulBindingProvider}.
 *
 * The following devices have the following valid types:
 * <li>RadiatorThermostat - thermostat,temperature,battery,valvepos</li>
 * <li>WallThermostat - thermostat,temperature,battery</li>
 *
 * Examples:
 * <li><code>{ maxcul="RadiatorThermostat:JEQ1234565" }</code> - will return/set the thermostat temperature of radiator thermostat with the serial number JEQ0304492</li>
 * <li><code>{ maxcul="RadiatorThermostat:JEQ1234565:battery" }</code> - will return the battery level of JEQ0304492</li>
 * <li><code>{ maxcul="WallThermostat:JEQ1234566:temperature" }</code> - will return the temperature of a wall mounted thermostat with serial number JEQ0304447</li>
 * <li><code>{ maxcul="WallThermostat:JEQ1234566:thermostat" }</code> - will set/return the desired temperature of a wall mounted thermostat with serial number JEQ0304447</li>
 * <li><code>{ maxcul="PushButton:JEQ1234567" }</code> - will default to 'switch' mode</li>
 * <li><code>{ maxcul="PairMode" }</code> - Switch only, enables pair mode for 60s. Will automatically switch off after this time.</li>
 * <li><code>{ maxcul="ListenMode" }</code> - Switch only, doesn't process messages - just listens to traffic, parses and outputs it.</li>
 * @author Paul Hampson (cyclingengineer)
 * @since 1.6.0
 */
public class MaxCulGenericBindingProvider extends AbstractGenericBindingProvider implements MaxCulBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "maxcul";
	}

	private static final Logger logger =
			LoggerFactory.getLogger(MaxCulGenericBindingProvider.class);

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		MaxCulBindingConfig config = new MaxCulBindingConfig(bindingConfig);

		switch (config.getDeviceType())
		{
		case PAIR_MODE:
		case LISTEN_MODE:
			if (!(item instanceof SwitchItem))
				throw new BindingConfigParseException("Invalid item type. PairMode/ListenMode can only be a switch");
			break;
		case PUSH_BUTTON:
		case SHUTTER_CONTACT:
			if (config.getFeature() == MaxCulFeature.BATTERY && !(item instanceof SwitchItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'battery' can only be a Switch");
			if (config.getFeature() == MaxCulFeature.SWITCH && !(item instanceof SwitchItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'switch' can only be a Switch");
			break;
		case RADIATOR_THERMOSTAT:
		case RADIATOR_THERMOSTAT_PLUS:
		case WALL_THERMOSTAT:
			if (config.getFeature() == MaxCulFeature.TEMPERATURE && !(item instanceof NumberItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'temperature' can only be a Number");
			else if (config.getFeature() == MaxCulFeature.VALVE_POS && !(item instanceof NumberItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'valvepos' can only be a Number");
			else if (config.getFeature() == MaxCulFeature.THERMOSTAT && !((item instanceof NumberItem) || (item instanceof SwitchItem)))
				throw new BindingConfigParseException("Invalid item type. Feature 'thermostat' can only be a Number or a Switch");
			else if (config.getFeature() == MaxCulFeature.BATTERY && !(item instanceof SwitchItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'battery' can only be a Switch");
			else if (config.getFeature() == MaxCulFeature.MODE && !(item instanceof NumberItem))
				throw new BindingConfigParseException("Invalid item type. Feature 'mode' can only be a Number");
			break;
		default:
			throw new BindingConfigParseException("Invalid config device type. Wasn't expecting "+config.getDeviceType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		logger.debug("Processing item "+item.getName());
		MaxCulBindingConfig config = new MaxCulBindingConfig(bindingConfig);

		addBindingConfig(item, config);

		// TODO detect updated binding configuration for temperatures and send CONFIG_TEMPERATURES message?
	}

	@Override
	public MaxCulBindingConfig getConfigForItemName(String itemName) {
		MaxCulBindingConfig config = null;
		if (super.bindingConfigs.containsKey(itemName)) {
			config = (MaxCulBindingConfig) super.bindingConfigs.get(itemName);
		}
		return config;
	}

	public String getItemNameForConfig(MaxCulBindingConfig bc)
	{
		String itemName = null;
		if (super.bindingConfigs.containsValue(bc))
		{
			for (Entry<String,BindingConfig> entry : super.bindingConfigs.entrySet())
			{
				if (entry.getValue().equals(bc))
				{
					itemName = entry.getKey();
					break;
				}
			}
		}
		return itemName;
	}

	@Override
	public MaxCulBindingConfig getConfigForSerialNumber(String serial) {
		MaxCulBindingConfig config = null;
		for (BindingConfig c : super.bindingConfigs.values() )
		{
			config = (MaxCulBindingConfig)c;
			if (config.getSerialNumber() == serial)
				return config;
		}
		return null;
	}

	@Override
	public List<MaxCulBindingConfig> getConfigsForSerialNumber(String serial) {
		List<MaxCulBindingConfig> configs = new ArrayList<MaxCulBindingConfig>();
		for (BindingConfig c : super.bindingConfigs.values() )
		{
			MaxCulBindingConfig config = (MaxCulBindingConfig)c;
			if (config.getSerialNumber() != null) /* could be PairMode/ListenMode device which has no serial */
			{
				logger.debug("Comparing '"+config.getSerialNumber()+"' with '"+serial+"'");
				if (config.getSerialNumber().compareToIgnoreCase(serial) == 0)
					configs.add(config);
			}
		}
		if (configs.isEmpty())
			return null;
		else
			return configs;
	}

	@Override
	public List<MaxCulBindingConfig> getConfigsForRadioAddr(String addr) {
		List<MaxCulBindingConfig> configs = new ArrayList<MaxCulBindingConfig>();
		for (BindingConfig c : super.bindingConfigs.values() )
		{
			MaxCulBindingConfig config = (MaxCulBindingConfig)c;
			if (config.getSerialNumber() != null) /* could be PairMode/ListenMode device which has no serial */
			{
				logger.debug("Comparing '"+config.getDevAddr()+"' with '"+addr+"'");
				if (config.getDevAddr().equalsIgnoreCase(addr))
					configs.add(config);
			}
		}
		return configs;
	}

	@Override
	public void buildAssociationMap() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<MaxCulBindingConfig> getAssociations(String deviceSerial) {
		// TODO Auto-generated method stub
		return null;
	}
}