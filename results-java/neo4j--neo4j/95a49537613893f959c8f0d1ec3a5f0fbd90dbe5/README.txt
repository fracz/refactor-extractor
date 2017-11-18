commit 95a49537613893f959c8f0d1ec3a5f0fbd90dbe5
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Wed Nov 28 11:06:14 2012 +0200

    Introduces BindingNotifier

    BindingNotifier formalizes the concept of a provider of binding addresses, for registering BindingListeners
    ClusterClient and ProtocolServer implement that
    Extracts HighAvailabilityMemberContext as an interface and calls the (single) implementation SimpleHAMC. That
      will be useful later for proxying it for rolling upgrades
    Slight refactoring in MultiPaxosServerFactory