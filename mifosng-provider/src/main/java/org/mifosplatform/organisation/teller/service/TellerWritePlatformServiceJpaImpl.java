package org.mifosplatform.organisation.teller.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.teller.domain.Cashier;
import org.mifosplatform.organisation.teller.domain.CashierRepository;
import org.mifosplatform.organisation.teller.domain.CashierTransaction;
import org.mifosplatform.organisation.teller.domain.CashierTransactionRepository;
import org.mifosplatform.organisation.teller.domain.CashierTxnType;
import org.mifosplatform.organisation.teller.domain.Teller;
import org.mifosplatform.organisation.teller.domain.TellerRepository;
import org.mifosplatform.organisation.teller.exception.CashierNotFoundException;
import org.mifosplatform.organisation.teller.exception.TellerNotFoundException;
import org.mifosplatform.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TellerWritePlatformServiceJpaImpl implements TellerWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(TellerWritePlatformServiceJpaImpl.class);

    private final PlatformSecurityContext context;
    private final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final TellerRepository tellerRepository;
    private final OfficeRepository officeRepository;
    private final StaffRepository staffRepository;
    private final CashierRepository cashierRepository;
    private final CashierTransactionRepository cashierTxnRepository;
    
    @Autowired
    public TellerWritePlatformServiceJpaImpl (final PlatformSecurityContext context,
            final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final TellerRepository tellerRepository, final OfficeRepository officeRepository,
            final StaffRepository staffRepository, CashierRepository cashierRepository,
            CashierTransactionRepository cashierTxnRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.tellerRepository = tellerRepository;
        this.officeRepository = officeRepository;
        this.staffRepository = staffRepository;
        this.cashierRepository = cashierRepository;
        this.cashierTxnRepository = cashierTxnRepository;
    }


	@Override
	@Transactional
	public CommandProcessingResult createTeller(JsonCommand command) {
       try {
            final AppUser currentUser = this.context.authenticatedUser();
            
            final Long officeId = command.longValueOfParameterNamed("officeId");

            this.fromApiJsonDeserializer.validateForCreate(command.json());
            
            // final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);
            final Office tellerOffice = this.officeRepository.findOne(officeId);
            if (tellerOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Teller teller = Teller.fromJson(tellerOffice, command);

            // pre save to generate id for use in office hierarchy
            this.tellerRepository.save(teller);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.getOffice().getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
	}

	@Override
	@Transactional
	public CommandProcessingResult modifyTeller(Long tellerId,
			JsonCommand command) {
        try {
        	
        	final Long officeId = command.longValueOfParameterNamed("officeId");
            final Office tellerOffice = this.officeRepository.findOne(officeId);
            if (tellerOffice == null) { throw new OfficeNotFoundException(officeId); }

            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Teller teller = validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);

            final Map<String, Object> changes = teller.update(tellerOffice, command);

            if (!changes.isEmpty()) {
                this.tellerRepository.saveAndFlush(teller);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.officeId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
	}
			
	/*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    private Teller validateUserPriviledgeOnTellerAndRetrieve(final AppUser currentUser, final Long tellerId) {

        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepository.findOne(userOfficeId);
        if (userOffice == null) { throw new OfficeNotFoundException(userOfficeId); }

        final Teller tellerToReturn = this.tellerRepository.findOne(tellerId);
        if (tellerToReturn != null) {
            final Long tellerOfficeId = tellerToReturn.officeId();
            if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(tellerOfficeId)) { throw new NoAuthorizationException(
                    "User does not have sufficient priviledges to act on the provided office."); }
        } else {
            throw new TellerNotFoundException(tellerId);
        }

        return tellerToReturn;
    }
    
	@Override
	@Transactional
	public CommandProcessingResult deleteTeller(Long tellerId) {
		// TODO Auto-generated method stub
		return null;
	}
	
    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleTellerDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("m_tellers_name_unq")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.teller.duplicate.name", "Teller with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.teller.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }


	@Override
	public CommandProcessingResult allocateCashierToTeller(final Long tellerId, JsonCommand command) {
		try {
            final AppUser currentUser = this.context.authenticatedUser();
            
            // final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);
            final Teller teller = this.tellerRepository.findOne(tellerId);
            if (teller == null) { throw new TellerNotFoundException(tellerId); }
            final Office tellerOffice = teller.getOffice();
            
            final Long staffId = command.longValueOfParameterNamed("staffId");

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());
                        
            
            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }

            final Cashier cashier = Cashier.fromJson(tellerOffice, teller, staff,
            		command);

            // pre save to generate id for use in office hierarchy
            this.cashierRepository.save(cashier);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
	}

	@Override
	@Transactional
	public CommandProcessingResult updateCashierAllocation (Long tellerId, Long cashierId,
			JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());
            
            final Long staffId = command.longValueOfParameterNamed("staffId");
            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }

            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);
            
            cashier.setStaff(staff);
            
            //TODO - check if staff office and teller office match

            final Map<String, Object> changes = cashier.update(command);

            if (!changes.isEmpty()) {
                this.cashierRepository.saveAndFlush(cashier);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getTeller().getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
	}

    private Cashier validateUserPriviledgeOnCashierAndRetrieve(final AppUser currentUser, 
    		final Long tellerId, final Long cashierId) {
    	
    	validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);
        
        final Cashier cashierToReturn = this.cashierRepository.findOne(cashierId);

        return cashierToReturn;
    }
    
    @Override
	@Transactional
	public CommandProcessingResult deleteCashierAllocation (Long tellerId, Long cashierId,
			JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);
            this.cashierRepository.delete(cashier);

        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

	    return new CommandProcessingResultBuilder() //
	            .withEntityId(cashierId) //
	            .build();
	}
    
    @Override
	public CommandProcessingResult allocateCashToCashier(final Long cashierId, JsonCommand command) {
    	return doTransactionForCashier(cashierId, CashierTxnType.ALLOCATE, command); // For fund allocation to cashier
    }
    
    @Override
	public CommandProcessingResult settleCashFromCashier(final Long cashierId, JsonCommand command) {
    	return doTransactionForCashier(cashierId, CashierTxnType.SETTLE, command); // For fund settlement from cashier
    }

	private CommandProcessingResult doTransactionForCashier(final Long cashierId, final CashierTxnType txnType, JsonCommand command) {
		try {
            final AppUser currentUser = this.context.authenticatedUser();
            
            final Cashier cashier = this.cashierRepository.findOne(cashierId);
            if (cashier == null) { throw new CashierNotFoundException(cashierId); }

            this.fromApiJsonDeserializer.validateForCashTxnForCashier(command.json());
                        
            final String entityType = command.stringValueOfParameterNamed("entityType");
            final Long entityId = command.longValueOfParameterNamed("entityId");
            if (entityType.equals("loan account")) {
            	// TODO : Check if loan account exists
            	// LoanAccount loan = null;
            	// if (loan == null) { throw new LoanAccountFoundException(entityId); }
            } else if (entityType.equals("savings account")) {
            	// TODO : Check if loan account exists
            	// SavingsAccount savingsaccount = null;
            	// if (savingsaccount == null) { throw new SavingsAccountNotFoundException(entityId); }
            	
            } if (entityType.equals("client")) {
            	// TODO: Check if client exists
            	// Client client = null;
            	// if (client == null) { throw new ClientNotFoundException(entityId); }
            } else {
            	// TODO : Invalid type handling
            }
            
            final CashierTransaction cashierTxn = CashierTransaction.fromJson(cashier, command);
            cashierTxn.setTxnType(txnType.getId());

            // pre save to generate id for use in office hierarchy
            this.cashierTxnRepository.save(cashierTxn);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getId()) //
                    .withSubEntityId(cashierTxn.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
	}


}
