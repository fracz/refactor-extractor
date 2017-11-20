commit 48a8658c8f2a9c9d63cb3fed36be5756b27c55cc
Author: vgrebenschikov <vova@fbsd.ru>
Date:   Mon Dec 19 11:24:44 2016 +0300

    plclogo - binding support for Siemens PLCLogo (#4861)

    * First iteration of plclogo binding for Siemens LOGO! 0BA7
    Needs refactor/comments and beutification

    * Added missing files

    * I2C binding 1.8

    * I2C binding 1.8

    * V1,1 PLCLOGO for Openhab

    * i2c v1

    * Cleanup.

    * Rename org.openhab.binding.i2c to org.openhab.binding.mcp23017

    * Integrate Pi4J 1.1 snapshot.

    * Use of multiple mcp23017 chips worked not properly.

    * Introduce configuration of polling interval.

    * Replace usage of EnumMap with if decisions.

    * Introduce MCP3424 binding.

    * Merge mcp23017 binding from 'remotes/dfliess/master'

    * Cleanup.

    * Rename org.openhab.binding.i2c to org.openhab.binding.mcp23017

    * Integrate Pi4J 1.1 snapshot.

    * Use of multiple mcp23017 chips worked not properly.

    * Introduce configuration of polling interval.

    * Replace usage of EnumMap with if decisions.

    * Merge mcp23017 binding from 'remotes/dfliess/master'

    * Cleanup.

    * Rename org.openhab.binding.i2c to org.openhab.binding.mcp23017

    * Integrate Pi4J 1.1 snapshot.

    * Use of multiple mcp23017 chips worked not properly.

    * Introduce configuration of polling interval.

    * Replace usage of EnumMap with if decisions.

    * Jenkins say: pom is wrong.

    * Copyright header and small documentation changes.

    * Change version.

    * Change version.

    * Ill formed documentation tags corrected.

    * Update Pi4J to latest snapshot.

    * Update project files.

    * Cleanup plclogo manifest file

    * Cleaned too much.

    * Port PLCLogo binding from Lehane Kellett to Logo8/0BA8.

    * Added mcp3424 and PLCLogo bindings for build.

    * Update Pi4J to 1.1 release

    * MCP23017 and MCP3424 builds was broken.

    * Rename "analogDelta" to "threshold".
    Extend warnings if state could not be created.

    * Update Pi4J to 1.1 release.

    * MCP23017 and MCP3424 builds was broken.

    # Conflicts:
    #       bundles/binding/org.openhab.binding.mcp23017/META-INF/MANIFEST.MF

    * Fix build settings for MCP23017 and MCP3424 bindings.

    * Initial import of hideki weather station binding.

    * Cleanup build settings for MCP23017 and MCP3424 bindings.

    * Add native sensor decoder.

    * Add binding for receiving and decoding 433Mhz hideki based weather stations.

    * Small cleanups.

    * Typo corrected.

    * Small cleanups.

    * Fix build settings for MCP23017 and MCP3424 bindings.

    * Cleanup build settings for MCP23017 and MCP3424 bindings.

    * Small cleanups.

    * Typo corrected.

    * Fix build settings for MCP23017 bindings.

    * Use classes instead of direct calculations.

    * Use consts as sensor types instead of numeric values.

    * Remove internals from external interfaces.

    * Resolve exception, if received sensor and item not matched.

    * Add missed break.

    * Tuning of decoder parameters.

    * Compile with new decoder settings.

    * avoid root privileges for the macp23017 binding

    This change avoid the root privileges for the mcp23017 binding. It
    allows to use the binding without running openhab as root.

    The main change is the call off GpioUtil.enableNonPrivilegedAccess()
    befor creating an GpioController instance

    * Migrate fix by M. Moog to avoid root privileged access from MCP23017 to MCP3424 binding.

    * Merge plclogo from g8kmh/openhab

    * Merge remote-tracking branch 'old/plclogo' into plclogo-1.8
    - unbreak build with openhab master
    - fix problem with memory locations above 7
    - simplify code of memory locations mapping
    - fix a bit configuration parsing logs at start (remove duplicates)
    - major refactoring of binding config - store memlocation in additional class
    - allow two locations - one for read another for write: <controller>:<RD>:<WR>

    * fix version to 1.9.0-SNAPSHOT

    Signed-off-by: Vladimir Grebenschikov <vgrebenschikov@gmail.com> (github: vgrebenschikov)

    * Fill in initial value for items even if matched defualt

    On first read from controller fill-in intial value for items even when thay match default value.
    So Items which are read from controller will be non-Null.

    Signed-off-by: Vladimir Grebenschikov <vova@fbsd.ru> (github: vgrebenschikov)

    * Merge support of Logo8/0BA8 and other fixes by falkena

    - 0BA8 memory map is commented (two places), but other merged changes works well on 0BA7

    * Cleanup branch.

    * Support Logo 0BA8 and configuration of controller model

    plclogo:controller1.model=0BA7
    or
    plclogo:controller2.model=0BA8

    * Change copyright header. Run eclipse source code formatter.

    * Fixed address room for Logo8.

    * Remove not used functions and members. Reflect code style.

    * Use parametrized logging.

    * Fixed parsing of memory configuration.

    * Merge plclogo from g8kmh/openhab

    * Merge remote-tracking branch 'old/plclogo' into plclogo-1.8
    - unbreak build with openhab master
    - fix problem with memory locations above 7
    - simplify code of memory locations mapping
    - fix a bit configuration parsing logs at start (remove duplicates)
    - major refactoring of binding config - store memlocation in additional class
    - allow two locations - one for read another for write: <controller>:<RD>:<WR>

    * fix version to 1.9.0-SNAPSHOT

    Signed-off-by: Vladimir Grebenschikov <vgrebenschikov@gmail.com> (github: vgrebenschikov)

    * Fill in initial value for items even if matched defualt

    On first read from controller fill-in intial value for items even when thay match default value.
    So Items which are read from controller will be non-Null.

    Signed-off-by: Vladimir Grebenschikov <vova@fbsd.ru> (github: vgrebenschikov)

    * Merge support of Logo8/0BA8 and other fixes by falkena

    - 0BA8 memory map is commented (two places), but other merged changes works well on 0BA7

    * Support Logo 0BA8 and configuration of controller model

    plclogo:controller1.model=0BA7
    or
    plclogo:controller2.model=0BA8

    * Define Logo memory block as PLCLogoBlock class, use enum as logo memory types instead of strings

    * Rewrote parsing of openhab items.

    * Fix @since tags. Formatting.

    * Align a bit memory definitions

    * FIx 0BA7 valid memory range

    * Update Moka7 copyright according to original sources copyright

    https://sourceforge.net/projects/snap7/files/Moka7/1.0.1/
    Moka7-refman.pdf

      Licensing
        Moka7 is distributed as a source code library under GNU Library or
        Lesser General Public License version 3.0 (LGPLv3).

        Basically this means that you can distribute your commercial firmware containing Moka7
        without the requirement to distribute the source code of your application and without
        the requirement that your firmware be itself distributed under LGPL.
        A small mention is however appreciated if you include it in your applications.

    * Minor project fixes

    * Use parameterized logging instead of string concatenation

    * Add example configuration.

    * Set of single bit worked not properly.
    Avoid cast from string to string.

    * Fix a bit plclogo section in openhab_default.cfg

    - align commented lines of config without spaces and text explanation with spaces as it was done in other sections
    - list supported controller models

    * Upgrade Moka7 to v1.0.2, sources are dual-licensed under EPL

    Signed-off-by: Vladimir Grebenschikov <vova@fbsd.ru> (github: vgrebenschikov)

    * Write new data to Logo if something was changed only.
    Resolve a serious deadlock can happen, if something goes wrong.
    Continue processing of items, also if no controller for item was found.

    * Update minimal amount of controller memory on commands

    - Update only one byte on bit-wise changes
    - Update only one word in case of number changes

    Signed-off-by: Vladimir Grebenschikov <vova@fbsd.ru> (github: vgrebenschikov)

    * Write to logo, if something was changed only. Cleanup.

    * Leave only EPL license in Moka7 sources, after approval by the author

    * Put EPL Copyright message exactly as standard for OpenHAB
    Specify real after under @author tag

    * Improve javadocs

    * OH2 initialization handlers, polishing

    Change authors nicknames to full-names
    Remove redundant inheritDoc
    Remove empty method override
    Generalize a bit parsing of config options
    Implement addBindingProvider and removeBindingProvider
    Add required/optional tags to default openhab config

    Signed-off-by: Vladimir Grebenschikov <vova@fbsd.ru> (github: vgrebenschikov)