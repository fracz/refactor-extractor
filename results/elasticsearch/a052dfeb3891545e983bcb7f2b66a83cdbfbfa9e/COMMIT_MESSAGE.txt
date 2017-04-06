commit a052dfeb3891545e983bcb7f2b66a83cdbfbfa9e
Author: Ryan Ernst <ryan@iernst.net>
Date:   Sun Jan 31 17:22:56 2016 -0800

    Plugins: Reduce complexity of plugin cli

    The plugin cli currently is extremely lenient, allowing most errors to
    simply be logged. This can lead to either corrupt installations (eg
    partially installed plugins), or confused users.

    This change rewrites the plugin cli to have almost no leniency.
    Unfortunately it was not possible to remove all leniency, due in
    particular to how config files are handled.

    The following functionality was simplified:
    * The format of the name argument to install a plugin is now an official
      plugin name, maven coordinates, or a URL.
    * Checksum files are required, and only checked, for official plugins
      and maven plugins. Checksums are also only SHA1.
    * Downloading no longer uses a separate thread, and no longer has a timeout.
    * Installation, and removal, attempts to be atomic. This only truly works
      when no config or bin files exist.
    * config and bin directories are verified before copying is attempted.
    * Permissions and user/group are no longer set on config and bin files.
      We rely on the users umask.
    * config and bin directories must only contain files, no subdirectories.
    * The code is reorganized so each command is a separate class. These
      classes already existed, but were embedded in the plugin cli class, as
      an extra layer between the cli code and the code running for each command.