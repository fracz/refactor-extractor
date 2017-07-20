commit c9d698f4fb8f9ed683bc737b7991edb3793fa498
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Thu Nov 13 15:15:51 2014 +1300

    Moved the `tmp/` path into the config (was hardcoded everywhere)

    The `tmp/` path was hardcoded everywhere, which resulted in using `SettingsPiwik::rewriteTmpPathWithInstanceId()` to rewrite it for specific use cases.

    I've moved that path into the config, and replaced all hardcoded usage (and calls to `rewriteTmpPathWithInstanceId()`) by a `get()` from the container.

    Getting entries from the container is a bad practice and dependency injection should be preferred, but we do baby steps. When refactoring those classes to DI, we'll replace calls to the container with proper dependency injection.
    Another thing we'll need to do too is move the hardcoded *sub-path* of `tmp/` (e.g. `tmp/sessions/`) into the config also (but again: baby steps).

    Another future step would be to remove completely instance ID and let it be handled by a plugin (or by end-user config). Having the `tmp/` path in the config means that plugins or users can override it and know it will be taken into account everywhere in Piwik.