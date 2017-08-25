commit 701160acd958f81a190f96a2e9da6baeafbe0bf8
Author: iglocska <andras.iklody@gmail.com>
Date:   Tue Feb 10 14:42:24 2015 +0100

    Fixed an issue with the free-text import failing on more than ~100 parsed values, fixes #389

    - Caused by a 1k variable / form limit imposed by php since 5.3.9
    - Form data now collected by JS and passed as a single JSON in the POST request
    - Allows massive IOC lists to be imported
    - improved performance