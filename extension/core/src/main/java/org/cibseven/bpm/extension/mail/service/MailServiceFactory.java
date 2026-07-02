/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cibseven.bpm.extension.mail.service;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Properties;
import java.util.function.Function;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.cibseven.bpm.extension.mail.AbstractFactory;
import org.cibseven.bpm.extension.mail.config.JakartaMailProperties;
import org.cibseven.bpm.extension.mail.oauth.OAuthCredentials;
import org.cibseven.bpm.extension.mail.oauth.OAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailServiceFactory extends AbstractFactory<MailService> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceFactory.class);
  private static final MailServiceFactory INSTANCE = new MailServiceFactory();

  private MailServiceFactory() {}

  public static MailServiceFactory getInstance() {
    return INSTANCE;
  }

  @Override
  protected MailService createInstance() {
    return new JakartaMailService(getSession());
  }

  private Session getSession() {
    Session session;
    Properties jakartaMailProperties = JakartaMailProperties.get();
    if (isJndiSession(jakartaMailProperties)) {
      String jndiName = extractJndiName(jakartaMailProperties);
      try {
        LOGGER.debug("Lookup mail session with jndi-name: {}", jndiName);
        Context ictx = new InitialContext();
        session = (Session) ictx.lookup(jndiName);
      } catch (NamingException e) {
        String msg =
            String.format("Cannot connect to mail session under jndi-name '%s' :", jndiName);
        LOGGER.error(msg, e);
        throw new IllegalArgumentException(msg, e);
      }
    } else {
      OAuthCredentials oauthCredentials = readOAuthCredentials(jakartaMailProperties);
      if (oauthCredentials != null) {
        LOGGER.debug("OAuth2 credentials detected, enabling XOAUTH2 mechanism");
        jakartaMailProperties.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        jakartaMailProperties.put("mail.smtp.sasl.enable", "true");
        jakartaMailProperties.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
        jakartaMailProperties.put("mail.imaps.auth.mechanisms", "XOAUTH2");
        jakartaMailProperties.put("mail.imaps.sasl.enable", "true");
        jakartaMailProperties.put("mail.imaps.sasl.mechanisms", "XOAUTH2");
      }
      session =
          Session.getInstance(
              jakartaMailProperties,
              new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                  String user = jakartaMailProperties.getProperty("mail.user");
                  String credential =
                      oauthCredentials != null
                          ? OAuthUtil.generateAccessToken(oauthCredentials)
                          : jakartaMailProperties.getProperty("mail.password");
                  return new PasswordAuthentication(user, credential);
                }
              });
    }
    return session;
  }

  private OAuthCredentials readOAuthCredentials(Properties properties) {
    String clientId = properties.getProperty("mail.oauth.clientId");
    if (clientId == null || clientId.isBlank()) {
      return null;
    }
    return new OAuthCredentials(
        clientId,
        properties.getProperty("mail.oauth.clientSecret"),
        properties.getProperty("mail.oauth.authenticationServerUrl"),
        properties.getProperty("mail.oauth.scope"));
  }

  private boolean isJndiSession(Properties properties) {
    String jndiName = extractJndiName(properties);
    return jndiName != null && !jndiName.trim().isEmpty();
  }

  private String extractJndiName(Properties properties) {
    return properties.getProperty(JakartaMailProperties.PROP_NAME_MAIL_SESSION_JNDI_NAME);
  }

  public void setWith(Function<Session, MailService> setter) {
    MailService mailService = setter.apply(getSession());
    set(mailService);
  }
}
