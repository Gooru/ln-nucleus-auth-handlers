package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;


import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("tenant_setting")
public class AJEntityTenantSetting extends Model {
    public static final String VALUE = "value";
    public static final String DEFAULT_FW_ID = "default_fw_id";
    public static final String SELECT_TENANT_SETTING_TX_FW_PREFS = "id = ?::uuid and key = 'tx_fw_prefs'";
}