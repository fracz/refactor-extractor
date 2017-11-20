commit cb454a469413c227e0d869241bda5799abe7d42d
Author: Liam Miller-Cushon <cushon@google.com>
Date:   Mon Sep 21 12:37:31 2015 -0700

    Allow Suppression documentation to be suppressed

    This allows the documentation to be customized for checks with special
    requirements, like MultipleTopLevelClasses and PackageLocation.

    Also, various improvements to documentation generation:

    * use Gson for serialization, instead of bespoke tab-delimited format
    * use a templating library instead of string munging and MessageFormat

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=103567245