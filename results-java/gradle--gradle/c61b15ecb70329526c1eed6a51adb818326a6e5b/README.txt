commit c61b15ecb70329526c1eed6a51adb818326a6e5b
Author: Cedric Champeau <cedric@gradle.com>
Date:   Fri Nov 4 17:28:04 2016 +0100

    First refactor towards typed configuration attributes

    This commit is the first step towards the introduction of strongly typed configuration attributes.
    So far, the attributes found on a configuration were `String`s. While suitable for simple attribute
    matching, we found limitations as soon as we want to implement more complex matching strategies.

    As a consequence, this refactors the configuration attributes by changing the backend storage. Instead
    of using a `Map<String, String>` for attributes, which had the advantage of being simple but losely
    typed, the new storage type is `ConfigurationAttributes`, which is a `Map`-like storage but
    providing a strongly typed API to get and set attributes.

    A lot of the complexity of this code comes from the fact that Java doesn't support types like:

        Map<Attribute<T>, T>

    (where you want to tell that the value returned from the type must correspond to the inferred type of
    `T` for a given attribute value: in other words, a polymorphic map).

    An attribute is now represented through an entry consisting of a key and a value. The value type
    depends on the key type. For a: `Key<String>`, we expect a value of type `String`. For a key
    of type `Key<BuildType>`, we expect a value of type `BuildType`. Since not all type safety can
    be guaranteed at compile time (not for puts, gets are ok), we enforce this at runtime.

    Attributes are added thanks to the `attribute(Key<T>, T)` method. Since we also want to be able
    to support multiple attributes having the same type (say, `String`), a key is a pair `(Class<T>, String)`
    where the `Class<T>` represents the type of the value, and the `String` is the name of the attribute.

    Typically, for a `BuildType`, we would find a key as this:

        BUILD_TYPE = KeyOf(BuildType.class, "buildType")

    Then to fetch the build type from the attributes, we would call `getAttribute` with the appropriate key:

        BuildType buildType = attributes.get(BUILD_TYPE)

    It is therefore possible to have multiple attributes with the same storage type but different names:

        FLAVOR = KeyOf(String.class, "flavor")
        ARCH = KeyOf(String.class, "arch")
        ...
        String flavor = attributes.get(FLAVOR)
        String arch = attributes.get(ARCH)

    It's worth noting that even if the `ConfigurationAttributes` type lives in `org.gradle.api.artifacts` it
    can safely be used in configuration metadata, used during dependency resolution, because it is a storage
    of types which are not directly related to `Configuration`.

    This commit also provides convenience methods in case the attributes values are `String`: instead of using
    the cumbersome `Key` wrapper, we can use `String` directly:

        configuration.attribute('flavor', 'free')

    is equivalent to:

        configuration.attribute(KeyOf(String.class, 'flavor'), 'free')

    This commit doesn't have any test specific to any other type than `String` now, because it just makes sure
    that the existing code works with the new storage.