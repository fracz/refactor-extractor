commit b405040dd508d0cc8147c6de9b555824cd79f458
Author: Craig Taverner <craig@amanzi.com>
Date:   Sun May 15 14:50:17 2016 +0200

    Cleaned up use of geometry internal types and improved testing

    * removed intetnal ActsAsMap
    * added explicit type checks for various public types in JSON serialization
    * added Geometry.getGeometryType for improved serialization and alignment with JTS