commit aeab939ae2a45455472b08448a34ab970735de49
Author: Oliver Gierke <ogierke@pivotal.io>
Date:   Wed Nov 26 12:40:13 2014 +0100

    DATAJPA-631 - ResourceLoader improvements in ClasspathScanningPersistenceUnitPostProcessor.

    A custom ResourceLoader configured on ClasspathScanningPersistenceUnitPostProcessor is now propagated to the ClassPathScanningCandidateComponentProvider used for looking up entity classes. We also forward the configured environment so that profile annotations are evaluated as well.

    Related issue: DATACMNS-591.