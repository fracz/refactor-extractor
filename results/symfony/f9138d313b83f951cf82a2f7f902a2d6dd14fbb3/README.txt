commit f9138d313b83f951cf82a2f7f902a2d6dd14fbb3
Author: Jeremy Mikola <jmikola@gmail.com>
Date:   Mon Jan 24 14:50:31 2011 -0500

    [FrameworkBundle] Implemented single-pass config loading with intelligent option merging for FrameworkExtension

    Restructured config format to make processing more straightforward. Important changes that might break existing configs:

     * Added "enabled" option for translator (improves multi-format compat)
     * Removed hash variation of validation annotations option (only boolean)
     * Moved namespace option directly under validation (improves multi-format compat)

    The new merge process depends on an internal array of all supported options and their default values, which is used for both validating the config schema and inferring how to merge options (as an added benefit, it helps make the extension self-documenting). Exceptions will now be thrown for merge errors resulting from unrecognized options or invalid types. Since incoming configurations are all merged atop the defaults, many isset() checks were removed. As a rule of thumb, we probably only want to ignore null values when an option would be used to set a parameter.

    Also:

     * Added missing attributes to symfony-1.0.xsd
       * profiler: added only-exceptions attribute
       * session: fix types and add pdo attributes
     * Create FrameworkExtension tests with PHP/XML/YAML fixtures
     * Use "%" syntax instead of calling getParameter() within FrameworkExtension
     * Normalize config keys and arrays with helper methods for PHP/XML/YAML compatibility

    Earlier changes:

     * Remove nonexistent "DependencyInjection/Resources/" path from XmlFileLoaders
     * Remove hasDefinition() checks, as register methods should only execute once
     * Remove first-run logic from registerTranslatorConfiguration(), as it is only run once
     * Removed apparently obsolete clearTags() calls on definitions for non-enabled features