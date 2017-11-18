commit 4667279b578c592668cb79ba9e30cc35c11dd7cc
Author: Cedric Champeau <cedric@gradle.com>
Date:   Thu Nov 10 18:38:43 2016 +0100

    Add the concept of missing vs unknown attribute values

    When a consumer configuration declares attributes, we try to match the attributes against the attributes of configurations
    of the target project. However, there's a situation where attributes can be defined in the producer, but not in
    the consumer, leading to an ambiguous match. This commit improves the existing algorithm by making a different between
    _missing_ and _unknown_ attributes. The producer can then decide to select a configuration or not depending on the
    type of value. If it's missing, it means that the consumer _could_ have provided a value, but for some reason didn't.
    If it's unknown, the consumer didn't know about this attribute type, so could not provide a value.

    This commit adds a fluent API for attribute matching strategies to define the behavior of disambiguation.