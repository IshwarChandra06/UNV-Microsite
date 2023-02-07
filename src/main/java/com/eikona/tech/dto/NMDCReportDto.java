package com.eikona.tech.dto;

import java.util.List;

public class NMDCReportDto<T> {

	private List<String> headList;
	private List<T> dataList;
	public List<String> getHeadList() {
		return headList;
	}
	public List<T> getDataList() {
		return dataList;
	}
	public void setHeadList(List<String> headList) {
		this.headList = headList;
	}
	public void setDataList(List<T> dataList) {
		this.dataList = dataList;
	}
}
