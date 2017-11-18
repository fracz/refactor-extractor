commit 3022f5e34fed329998589b719f3fd545c271a9db
Author: Andy Wilkinson <awilkinson@gopivotal.com>
Date:   Fri Jun 14 12:34:12 2013 +0100

    Make Message type pluggable

    To improve compatibility between Spring's messaging classes and
    Spring Integration, the type of Message that is created has been made
    pluggable through the introduction of a factory abstraction;
    MessageFactory.

    By default a MessageFactory is provided that will create
    org.springframework.messaging.GenericMessage instances, however this
    can be replaced with an alternative implementation. For example,
    Spring Integration can provide an implementation that creates
    org.springframework.integration.message.GenericMessage instances.

    This control over the type of Message that's created allows messages
    to flow from Spring messaging code into Spring Integration code without
    any need for conversion. In further support of this goal,
    MessageChannel, MessageHandler, and SubscribableChannel have been
    genericized to make the Message type that they deal with more
    flexible.