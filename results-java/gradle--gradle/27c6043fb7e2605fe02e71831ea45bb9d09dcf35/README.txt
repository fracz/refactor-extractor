commit 27c6043fb7e2605fe02e71831ea45bb9d09dcf35
Author: Rodrigo B. de Oliveira <rodrigo@gradle.com>
Date:   Thu Aug 17 17:00:35 2017 -0300

    Polish `CorePluginResolver`

     - Compose method
     - Add static imports to shorten lines and improve flow
     - Call `getNamespace()` only once to avoid garbage