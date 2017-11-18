commit 5b4f979a54b18b614efd264c2b71d6df38e666a2
Author: Jesse Wilson <jwilson@squareup.com>
Date:   Sat Jan 10 10:19:32 2015 -0500

    Nested classes and imports.

    This changes TypeSpec to no longer know what its enclosing package,
    and potentially also not its enclosing class if there was one. This
    makes TypeSpec work the same as FieldSpec and MethodSpec, which also
    don't know how they are enclosed. As a model I think it's simpler,
    particularly since it was often a lie anyway: what is the fully qualified
    name of an anonymous class?

    This also improves imports for nested classes.