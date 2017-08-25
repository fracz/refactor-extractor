commit 5c2b629d3622fa64d329601138cb3a043f938bba
Author: Greg Anderson <greg.1.anderson@greenknowe.org>
Date:   Wed Jun 28 20:13:00 2017 -0700

    Factor out Configuration classes into a separate project (#600)

    * Factor out Configuration classes into a separate project, consolidation\config.

    * Update to new Consolidation\Config\Loader namespace.

    * Update for consolidation/config refactoring: Rename InjectConfigForCommand and ApplyConfig to ConfigForCommand and ConfigForSetters, respectively, ane move them to the Consolidation\Config\Inject namespace.

    * Use 1.0.0 stable of consolidation/config.