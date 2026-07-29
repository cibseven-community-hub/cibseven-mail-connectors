# Cibseven-mail Plugin for CIB seven Run

This plugin wraps the cibseven-mail spring-boot-starter and is intended to be used with CIB seven Run.
The plugin configures the connectors for sending, polling, and deleting e-mails based on the YAML used for configuring CIB seven Run, and registers the connectors upon startup.

## Install

This plugin can be used with CIB seven Run.

1. Add the `cibseven-mail-extension-run-${VERSION}.jar` to the `configuration/userlib` folder.

    _There is no need to add  `cibseven-connect-core-${VERSION}.jar` or `cibseven-engine-plugin-connect-${VERSION}.jar`, since both are already embedded in `internal/cibseven-bpm-run-core.jar`._

2. Configure the plugin.
    ```yaml
    camunda.bpm.run:
        process-engine-plugins:
        - plugin-class: org.cibseven.connect.plugin.impl.ConnectProcessEnginePlugin

    ```

3. If using FreeMarker, add `cibseven-template-engines-freemarker-${VERSION}.jar` and the FreeMarker jar to the `configuration/userlib` folder.

## How to Use it?

For instructions on how to use the connectors from a service task, see [the root project's readme](/README.md).

To use the notification service, please register Beans of type `Consumer<Mail>` or `MessageHandler` which will automatically be picked up and registered to the notification service.

## How to Configure it?

As this plugin relies on the cibseven-mail spring-boot-starter, head over to the [configuration section](./../spring-boot/README.md) there.
