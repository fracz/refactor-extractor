commit 7b9d5fff89ad56493d3478c52cf8ac7ec4339615
Author: Abyr Valg <valga.github@abyrga.ru>
Date:   Sat Jul 15 16:27:11 2017 +0300

    Use single method for all retry-configurators (#1405)

    This refactors all configuration-retries to using a single function,
    and updates its error checking algorithm to match the latest app behavior.

    * Keep HTTP response

    * Generic configureWithRetries() method

    * Use generic configurator for direct videos

    * Use generic configurator for single videos

    * Use generic configurator for albums

    * Tweak PHPDoc

    * Tweak comment

    * Tweak exception message

    * Add is-er for HTTP response

    * Check also failed responses in configurator

    * Configurator should sleep at least 1 sec