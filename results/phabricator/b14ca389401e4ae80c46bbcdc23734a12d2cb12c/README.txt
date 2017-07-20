commit b14ca389401e4ae80c46bbcdc23734a12d2cb12c
Author: Bob Trahan <btrahan@phacility.com>
Date:   Thu Apr 9 15:07:12 2015 -0700

    Conpherence - improve stack re: non-update updates

    Summary:
    Fixes T7761. Fixes T7318.

    When we send an empty message to the server, pretend its just a request to load the page. Make load a bit smarter such that if we don't get back any transactions, rather than error like the fool, just send down to the client the notion of a 'non_update'. Instrument the client to just turn off the appropriate loading state, etc for a non update.

    T7318 is a tricky beast since we don't know exactly how to reproduce it but if / when it occurs again it would be some other bizarre application behavior maybe? We won't be getting the execption anymore, that's for sure.

    Test Plan: removed code in `ConpherenceThreadManager.sendMessage` that protects against sending empty messages. sent empty messages (non updates) like whoa and everything worked on both durable column and main column view. re-added the code in `ConpherenceThreadManager.sendMessage` and noted empty messages did not send while any text including a space sent up nicely

    Reviewers: epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T7318, T7761

    Differential Revision: https://secure.phabricator.com/D12339