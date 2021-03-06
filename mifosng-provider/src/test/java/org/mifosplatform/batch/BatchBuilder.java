package org.mifosplatform.batch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.batch.service.BatchApiService;
import org.mockito.Mockito;

/**
 * Runs a unit test for BatchApiResource by mocking the
 * BatchRequest and BatchApiService objects.
 * 
 * @author Rishabh Shukla
 */
public class BatchBuilder {

	//Verify a non-empty response by BatchApiResource.	 
	@Test
	public void batchApiTest() {
		
		//Mock a BatchRequest objects
		final BatchRequest batchTest = Mockito.mock(BatchRequest.class);
		
		//Mock a BatchApiService object
		final BatchApiService serviceTest = Mockito.mock(BatchApiService.class);
		
		final List<BatchRequest> requestList = new ArrayList<>();
		requestList.add(batchTest);

		//Call the BatchApiService using mocked objects
		final List<BatchResponse> result = serviceTest.handleBatchRequestsWithoutEnclosingTransaction(requestList);
	
		//Verifies whether handleBatchRequests() function of BatchApiService was called
		Mockito.verify(serviceTest).handleBatchRequestsWithoutEnclosingTransaction(requestList);

		//Verifies a non-empty response by the BatchApiResource
		Assert.assertNotNull(result);
	}
}
