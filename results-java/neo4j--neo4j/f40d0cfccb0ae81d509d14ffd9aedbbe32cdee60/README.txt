commit f40d0cfccb0ae81d509d14ffd9aedbbe32cdee60
Author: Jonas Kalderstam <jonas@kalderstam.se>
Date:   Thu Mar 9 15:09:44 2017 +0100

    Refactor config api slightly

    * Moves information from SettingGroup-level to Setting-level
      This improves the ability to identify deprecations, internal
      settings, and similarly. Especially for connectors, which
      previously were not able to set the values for the containing
      settings.
    * Adds several methods for use in config parsing located in
      BaseSetting. These values are set via annotations at startup
      when settings are loaded via ServiceLoading in LoadableConfig.

    As a consequence:

    * Connector-settings (like dbms.connector.http.enabled) are now
      listed in the output of dbms.listConfig procedure, with correct
      descriptions and everything.
    * Connector-settings are included in the normal deprecation
      warnings.
    * All settings can now be documented via the config api.