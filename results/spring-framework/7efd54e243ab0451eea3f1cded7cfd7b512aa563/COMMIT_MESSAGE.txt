commit 7efd54e243ab0451eea3f1cded7cfd7b512aa563
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Fri Feb 14 16:11:08 2014 -0800

    Additional caching for ResolvableTypes

    Add additional caching to ResolvableTypes and SerializableTypeWrapper
    in order to improve SpEL performance.

    Issue: SPR-11388