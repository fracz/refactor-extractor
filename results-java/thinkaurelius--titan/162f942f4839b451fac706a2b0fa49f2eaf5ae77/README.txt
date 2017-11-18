commit 162f942f4839b451fac706a2b0fa49f2eaf5ae77
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed May 29 18:50:45 2013 -0400

    Call AbstractElement getID() insteado of getId()

    Relying on capitalization significance to differentiate methods on
    this object seems like asking for trouble.  For now, just fix the
    capitalization bug introduced by
    37b84fb6425485bd5f2ab0396885a9a9d616e47c, but we might want to
    refactor getID() and getId() to more distinct names.