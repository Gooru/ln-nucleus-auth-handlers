package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;

public class AJEntityUserAccounts extends Model {

	public static final String EMAIL = "email";

	private static final Set<String> USERS_ACCOUNTS_PAYLOAD_FIELDS = new HashSet<>(
			Arrays.asList(ParameterConstants.PARAM_EMAIL, EMAIL));

	public static final List<String> USER_ACCOUNTS_FIELDS = Arrays.asList("name", "image_url", "login_type",
			"short_name");

	public static final String SELECT_ACCOUNTS_BY_EMAIL = "select tenant.name as tenant_name, tenant.image_url, users.login_type, tenant.short_name as tenant_short_name from users "
			+ "inner join tenant on tenant.id = users.tenant_id where email= ?";

	private static final Map<String, FieldValidator> validatorRegistry;

	static {
		validatorRegistry = initializeValidators();
	}

	private static class UserAccountsValidationRegistry implements ValidatorRegistry {
		@Override
		public FieldValidator lookupValidator(String fieldName) {
			return validatorRegistry.get(fieldName);
		}
	}

	public static ValidatorRegistry getValidatorRegistry() {
		return new UserAccountsValidationRegistry();
	}

	private static Map<String, FieldValidator> initializeValidators() {
		Map<String, FieldValidator> validatorMap = new HashMap<>();
		validatorMap.put(EMAIL, (FieldValidator::validateEmail));
		return validatorMap;
	}

	public static FieldSelector usersAccountsFieldSelector() {
		return () -> Collections.unmodifiableSet(USERS_ACCOUNTS_PAYLOAD_FIELDS);
	}

}
