package com.conflux.mifosplatform.portfolio.loanaccount.service;


import javax.annotation.PostConstruct;

import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.mifosplatform.portfolio.common.service.BusinessEventListner;
import org.mifosplatform.portfolio.common.service.BusinessEventNotifierService;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.stereotype.Component;

import com.conflux.mifosplatform.portfolio.loanaccount.exception.LoanApplicationSubStatusException;

@Component
public class LoanApplicationCustomValidations {
	
	private final BusinessEventNotifierService businessEventNotifierService;
	
	@Autowired
    public LoanApplicationCustomValidations (
            final BusinessEventNotifierService businessEventNotifierService
            ) {
        this.businessEventNotifierService = businessEventNotifierService;
    }
	
	@PostConstruct
    public void addListners() {
        this.businessEventNotifierService.addBusinessEventPreListners(
        		BUSINESS_EVENTS.LOAN_SUBMITTED, new CheckForSubStatusActiveGoodStanding());		
    }
	
	private class CheckForSubStatusActiveGoodStanding implements BusinessEventListner {

		private String ACTIVE_GOOD_STANDING = "Active in Good Standing";
		
        @Override
        public void businessEventToBeExecuted(@SuppressWarnings("unused") AbstractPersistable<Long> businessEventEntity) {
        	if (businessEventEntity instanceof Loan) {
                Loan loan = (Loan) businessEventEntity;
                if ( (loan.client() != null) &&
                		( loan.client().subStatus() != null)	&&
                		( loan.client().subStatus().label() != null) )
                if ( ! (loan.client().subStatus().label().equals(ACTIVE_GOOD_STANDING)) ) {
                	throw new LoanApplicationSubStatusException (
                			"clientsubstatus",
                			"Invalid Client Sub-Status for Loan Application.",
                			(Object[]) null);
                }
            }
        }

        @Override
        public void businessEventWasExecuted(AbstractPersistable<Long> businessEventEntity) {
            
        }
    }

}
