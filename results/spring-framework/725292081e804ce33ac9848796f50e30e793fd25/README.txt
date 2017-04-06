commit 725292081e804ce33ac9848796f50e30e793fd25
Author: Sam Brannen <sam@sambrannen.com>
Date:   Wed Jul 29 15:27:06 2015 +0200

    Introduce 'value' alias for 'attribute' in @AliasFor

    SPR-11512 introduced support for annotation attribute aliases via
    @AliasFor, requiring the explicit declaration of the 'attribute'
    attribute. However, for aliases within an annotation, this explicit
    declaration is unnecessary.

    This commit improves the readability of alias pairs declared within an
    annotation by introducing a 'value' attribute in @AliasFor that is an
    alias for the existing 'attribute' attribute. This allows annotations
    such as @ContextConfiguration from the spring-test module to declare
    aliases as follows.

    public @interface ContextConfiguration {

         @AliasFor("locations")
         String[] value() default {};

         @AliasFor("value")
         String[] locations() default {};

        // ...
    }

    Issue: SPR-13289