commit 2656dddf65dfdf5a4feac8efb370ccfecc64ebe8
Author: Daniil Ovchinnikov <daniil.ovchinnikov@jetbrains.com>
Date:   Tue Oct 27 18:51:12 2015 +0300

    [groovy] map access improvements

    - Do not resolve reference if qualifier is inheritor of `java.util.Map`.
    - Get inferred type from map access only when qualifier is not resolved to a class.
    - Use qualifier's full type instead of nominal type when getting map values type.