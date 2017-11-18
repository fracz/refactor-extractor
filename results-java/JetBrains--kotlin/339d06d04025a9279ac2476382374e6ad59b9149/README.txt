commit 339d06d04025a9279ac2476382374e6ad59b9149
Author: Simon Ogorodnik <Simon.Ogorodnik@jetbrains.com>
Date:   Fri Oct 27 22:48:40 2017 +0300

    Improve diagnosticMissingPackageFragment reporting

    Single execution path to report missing package fragment problems

    Split failures on PluginDeclarationProviderFactory site by
    known reasons to improve exception analysis