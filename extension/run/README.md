# Cibseven-mail Plugin for CIB seven Run

This plugin wraps the cibseven-mail spring-boot-starter and is intended to be used with CIB seven Run.
The plugin configures the connectors for sending, polling, and deleting e-mails based on the YAML used for configuring CIB seven Run, and registers the connectors upon startup.

## Install

This plugin can be used with CIB seven Run.

1. Add the `cibseven-mail-extension-run-${VERSION}.jar`to the `configuration/userlib`folder.

2. Configure the plugin.

## How to Use it?

For instructions on how to use the connectors from a service task, see [the root project's readme](/README.md).

To use the notification service, please register Beans of type `Consumer<Mail>` or `MessageHandler` which will automatically be picked up and registered to the notification service.

## How to Configure it?

As this plugin relies on the cibseven-mail spring-boot-starter, head over to the [configuration section](./../spring-boot/README.md) there.
