package org.gooru.nucleus.auth.handlers.constants;

/**
 * @author szgooru Created On: 02-Jan-2017
 */
public final class HelperConstants {

  private HelperConstants() {
    throw new AssertionError();
  }

  public static final String RESOURCE_BUNDLE = "message";
  public static final int RESET_PASS_TOKEN_EXPIRY = 86400;
  public static final String CHAR_ENCODING_UTF8 = "UTF-8";
  public static final String HEADER_TOKEN = "Token ";

  public enum UserCategories {
    teacher("teacher"), student("student"), parent("parent"), other("other"), school_admin(
        "school_admin"), school_district_admin("school_district_admin");

    public final String userCategory;

    UserCategories(String userCategory) {
      this.userCategory = userCategory;
    }

    public String getUserCategory() {
      return this.userCategory;
    }

  }

  public enum UserGender {
    male("male"), female("female"), other("other"), not_wise_to_share("not_wise_to_share");

    public final String gender;

    UserGender(String gender) {
      this.gender = gender;
    }

    public String getGender() {
      return this.gender;
    }
  }

  public enum UserLoginType {
    google("google"), wsfed("wsfed"), saml("saml"), credential("credential"), ltisso("ltisso");

    public final String type;

    UserLoginType(String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }
  }

  public enum GrantTypes {
    anonymous("anonymous"), credential("credential"), google("google"), wsfed("wsfed"), saml(
        "saml"), ltisso("ltisso");

    public final String type;

    GrantTypes(String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }
  }
}
