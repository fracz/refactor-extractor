commit 00a0ee3c080a0980c21257d3067f1cfab99e84ca
Author: Karel Goderis <karel.goderis@me.com>
Date:   Fri Jan 10 21:50:26 2014 +0100

     Many many Sonos binding fixes and improvements
     - Fix issues #433, #476 and #608
     - Prepare support for all types of Sonos HW
     - Improve network scanning
     - Replace pollingthread by selective polling based on Quartz Jobs
     - Make binding a "good" openHAB citizen (new base abstract class)
     - Changed item binding syntax (removed <direction>)
     - Improved Volume control
     - Improved PlayList and Radio URI management