commit da810c942fb29cf948128c07191b5ac522d88745
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Mar 17 19:37:37 2011 +0100

    (GRADLE-1446) Customizations within before/whenConfigured hooks are honored by the tooling api. Details:

    - Added EclipseDomainModel that is accessible via EclipsePlugin. This will contain entire model for of eclipse plugin. Currently it is small but will grow.
    - enabled and refactored integration test that validates that before/whenConfigured are honored by tooling api.
    - ModelBuilder now uses the eclipse plugin model instead of eclipse generator tasks
    - Rename job at GeneratorTask