commit c1894b85270f769d960ace3c29f737f40588bcb2
Author: Alexander Falkenstern <alexander.falkenstern@gmail.com>
Date:   Sat Dec 10 16:45:45 2016 +0100

    Reworked pull request #3000: i2c mcp23017 Binding by dfliess (#4464)

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

    * Revert "Merge remote-tracking branch 'remotes/origin/features/hideki'"

    This reverts commit e30acafb3adaa7dfb79e6a1a9c06b18afad14a97, reversing
    changes made to 2754e101b2b4b37bbbad1d5c4ebbbde3865ce8ed.

    * Clean up PR.

    * Run eclipse formatter.

    * Correct @since and @autor tags.

    * Add missed space.

    * Use parameterized logging instead of string concatenation.

    * Use parameterized logging instead of string concatenation.

    * Bundle-ActivationPolicy should be set to "lazy"

    * Add README.md

    * Minor.