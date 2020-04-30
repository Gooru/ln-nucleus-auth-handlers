package org.gooru.nucleus.auth.handlers.constants;

public final class MessageConstants {

  public static final String MSG_HEADER_OP = "mb.operation";
  public static final String MSG_HEADER_TOKEN = "access.token";
  public static final String MSG_HEADER_BASIC_AUTH = "basic.auth";
  public static final String MSG_HEADER_NONCE_TOKEN = "nonce.token";
  public static final String MSG_HEADER_REFRESH_TOKEN = "refresh.token";
  public static final String MSG_OP_AUTH_WITH_PREFS = "auth.with.prefs";
  public static final String MSG_OP_STATUS = "mb.operation.status";
  public static final String MSG_KEY_PREFS = "prefs";
  public static final String MSG_OP_STATUS_SUCCESS = "success";
  public static final String MSG_OP_STATUS_ERROR = "error";
  public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
  public static final String MSG_USER_ANONYMOUS = "anonymous";
  public static final String MSG_USER_ID = "userId";
  public static final String MSG_HTTP_STATUS = "http.status";
  public static final String MSG_HTTP_BODY = "http.body";
  public static final String MSG_HTTP_RESPONSE = "http.response";
  public static final String MSG_HTTP_ERROR = "http.error";
  public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
  public static final String MSG_HTTP_HEADERS = "http.headers";
  public static final String MSG_HTTP_PARAM = "http.params";
  public static final String MSG_USER_CONTEXT_HOLDER = "user.context.holder";
  public static final String MSG_HEADER_REQUEST_DOMAIN = "http.request.domain";
  public static final String MSG_HEADER_API_KEY = "http.api.key";
  public static final String MSG_HEADER_SEESION_TOKEN = "session.token";
  public static final String TOKEN = "Token";

  // Containers for different responses
  public static final String RESP_CONTAINER_MBUS = "mb.container";
  public static final String RESP_CONTAINER_EVENT = "mb.event";
  public static final String MSG_MESSAGE = "message";

  public static final String MSG_OP_ANONYMOUS_SIGNIN = "anonymous.signin";
  public static final String MSG_OP_USER_SIGNIN = "user.signin";
  public static final String MSG_OP_USER_SIGNOUT = "user.signout";
  public static final String MSG_OP_ACCESS_TOKEN_CHECK = "access.token.check";
  public static final String MSG_OP_ACCESS_TOKEN_DETAILS = "access.token.details";

  public static final String MSG_OP_DOMAIN_BASED_REDIRECT = "domain.based.redirect";
  public static final String MSG_OP_INIT_LOGIN = "init.login";
  public static final String MSG_OP_REFRESH_TOKEN_CREATE_ACCESS_TOKEN =  "refresh.token.create.access.token";

  // Authorize command
  public static final String MSG_OP_USER_AUTHORIZE = "user.authorize";

  // User Operations
  public static final String MSG_OP_USER_SIGNUP = "user.signup";
  public static final String MSG_OP_USER_UPDATE = "user.update";
  public static final String MSG_OP_USER_GET = "user.get";
  public static final String MSG_OP_USER_PASSWORD_RESET_TRIGGER = "user.password.reset.trigger";
  public static final String MSG_OP_USER_PASSWORD_RESET = "user.password.reset";
  public static final String MSG_OP_USER_PASSWORD_CHANGE = "user.password.change";
  public static final String MSG_OP_GET_USER_FIND = "get.user.find";
  public static final String MSG_OP_GET_USERS_FIND = "get.users.find";
  public static final String MSG_OP_GET_USER_ACCOUNTS = "get.user.accounts";

  
  public static final String MSG_OP_INTERNAL_AUTHENTICATE = "internal.authenticate";
  public static final String MSG_OP_INTERNAL_IMPERSONATE = "internal.impersonate";
  public static final String MSG_OP_INTERNAL_LTI_SSO = "internal.lti.sso";
  public static final String MSG_OP_INTERNAL_WSFED_SSO = "internal.lti.sso.wsfed";
  public static final String MSG_OP_INTERNAL_TENANT_REALM = "internal.tenant.realm";
  public static final String MSG_OP_INTERNAL_OAUTH2_SSO = "internal.sso.oauth2";

  
  public static final String MSG_OP_RESET_EMAIL_ADDRESS = "reset.email";
  public static final String MSG_OP_RESEND_CONFIRMATION_EMAIL = "resend.confirmation.email";
  public static final String MSG_OP_CONFIRMATION_EMAIL = "confirm.email";

  // Auth client command
  public static final String MSG_OP_CREATE_AUTH_CLIENT = "create.auth.client";

  private MessageConstants() {
    throw new AssertionError();
  }

}
