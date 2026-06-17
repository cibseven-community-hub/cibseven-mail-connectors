# cibseven-mail


[![CIB seven 2.2.0](https://img.shields.io/badge/CIB%20seven-2.2.0-orange.svg)](https://docs.cibseven.org/manual/2.2/)
[![Maven Central](https://img.shields.io/maven-central/v/org.cibseven.community.data/cibseven-mail?label=Maven%20Central)](https://central.sonatype.com/artifact/org.cibseven.community.data/cibseven-mail)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A community extension for CIB seven to integrate emails in a process and interact with them.

![Sample process](docs/sample-process.png)

## Features

* send mail
* poll mails
* delete mails
* react on incoming mails

## Install

> Requirements:
* CIB seven >= 2.0.0
* Java 17

### For Spring Boot

Read [these instructions](./extension/spring-boot).

### For Embedded Process Engine

Add `cibseven-mail-core` as dependency to your application. Using Maven, you have to add the following lines to your POM:

```xml
<dependency>
  <groupId>org.cibseven.bpm.extension</groupId>
  <artifactId>cibseven-mail-core</artifactId>
  <version>2.2.0</version>
</dependency>
```

### For Shared Process Engine

Add `cibseven-mail-core-${VERSION}.jar` to your application server (e.g. `apache-tomcat-10.1.55\lib`).

Also make sure that you included the following dependencies:

* [cibseven-connect-core](http://mvnrepository.com/artifact/org.cibseven.connect/cibseven-connect-core) >= 2.0.0
* [JakartaMail](https://mvnrepository.com/artifact/jakarta.mail/jakarta.mail-api) >= 2.1.2
* [Eclipse Angus Mail](https://mvnrepository.com/artifact/org.eclipse.angus/angus-mail) >= 2.0.2
* [slf4j-api](http://mvnrepository.com/artifact/org.slf4j/slf4j-api) >= 2.0.11

If you use Wildfly, follow the [special instructions](docs/shared-process-engine-wildfly.md).

## How to use it?

The extension is build on top of the [Connectors API](https://docs.cibseven.org/manual/latest/reference/connect/) and provide some connectors for interacting with emails. The connectors can be used inside a process as implementation of a service task and are referenced by id. Use the Camunda Modeler to configure it.

```xml
<serviceTask id="sendMail" name="Send Mail Task">
  <extensionElements>
    <camunda:connector>
      <camunda:connectorId>mail-send</camunda:connectorId>
      <!-- input / output mapping -->
    </camunda:connector>
  </extensionElements>
</serviceTask>
```

See the [connectors user guide](https://docs.cibseven.org/manual/latest/user-guide/process-engine/connectors/) how to configure the process engine to use connectors.

### Send Mails

![icon](docs/mail-send-icon.png)

Connector-Id: mail-send

| Input parameter | Type                                   | Required?             |
|-----------------|----------------------------------------|-----------------------|
| from            | String                                 | no (read from config) |
| fromAlias       | String                                 | no (read from config) |
| to              | String                                 | yes                   |
| cc              | String                                 | no                    |
| bcc             | String                                 | no                    |
| subject         | String                                 | yes                   |
| text            | String                                 | no                    |
| html            | String                                 | no                    |
| fileNames       | List of String (path to files)         | no                    |
| files           | Map of String to file process variable | no                    |

The text or html body can also be generated from a template (e.g. using FreeMarker). See the [example](examples/pizza#send-a-mail).

### Poll Mails

![icon](docs/mail-poll-icon.png)

Connector-Id: mail-poll

| Input parameter      | Type                  | Required?             |
|----------------------|-----------------------|-----------------------|
| folder               | String (e.g. 'INBOX') | no (read from config) |
| download-attachments | Boolean               | no (read from config) |

| Output parameter | Type                                                                                      |
|------------------|-------------------------------------------------------------------------------------------|
| mails            | List of [Mail](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java) |

If `download-attachments` is set to `true` then it stores the attachments of the mails in the folder which is provided by the configuration. The path of the stored attachments can be gotten from the [Attachment](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Attachment.java)s of the [Mail](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java).

By default, the polled mails are marked as read. If the property `mail.imaps.peek` is set to `true` then the mails are just polled and not marked as read.

### Delete Mails

![icon](docs/mail-delete-icon.png)

Connector-Id: mail-delete

| Input parameter | Type                  | Required?             |
|-----------------|-----------------------|-----------------------|
| folder          | String (e.g. 'INBOX') | no (read from config) |
| mails           | List of Mail          | no<sup>1</sup>        |
| messageIds      | List of String        | no<sup>1</sup>        |
| messageNumbers  | List of Integer       | no<sup>1</sup>        |

<sup>1</sup> Either `mails`, `messageIds` or `messageNumbers` have to be set.

### React on incoming Mails

![icon](docs/mail-notification-icon.png)

The extension provide the [MailNotificationService](extension/core/src/main/java/org/camunda/bpm/extension/mail/notification/MailNotificationService.java) to react on incoming mails (e.g. start a process instance or correlate a message). You can register handlers / consumers which are invoked when a new mail is received.

```java
MailNotificationService notificationService = new MailNotificationService(configuration);

notificationService.registerMailHandler(mail -> {
  runtimeService.startProcessInstanceByKey("process",
    Variables.createVariables().putValue("mail", mail));
});

notificationService.start();

// ...

notificationService.stop();

```

If you use a mail handler and enabled `downloadAttachments` in the configuration then it stores the attachments of the mail before invoking the handler. Otherwise, you can also trigger the download manual by calling [Mail.downloadAttachments()](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java).

## How to configure it?

By default, the extension loads the configuration from a properties file `mail-config.properties` on classpath. You can change the lookup path using the environment variable `MAIL_CONFIG`. If you want to look up a file on the classpath, use the `classpath:` prefix (e.g. `classpath:/my-application.config`).

An example configuration can look like:

```
# send mails via SMTP
mail.transport.protocol=smtp

mail.smtp.host=smtp.gmail.com
mail.smtp.port=465
mail.smtp.auth=true
mail.smtp.ssl.enable=true
mail.smtp.socketFactory.port=465
mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory

# poll mails via IMAPS
mail.store.protocol=imaps

mail.imaps.host=imap.gmail.com
mail.imaps.port=993
mail.imaps.timeout=10000

# if peek = false then the polled mails are marked as read
mail.imaps.peek=false

# additional config
mail.poll.folder=INBOX
mail.sender=USER@google.com
mail.sender.alias=User Inc

mail.attachment.download=true
mail.attachment.path=attachments

# credentials
mail.user=USER@gmail.com
mail.password=PASSWORD
```

You can find some sample configurations at [extension/core/configs](extension/core/configs). If you use a mail provider which has no configuration yet, feel free to add one. You can verify your configuration with the [integration tests](extension/core/src/test/java/org/camunda/bpm/extension/mail/integration/MailProviderIntegrationTest.java).

### Alternative Configuration

if you are running CIB seven in the environment that supports Mail Service and [Java Naming and Directory Interface (JNDI)](https://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface), you can configure mail session in the container and make it available through jndi. Provide the jndi-name of your bound mail session within properties file `mail-config.properties` like this:

```
mail.session.jndi.name=java:jboss/mail/MyMailSessionName
```

Please refer you container documentation to configure mail service.

If you do not need other properties then session configuration, you can even skip property file `mail-config.properties` and specify your mail session jndi-name directly via `MAIL_CONFIG` environment variable like this:`jndi:java:jboss/mail/MyMailSessionName`. _Ensure it starts with `jndi:`_

## Examples

The following examples shows how to use the connectors and services.

* [Pizza Order](examples/pizza)
  * poll mails
  * send mail with generated text body
  * delete mail
* [Print Service](examples/print-service)
  * using the MailNotificationService
  * send mail with attachment

## Setting up configuration using HELM

The mail connector cannot directly support Helm values files since it cannot assume the deployment environment is Kubernetes.

This is why it is configured via the MAIL_CONFIG environment variable and a properties file.

However, supporting Helm deployment is easily done by following:

1) Accept the mail configuration in your Values.yaml, like this:

    ~~~
    ...
    mail:
        smtp:
            auth: true
            port: 465
    ...
    ~~~

2) Render the mail.properties file with a Helm template

    ~~~
    mail.smtp.auth={{ .Values.mail.smtp.auth }}
    mail.smtp.port={{ .Values.mail.smtp.port }}
    ~~~

3) Put the mail.properties file in a ConfigMap and mount it in your deployment on /config/mail.properties (or anywhere else you prefer)

4) Set the MAIL_CONFIG environment variable to "file:/config/mail.properties" in your deployment

## Next Steps

Depends on the input of the community. Some ideas:

* provide element templates for camunda modeler (not supported yet)
* integration of file process variables
* spring-based configuration

## Contribution

Found a bug? Please report it using [GitHub Issues](https://github.com/cibseven-community-hub/cibseven-mail-connectors/issues).

Want to extend, improve or fix a bug in the extension? [Pull Requests](https://github.com/cibseven-community-hub/cibseven-mail-connectors/pulls) are very welcome.


## FAQ

See also

* [JavaMail Project Documentation/FAQ](https://java.net/projects/javamail/pages/Home)
* [Oracle JavaMail FAQ](http://www.oracle.com/technetwork/java/faq-135477.html)

### Can't send / receive mails from Gmail

It can be that Google blocks the requests because it estimates your application as unsafe. You may also receive an email from Google. To fix this go to https://www.google.com/settings/security/lesssecureapps and enable less secure apps.

## License

[Apache License, Version 2.0](./LICENSE)
