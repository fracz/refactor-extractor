commit 6837111bda5e4df9bdfa617c78c1ea32ced7ab5c
Author: Chris Beams <cbeams@vmware.com>
Date:   Sun Oct 9 20:32:21 2011 +0000

    Refactor AnnotationUtils#findAllAnnotationAttributes

    Remove all convenience variants of #findAllAnnotationAttributes and
    refactor the remaining method to accept a MetadataReaderFactory
    instead of creating its own SimpleMetadataReaderFactory internally.
    This allows clients to use non-default class loaders as well as
    customize the particular MetadataReaderFactory to be used (e.g.
    'simple' vs 'caching', etc).

    Issue: SPR-8752