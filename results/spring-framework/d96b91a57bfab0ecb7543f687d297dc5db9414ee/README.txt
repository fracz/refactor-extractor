commit d96b91a57bfab0ecb7543f687d297dc5db9414ee
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Mon Jan 20 13:33:24 2014 -0800

    Fix SerializableTypeWrapper equals() performance

    Change SerializableTypeWrapper proxies to directly call equals() methods
    on the underlying Type, rather than possibly generating more wrappers.

    This should help to improve performance, especially as the equals()
    method is called many times when the ResolvableType cache is checked.

    Issue: SPR-11335