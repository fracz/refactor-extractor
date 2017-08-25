commit f9f71ad37706cbe37ebc8af34aa55c029369c075
Author: Ermal Luçi <eri@pfsense.org>
Date:   Fri Aug 14 17:13:38 2009 +0000

    * Convert captive portal rules to use tables. This reduces the number of rules ALOT.
    * Make the peruserbw setting use tables also by taking advantage of the tablearg option.
    * Convert statistics to use the new improvements of ipfw tables merged previously.
    * Make the limit of users allowed around 25000 instead of 9999 of before.

    NOTE: The only thing remaining for full optimization on ipfw(4) side is converting passthrumac and layer2 secure rules to tables aswell.