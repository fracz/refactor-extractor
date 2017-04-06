commit a598123838291b35cfa41445acc4119e1ab44b8b
Author: Tanguy Leroux <tlrx.dev@gmail.com>
Date:   Wed Apr 1 14:05:10 2015 +0200

    AbstractBlobContainer.deleteByPrefix() should not list all blobs

    The current implementation of AbstractBlobContainer.deleteByPrefix() calls AbstractBlobContainer.deleteBlobsByFilter() which calls BlobContainer.listBlobs() for deleting files, resulting in loading all files in order to delete few of them. This can be improved by calling BlobContainer.listBlobsByPrefix() directly.

    This problem happened in #10344 when the repository verification process tries to delete a blob prefixed by "tests-" to ensure that the repository is accessible for the node. When doing so we have the following calling graph: BlobStoreRepository.endVerification() -> BlobContainer.deleteByPrefix() -> AbstractBlobContainer.deleteByPrefix() -> AbstractBlobContainer.deleteBlobsByFilter() -> BlobContainer.listBlobs()... and boom.

    Also, AbstractBlobContainer.listBlobsByPrefix() and BlobContainer.deleteBlobsByFilter() can be removed because it has the same drawbacks as AbstractBlobContainer.deleteByPrefix() and also lists all blobs. Listing blobs by prefix can be done at the FsBlobContainer level.

    Related to #10344