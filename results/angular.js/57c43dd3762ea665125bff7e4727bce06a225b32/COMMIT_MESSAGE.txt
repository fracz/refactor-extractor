commit 57c43dd3762ea665125bff7e4727bce06a225b32
Author: Brian Ford <btford@umich.edu>
Date:   Thu Aug 22 12:32:42 2013 -0700

    docs(module): improve the installation instructions for optional modules

    Currently, the documentation does a bad job of explaining the distinction between the services that it provides,
    and the module itself. Furthermore, the instructions for using optional modules are inconsistent or missing.
    This commit addresses the problem by ading a new `{@installModule foo}` annotation to the docs generator that
    inlines the appropriate instructions based on the name of the module.