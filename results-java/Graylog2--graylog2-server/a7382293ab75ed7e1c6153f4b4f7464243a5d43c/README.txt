commit a7382293ab75ed7e1c6153f4b4f7464243a5d43c
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Jan 7 15:01:06 2015 +0100

    refactor ObjectMapper provider into proper modules

    * use modules instead of singleton providers to bind the specific object mappers around the code base
    * remove reflection for shared bindings modules
    * register context resolver for rest api service using an injected objectmapper
    * fix mongojack property name strategy to avoid seeing _id everywhere
    * fix _id for blacklist filter resource (due to type polymorphism it doesn't work by default)