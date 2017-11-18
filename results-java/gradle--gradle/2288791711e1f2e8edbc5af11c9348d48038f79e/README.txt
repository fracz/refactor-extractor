commit 2288791711e1f2e8edbc5af11c9348d48038f79e
Author: Eric Wendelin <eric@gradle.com>
Date:   Tue Jan 24 09:30:50 2017 -0700

    Revert changes for custom plugin portal

    Revert 634e8884b8c8af88de7b45400d9448dcfb7687cf - add plugin resolution improvements to release notes
    Revert 7164418390ebfa366eac77a8064d94ab386b7857 - Make pluginRepositories available in init scripts
    reverts commit a1f3919be2d8b18e70c9777ba768b95272a08d1c.
    reverts commit e996ec71a78dfd88d43f0e9bf589ca8b29c3160c.
    reverts commit 02ae8d7131ed123238eb3a6dd200dfc8e8b360f7.
    reverts commit 72f57e195af6e677316252f59965c17e889bb6bf.
    reverts commit 9dcc63dad116b55cb860c89e3d749d58c787fbbe.
    Revert e0b7a1c9febf72f8ca8a00a9e7f68089b3cda9ce - Tests and bug fixes for custom plugin portal
    reverts commit e8805c1616912322e5441016e38329e11af3ca63.
    reverts commit 20b815f768dea7f3bca48664233d13174d2e0f60.
    Revert 0ae9da185d0df9b5831dce48eca21118d1fac5d7 - Move PluginId to interface
    Revert 52c8306d872778404cbbd117bc2214da9f76e0c1 - Public interface changes for custom plugin portal
    reverts commit 27ec8f7e535778f4fbb93df09937b027ac1ca488.
    Revert 1b3f52920d8bf82be470d1aa972af21af5902f32 for custom plugin portal
    Revert 71d6282cecba8ac5becad3f13f29f122212c0ff2 for custom plugin portal
    reverts commit 500f9a90ea1775f4b5fe02fa9122750a10e90a5c.
    Revert fa2963120a9d84bc156cb7418339ffccc2f88629 for custom plugin portal
    Revert 7141b00cd487e265b08aaef364477870ccb38655 for custom plugin portal