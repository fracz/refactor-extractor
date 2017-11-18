commit fe69185cd4c5e77537df5edec7485444893739b2
Author: Ilya Chernikov <ilya.chernikov@jetbrains.com>
Date:   Tue Sep 20 23:34:14 2016 +0200

    Refactor script definition and related parts:

     - simplify script definition interface, convert it to class
     - create simple definitions right from base
     - refactor (rename and simplify) script definition with annotated template
     - simplify usages of script definition in many places