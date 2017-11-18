commit 8c96373c23d481e52a4f39a75f9707bbe84f4c65
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Mon Jan 21 15:33:46 2013 +0200

    Increases visibility of members in KernelData and Version

    KernelData constructor becomes protected from package protected
    Version.getKernelVersion() becomes public from package protected

    These are so refactorings and package moves in enterprise/ha are possible