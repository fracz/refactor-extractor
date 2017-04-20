commit bb11c181bc0f7e8ca40fc4b5d0f32f8274b2db6e
Author: Matt Cockayne <matt@zucchi.co.uk>
Date:   Sat Oct 8 09:23:29 2011 +0100

    Updated Module/Manager for improved dependencies

    Updated Module/Manager to allow better handling
    of Module Dependancies & provision.

    Now handles dependancies by default is they exist

    evaluates min required modules

    adds module versioning to application structure,
    this should allow for the building of "installer" scripts based on versions

    Also allows easy retrieval of dependancies/provisions
    post init