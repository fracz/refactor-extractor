<?php

namespace VersionPress\DI;

final class VersionPressServices {
    const STORAGE_FACTORY = 'storage-factory';
    const MIRROR = 'mirror';
    const DB_SCHEMA = 'db-schema';
    const DATABASE = 'database';
    const COMMITTER = 'committer';
    const INITIALIZER = 'initializer';
    const SYNCHRONIZATION_PROCESS = 'synchronization-process';
    const SYNCHRONIZER_FACTORY = 'synchronizer-factory';
    const REVERTER = 'reverter';
    const REPOSITORY = 'repository';
    const VP_CONFIGURATION = 'vp-configuration';
    const PLAIN_WPDB = 'wpdb';
    const VPID_REPOSITORY = 'vpid-repository';

    private function __construct() {
    }
}