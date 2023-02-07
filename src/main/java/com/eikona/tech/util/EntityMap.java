package com.eikona.tech.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eikona.tech.entity.Device;
import com.eikona.tech.repository.DeviceRepository;

@Component
public class EntityMap {

	@Autowired
	private DeviceRepository deviceRepository;
	
	public Map<String, Device> getAllDevice() {
		List<Device> devieList = deviceRepository.findAllByIsDeletedFalse();
		Map<String, Device> deviceMap = new HashMap<String, Device>();

		for (Device device : devieList) {
			deviceMap.put(device.getName(), device);
		}
		return deviceMap;
	}

}
