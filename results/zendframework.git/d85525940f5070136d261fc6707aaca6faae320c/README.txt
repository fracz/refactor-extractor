commit d85525940f5070136d261fc6707aaca6faae320c
Author: Maks3w <github.maks3w@virtualplanets.net>
Date:   Tue Jun 12 23:18:55 2012 +0200

    [Mail] Various improvements.

    - Fix a issue with headers being rendered as CRCRLF with SMTP Protocol (ZF2-185)
    - Add test case to detect the above issue and trace the full command sended.
    - Add check in SMTP Transport for require at least one recipient if at least one DATA (header or body) will send per RFC-2821