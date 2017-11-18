commit 2564575cae3f4b8930743e0f4ef0dd86c70516aa
Author: Jacob Hansson <jakewins@gmail.com>
Date:   Fri May 22 13:08:47 2015 -0500

    Introduce SystemInformation service, hook into UDC.

    UDC has a complex dependency hierarchy, and because of that it uses reflection
    to extract information from system components. This introduces a "SystemInformation"
    service where components publish metadata and UDC can access it without reflection.

    Also minor improvements to UDC resilience - only store up to 10 latest client
    names and urlencode query parameters in UDC ping payload.