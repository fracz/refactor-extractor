commit 7b28967ea58630c3cbdb93f4355d1bb96fda6f02
Author: Michal Čihař <mcihar@suse.cz>
Date:   Tue Aug 27 10:06:04 2013 +0200

    Upgrade TCPDF to 6.0.023

    Changes:

    6.0.023 (2013-08-05)
        - GNU Freefont fonts were updated.
        - Licensing and copyright information about fonts were improved.
        - PNG image support was improved.

    6.0.022 (2013-08-02)
        - fixing initialization problem for signature_appearance property.

    6.0.021 (2013-07-18)
        - The bug caused by the preg_split function on some PHP 5.2.x versions was fixed.

    6.0.020 (2013-06-04)
        - The method addTTFfont() was fixed (Bug item #813 Undefined offset).

    6.0.019 (2013-06-04)
        - The magic constant __DIR__ was replaced with dirname(__FILE__) for php 5.2 compatibility.
        - The exceptions raised by file_exists() function were suppressed.