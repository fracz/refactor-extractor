commit 18c471e7117472baa07d42011e0a11b48ff1b625
Author: Bernd Ahlers <bernd@torch.sh>
Date:   Fri Oct 10 18:16:40 2014 +0200

    Unbreak timestamp deserializing in the message cache and add tests.

    A missing manual conversion of the serialized timestamp (a long) to a DateTime
    object in the deserialize method caused the timestamp to be stored as a
    long in Elasticsearch.
    This wasn't catched by static typing because the Message object stores
    all fields in a "Map<String, Object>" and Jackson happily deserialized
    the field into a long.

    The current Message implementation also does not allow to set the "_id"
    field. That means there was no "_id" field in the deserialized Message.
    The deserializer now creates the Message object in a different way so
    that there is an "_id" field in the message.

    That means that the message has a new "_id" after
    serializing/deserializing! This cannot be fixed without refactoring the
    Message class.

    Fixes #728.