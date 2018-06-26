package com.example.pc2.rfidreader.Model;

import java.io.Serializable;

public class TagInfo implements Serializable {

	private String tagUid;

	private String status;

	private boolean isUpload;


	public boolean isUpload() {
		return isUpload;
	}

	public void setUpload(boolean isUpload) {
		this.isUpload = isUpload;
	}

	public String getTagUid() {
		return tagUid;
	}

	public void setTagUid(String tagUid) {
		this.tagUid = tagUid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
