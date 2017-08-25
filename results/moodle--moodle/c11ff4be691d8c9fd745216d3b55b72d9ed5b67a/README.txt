commit c11ff4be691d8c9fd745216d3b55b72d9ed5b67a
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Jul 24 12:42:33 2013 +0100

    MDL-40857 External tool (LTI) capabilitiy names confusing.

    I have improved the names in the language file so that they actually say
    what they do (based on my reading of the code). I have also added
    comments to access.php explaining each capability.

    I have corrected a few of the RISK in access.php which were wrong.

    I have changed a couple of the archetypes so that guests cannot launch
    LTI activities, and editing teachers cannot do editing things.