package org.cibseven.bpm.extension.mail.oauth;

import lombok.Data;

@Data
public class OAuth2Details {

  private String scope;
  private String grantType;
  private String clientId;
  private String clientSecret;
  private String accessToken;
  private String refreshToken;
  private String username;
  private String password;
  private String authenticationServerUrl;
  private String resourceServerUrl;
  private boolean isAccessTokenRequest;
}
