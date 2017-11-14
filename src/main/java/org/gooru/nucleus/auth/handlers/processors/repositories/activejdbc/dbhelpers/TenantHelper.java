package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.dbhelpers;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityTenant;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 31-Oct-2017
 */
public final class TenantHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantHelper.class);

    private TenantHelper() {
        throw new AssertionError();
    }

    public static String getTenantRoot(String tenantId) {
        String tenantRoot = null;
        boolean isFound = false;
        final String origTenantId = tenantId;

        try {
            while (!isFound) {
                Object parentTenant = Base.firstCell(AJEntityTenant.SELECT_PARENT_TENANT, tenantId);
                if (parentTenant != null) {
                    tenantId = parentTenant.toString();
                    continue;
                }

                tenantRoot = tenantId;
                isFound = true;
            }
        } catch (Throwable t) {
            LOGGER.warn("unable to find tenant root of the tenant '{}'", tenantId);
        }

        return origTenantId.equalsIgnoreCase(tenantRoot) ? null : tenantRoot;
    }
}
