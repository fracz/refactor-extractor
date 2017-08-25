commit 001feb6649acf97367525d3c345cff340a1f2427
Author: Ruslan Kabalin <r.kabalin@lancaster.ac.uk>
Date:   Mon Jun 29 15:12:10 2015 +0100

    MDL-50887 antivirus: Refactor antivirus scanning to use new plugin.

    This patch moves existing scanning functionality to plugin level. It does
    not add anything new, just refactors the existing functionality.

    AMOS BEGIN
      MOV [clamemailsubject,core],[emailsubject,antivirus]
      MOV [clamfailed,core],[clamfailed,antivirus_clamav]
      MOV [clamlost,core],[invalidpathtoclam,antivirus_clamav]
      MOV [clamunknownerror,core],[unknownerror,antivirus_clamav]
      MOV [virusfounduser,core],[virusfounduser,antivirus]
    AMOS END