commit b2a0835c002b0147bad2632fafd1d17d3b511de5
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Dec 15 15:33:17 2014 +0300

    EditSource action improved for commit and push dialog;

    * close appropriate model dialog after action performed if needed;
    * invoke later deprecated call changed;
    * action constructor now depends on parent component instead of dialog wrapper