package org.cibseven.bpm.extension.mail.oauth;

public class OAuthUtil {

  private OAuthUtil() {}

  public static String generateAccessToken(OAuthCredentials credentials) {
    try {
      OAuth2Details oauthDetails = new OAuth2Details();
      oauthDetails.setGrantType("client_credentials");
      oauthDetails.setClientId(credentials.getClientId());
      oauthDetails.setClientSecret(credentials.getClientSecret());
      oauthDetails.setAuthenticationServerUrl(credentials.getAuthenticationServerUrl());
      oauthDetails.setScope(credentials.getScope());

      return new OAuthTokenSupplier().generateAccessToken(oauthDetails);
    } catch (Exception e) {
      throw new IllegalStateException("Can not get access token", e);
    }
  }
}
