commit 28cdd4ecd896bfd35fe0b430069dd9329a857598
Author: Roman Chernyatchik <roman.chernyatchik@jetbrains.com>
Date:   Tue Jan 19 16:35:09 2010 +0300

    Coverage Suites:
    1. CoverageSuite extracted from BaseCoverageSuite for TC compatibility
    2. Usefull methods were moved to BaseCoverageSuite from CoverageSuiteImpl

    CoverageEnabledConfiguration
    1. Separated to Java and common part
    2. Removed strange logic related to coverage file data path when coverage runner is unknown
    3. Removed unused method getRunnerId
    4. Coverage Configurations for different RunConfigurations should be provided using CoverageSupportProvider
    5. RunConfiguration extension for Java run configurations refactored according new JavaCoverageEnabledConfugations