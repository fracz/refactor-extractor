commit cddbcebd42c12d8e81d50c0bef50241d217f4274
Author: emcmanus <emcmanus@google.com>
Date:   Fri Jan 16 15:08:57 2015 -0800

    In @AutoValue classes, implement an abstract method `Builder toBuilder()` if there is one. Also, improve some error messages about incorrect types by including type parameters. For example, if the Builder.build() method is supposed to return Foo<T> then say Foo<T> and not just Foo.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=84167112