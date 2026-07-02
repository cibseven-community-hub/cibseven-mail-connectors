package org.cibseven.bpm.extension.mail.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthTokenSupplier {

  private static final Pattern ACCESS_TOKEN_PATTERN =
      Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");

  public String generateAccessToken(OAuth2Details oauthDetails) {
    try {
      String body = buildRequestBody(oauthDetails);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(oauthDetails.getAuthenticationServerUrl()))
              .header("Content-Type", "application/x-www-form-urlencoded")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();

      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new RuntimeException(
            "Token endpoint returned HTTP " + response.statusCode() + ": " + response.body());
      }

      return extractAccessToken(response.body());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Could not obtain access token", e);
    }
  }

  private String buildRequestBody(OAuth2Details oauthDetails) {
    StringBuilder body = new StringBuilder();
    body.append("grant_type=").append(encode(oauthDetails.getGrantType().toString()));
    body.append("&client_id=").append(encode(oauthDetails.getClientId()));
    body.append("&client_secret=").append(encode(oauthDetails.getClientSecret()));
    if (oauthDetails.getScope() != null && !oauthDetails.getScope().isEmpty()) {
      body.append("&scope=").append(encode(oauthDetails.getScope()));
    }
    if (oauthDetails.getUsername() != null && !oauthDetails.getUsername().isEmpty()) {
      body.append("&username=").append(encode(oauthDetails.getUsername()));
    }
    if (oauthDetails.getPassword() != null && !oauthDetails.getPassword().isEmpty()) {
      body.append("&password=").append(encode(oauthDetails.getPassword()));
    }
    return body.toString();
  }

  private String extractAccessToken(String json) {
    Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(json);
    if (matcher.find()) {
      return matcher.group(1);
    }
    throw new RuntimeException("Could not extract access_token from response: " + json);
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
