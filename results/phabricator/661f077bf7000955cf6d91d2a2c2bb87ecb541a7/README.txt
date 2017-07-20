commit 661f077bf7000955cf6d91d2a2c2bb87ecb541a7
Author: epriestley <git@epriestley.com>
Date:   Mon Oct 10 19:22:30 2011 -0700

    Replace callsites to sha1() that use it to asciify entropy with
    Filesystem::readRandomCharacters()

    Summary: See T547. To improve auditability of use of crypto-sensitive hash
    functions, use Filesystem::readRandomCharacters() in place of
    sha1(Filesystem::readRandomBytes()) when we're just generating random ASCII
    strings.

    Test Plan:
      - Generated a new PHID.
      - Logged out and logged back in (to test sessions).
      - Regenerated Conduit certificate.
      - Created a new task, verified mail key generated sensibly.
      - Created a new revision, verified mail key generated sensibly.
      - Ran "arc list", got blocked, installed new certificate, ran "arc list"
    again.

    Reviewers: jungejason, nh, tuomaspelkonen, aran, benmathews

    Reviewed By: jungejason

    CC: aran, epriestley, jungejason

    Differential Revision: 1000