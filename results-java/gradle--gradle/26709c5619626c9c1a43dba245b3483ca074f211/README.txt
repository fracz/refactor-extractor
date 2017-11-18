commit 26709c5619626c9c1a43dba245b3483ca074f211
Author: daz <darrell.deboer@gradleware.com>
Date:   Tue Mar 18 09:36:55 2014 -0600

    Fixed caching of module metadata so that isMetaDataOnly is cached

    - This improves the state of resolving maven modules with packaging='pom':
      these will now resolve correctly when online, but HEAD requests are made for every resolve