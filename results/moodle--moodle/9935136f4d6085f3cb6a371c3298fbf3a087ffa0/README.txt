commit 9935136f4d6085f3cb6a371c3298fbf3a087ffa0
Author: Sam Hemelryk <sam@moodle.com>
Date:   Fri Jun 28 12:39:18 2013 +1200

    MDL-40368 tool_capability: overall improvements made

    * Can now compare multiple capabilities at the same time.
    * Capability search now persists between requests.
    * Upgraded to use renderers.
    * Upgraded module.js to YUI shifted module.
    * Converted deprecated calls.
    * Converted forms to Moodleforms.
    * Implemented backend functionality to overview at
      different context levels. Not possible presently as
      there are no front end hooks.