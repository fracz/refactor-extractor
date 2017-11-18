commit a22a73d24c1559edeb1972a817d7be79cfa1192e
Author: Luke Daley <ld@ldaley.com>
Date:   Tue Aug 21 14:00:41 2012 +0100

    Made the base file store impl more generic and improved test coverage.

    The short circuit behaviour of not copying in files when there is an existing file at the destination is now a subclass variant. It might be better to do this as a decorator, but the paths inside the filestore aren't part of the API. They just happen to be for the filestore that keys by path.