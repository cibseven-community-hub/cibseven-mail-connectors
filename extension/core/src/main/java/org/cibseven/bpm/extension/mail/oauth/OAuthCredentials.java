package org.cibseven.bpm.extension.mail.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthCredentials {

  private String clientId;
  private String clientSecret;
  private String authenticationServerUrl;
  private String scope;
}
