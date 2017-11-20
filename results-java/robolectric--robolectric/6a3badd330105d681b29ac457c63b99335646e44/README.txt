commit 6a3badd330105d681b29ac457c63b99335646e44
Author: Jonathan Gerrish <jonathan@indiekid.rg>
Date:   Wed Dec 28 08:37:44 2016 -0800

    Use ResourceIdGenerator for missing Android IDs in PackageResourceIndex. This will be refactored again in the future to generate the IDs as the XML files are being processed and the resources loaded into the index.