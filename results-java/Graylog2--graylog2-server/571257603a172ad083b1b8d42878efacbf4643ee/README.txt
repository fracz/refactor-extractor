commit 571257603a172ad083b1b8d42878efacbf4643ee
Author: Dennis Oelkers <dennis@torch.sh>
Date:   Thu Feb 13 11:23:13 2014 +0100

    Extracting shared parts of server/radio configuration to base class

    This allows us to reference the super class in shared parts of the code
    and access common properties, which helps us to refactor some parts of
    the code to more generic semantics.