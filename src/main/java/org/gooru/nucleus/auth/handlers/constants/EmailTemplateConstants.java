package org.gooru.nucleus.auth.handlers.constants;

/**
 * @author szgooru Created On: 12-Jan-2017
 */
public final class EmailTemplateConstants {
  public static final String WELCOME_MAIL = "welcome_mail";
  public static final String PARENT_REGISTARTION_CONFIRMATION = "parent_registration_confirmation";
  public static final String INVITE_COLLABORATOR = "invite_collaborator";
  public static final String PASSWORD_CHANGED = "password_changed_confirmation";
  public static final String EMAIL_ADDRESS_CHANGE_REQUEST = "email_address_change_request";
  public static final String USER_REGISTARTION_CONFIRMATION = "user_registration_confirmation";
  public static final String CHILD_USER_REGISTARTION_CONFIRMATION =
      "child_user_registration_confirmation";
  public static final String USER_INVITE_CLASS = "user_invite_class";
  public static final String USER_INVITE_OPEN_CLASS = "user_invite_open_class";
  public static final String PASSWORD_CHANGE_REQUEST = "password_change_request";

  private EmailTemplateConstants() {
    throw new AssertionError();
  }
}
