commit 623966a396316b9ae82f60cae59d2899a3b317e5
Author: Petr Skoda <commits@skodak.org>
Date:   Wed Apr 13 11:54:58 2011 +0200

    MDL-17344 fix case and unicode related profile field issues in user upload

    This fixes issue with uppercase and non-ascii profile fields and closes the iterator properly when field not found. Please note that profile fields with upper case letters must be specified exactly in CSV file headers. includes improved docs and parameter typo fix (credit for the parameter typo discovery goes to Aparup Banerjee)