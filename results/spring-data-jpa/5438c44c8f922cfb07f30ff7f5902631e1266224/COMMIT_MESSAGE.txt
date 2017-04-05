commit 5438c44c8f922cfb07f30ff7f5902631e1266224
Author: Oliver Gierke <ogierke@gopivotal.com>
Date:   Wed Mar 12 15:30:46 2014 +0100

    DATAJPA-173 - Extended support for metadata detection on CRUD methods.

    Extended the mechanism previously existing to detect @Lock annotations on redeclared CRUD methods into one being able to transport arbitrary metadata into the execution of CRUD methods.

    Renamed LockModeRepositoryPostProcessor to CrudMethodMetadataPostProcessor, refactored the internals and added some metadata caching to avoid repeated reflection lookups to evaluate annotations.