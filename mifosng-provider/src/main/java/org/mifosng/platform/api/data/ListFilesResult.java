package org.mifosng.platform.api.data;

public class ListFilesResult {

	private String[] files;

	public ListFilesResult(final String[] files) {
		this.files = files;
	}

	public String[] getFiles() {
		return files;
	}

	public void setFiles(String[] files) {
		this.files = files;
	}
}
