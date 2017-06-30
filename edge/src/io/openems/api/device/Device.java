/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.api.device;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.api.bridge.Bridge;
import io.openems.api.device.nature.DeviceNature;
import io.openems.api.exception.OpenemsException;
import io.openems.api.thing.Thing;

public abstract class Device implements Thing {
	public final static String THINGID_PREFIX = "_device";
	private static int instanceCounter = 0;
	protected final Logger log;
	private Bridge bridge = null;
	private final String thingId;

	public Device() throws OpenemsException {
		this.thingId = THINGID_PREFIX + instanceCounter++;
		log = LoggerFactory.getLogger(this.getClass());
	}

	public Bridge getBridge() {
		return bridge;
	}

	@Override
	public String id() {
		return this.thingId;
	}

	protected abstract Set<DeviceNature> getDeviceNatures();
}