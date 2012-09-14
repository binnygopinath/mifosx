package org.mifosng.platform.api.data;

/**
 * 
 */
public class FileUploadResult {
	
	private boolean success;
	private String fileName;
	private long bankId;
	private long loanId;

	public FileUploadResult(final boolean success, final String fileName, final long bankId, final long loanId) {
		this.success = success;
		this.fileName = fileName;
		this.bankId = bankId;
		this.loanId = loanId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getBankId() {
		return bankId;
	}

	public void setBankId(long bankId) {
		this.bankId = bankId;
	}

	public long getLoanId() {
		return loanId;
	}

	public void setLoanId(long loanId) {
		this.loanId = loanId;
	}
}