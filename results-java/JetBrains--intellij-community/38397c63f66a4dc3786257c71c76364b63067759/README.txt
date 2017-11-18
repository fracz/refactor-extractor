commit 38397c63f66a4dc3786257c71c76364b63067759
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Thu Nov 27 16:02:22 2014 +0300

    Make several improvements in AddImportHelper

    * Added missing javadoc and @Nullable/@NotNull annotations to
    several methods
    * Renamed addImportFromStatement to addFromImportStatement to be
    consistent with PSI
    * Renamed addImportFrom to addOrUpdateImportFrom to emphasise its
    intended behavior