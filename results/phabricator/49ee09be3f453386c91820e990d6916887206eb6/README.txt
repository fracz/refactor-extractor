commit 49ee09be3f453386c91820e990d6916887206eb6
Author: Bob Trahan <btrahan@phacility.com>
Date:   Fri Jan 9 14:53:56 2015 -0800

    Tokenizers - improve algorithm around exact matches.

    Summary: Fixes T6102. Gets the from  D10867 running in the tokenizer. This thing is pretty copy / pastey, but I guess that's okay?

    Test Plan: looked at a project //tokenizer// and typed "project". since I have a million things with the word "project" in it, I was delighted to see the "project" project first in this project tokenizer.

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T6102

    Differential Revision: https://secure.phabricator.com/D11305