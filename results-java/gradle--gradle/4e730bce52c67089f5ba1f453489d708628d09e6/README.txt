commit 4e730bce52c67089f5ba1f453489d708628d09e6
Author: daz <darrell.deboer@gradleware.com>
Date:   Tue Mar 18 09:36:55 2014 -0600

    Fixed caching of module metadata so that isMetaDataOnly is cached

    - This improves the state of resolving maven modules with packaging='pom':
      these will now resolve correctly when online, but HEAD requests are made for every resolve