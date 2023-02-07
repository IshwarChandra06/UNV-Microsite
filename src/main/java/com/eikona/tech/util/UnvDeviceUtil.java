package com.eikona.tech.util;

import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eikona.tech.constants.ApplicationConstants;
import com.eikona.tech.constants.NumberConstants;
import com.eikona.tech.constants.UnvDeviceConstants;
import com.eikona.tech.entity.Device;
import com.eikona.tech.repository.DeviceRepository;

@Component
@EnableScheduling
public class UnvDeviceUtil {
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private RequestExecutionUtil requestExecution;
	
//	@Scheduled(cron ="0 59 23 * * *")
	public void employeeTotalCountInDeviceSync() {
		List<Device> listDevice = deviceRepository.findAllByIsDeletedFalse();
		for(Device device:listDevice) {
			pullEmployeeFromDeviceToMata(device);
		}
	}

	public void pullEmployeeFromDeviceToMata(Device device) {
		try {

			String myurl = ApplicationConstants.HTTP_COLON_DOUBLE_SLASH + UnvDeviceConstants.PEOPLE_API_SEARCH_INFO.formatted(device.getIpAddress());

			int offset = NumberConstants.ZERO;
			Long totalEmployee = NumberConstants.LONG_ZERO;

				String myjson = UnvDeviceConstants.PEOPLE_INFO_JSON.formatted(offset);

				HttpPost request = new HttpPost(myurl);
				StringEntity entity = new StringEntity(myjson);
				request.setEntity(entity);
				String responeData = requestExecution.executeHttpPostRequest(request);

				JSONParser jsonParser = new JSONParser();
				JSONObject jsonResponse = (JSONObject) jsonParser.parse(responeData);
				JSONObject responseObj = (JSONObject) jsonResponse.get(UnvDeviceConstants.RESPONSE);

				JSONObject responseDataObj = (JSONObject) responseObj.get(UnvDeviceConstants.DATA);
				totalEmployee = (Long) responseDataObj.get(UnvDeviceConstants.TOTAL);
				device.setTotalPerson(totalEmployee);

			deviceRepository.save(device);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
