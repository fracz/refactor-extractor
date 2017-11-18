commit 9a5b61ccc83303ceeec2059f58c1977af9faa9e3
Author: Raph Levien <raph@google.com>
Date:   Tue Apr 29 18:26:48 2014 -0700

    Parsing of XML font configuration files for Minikin

    This patch improves Minikin-based font handling, to deal with error
    conditions (missing fonts and so on), and also moves the parsing of
    fallback_fonts.xml and system_fonts.xml into Java code.

    Change-Id: Ib0debdbd56ad3b0196be6d2a35668d711c98f1e5