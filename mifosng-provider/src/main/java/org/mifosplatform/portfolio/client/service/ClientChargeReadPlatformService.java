/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.client.data.ClientChargeData;

public interface ClientChargeReadPlatformService {

    Page<ClientChargeData> retrieveClientCharges(Long clientId, String status, Boolean pendingPayment, SearchParameters parameters);

    ClientChargeData retrieveClientCharge(Long clientId, Long clientChargeId);
    
    Collection<CalendarData> retrieveCalendars(Long groupId,Long clientId);

}
