<?php

namespace VersionPress\Cli;
use Symfony\Component\Process\Process;
use VersionPress\DI\VersionPressServices;
use VersionPress\Synchronizers\SynchronizationProcess;
use WP_CLI;
use WP_CLI_Command;
use wpdb;

/**
 * Internal VersionPress commands.
 *
 * ## USAGE
 *
 * These internal commands are not registered with WP-CLI automatically like the "public"
 * `wp vp` commands in versionpress.php. You have to manually require the file, e.g.:
 *
 *     wp --require=wp-content/plugins/versionpress/src/Cli/vp-internal.php vp-internal ...
 *
 * These internal commands are mostly used by public `wp vp` commands.
 *
 */
class VPInternalCommand extends WP_CLI_Command {


    /**
     * Restores a WP site from Git repo. Fails if the db exists.
     *
     * ## OPTIONS
     *
     * --db=<name>
     * : Name of the db, a new user in it and his password
     *
     * @synopsis --db=<name>
     *
     * @subcommand restore-site
     *
     * @when before_wp_load
     */
    public function restoreSite($args, $assoc_args) {

        if (!defined('WP_CONTENT_DIR')) {
            define('WP_CONTENT_DIR', 'xyz'); //doesn't matter, it's just to prevent the NOTICE in the require`d bootstrap.php
        }
        require_once(__DIR__ . '/../../bootstrap.php');

        // 1) Create wp-config.php
        $configCmd = 'wp core config --dbname=' . escapeshellarg($assoc_args['db']) . ' --dbuser=root';

        $process = new Process($configCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Failed creating wp-config.php");
            WP_CLI::error($process->getErrorOutput());
        } else {
            WP_CLI::success("wp-config.php created");
        }


        // 2) Create db
        $createDbCommand = 'wp db create';

        $process = new Process($createDbCommand);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Failed creating DB");
            WP_CLI::error($process->getErrorOutput());
        } else {
            WP_CLI::success("DB created");
        }


        // 3) Disable VersionPress tracking
        $dbphpFile = 'wp-content/db.php';
        if (file_exists($dbphpFile)) {
            unlink($dbphpFile);
        }
        // will be restored at the end of this method



        // 3) Create WP tables
        $createWpTablesCmd = 'wp core install --url=' . escapeshellarg("http://localhost/" . basename(getcwd())) . ' --title=x --admin_user=x --admin_password=x --admin_email=x@example.com';
        $process = new Process($createWpTablesCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Failed creating WP tables.");
            WP_CLI::error($process->getErrorOutput());
        } else {
            WP_CLI::success("WP tables created");
        }


        // Finally, clean the working copy

        $cleanWDCmd = 'git reset --hard && git clean -xf wp-content/vpdb';

        $process = new Process($cleanWDCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Could not clean working directory");
            WP_CLI::error($process->getErrorOutput());
        } else {
            WP_CLI::success("Working directory reset");
        }



        // The next couple of the steps need to be done after the WP is fully loaded; we can use finish-init-clone for that

        $finishInitCloneCmd = 'wp --require=' . escapeshellarg(__FILE__) . ' vp-internal finish-init-clone --truncate-options';

        $process = new Process($finishInitCloneCmd);
        $process->run();
        WP_CLI::log($process->getOutput());
        if (!$process->isSuccessful()) {
            WP_CLI::error("Could not finish site restore");
        }


    }


    /**
     * Initializes a clone
     *
     * ## OPTIONS
     *
     * --name=<name>
     * : Name of the clone.
     *
     * --site-url=<url>
     * : Site URL of the clone
     *
     * --force-db
     * : Normally, init-clone fails if the targed DB already exists. Specify
     * this flag to drop the db first.
     *
     * @synopsis --name=<name> --site-url=<url> [--force-db]
     *
     * @subcommand init-clone
     *
     * @when before_wp_load
     */
    public function initClone($args, $assoc_args) {

        /** @noinspection PhpIncludeInspection */
        require_once(__DIR__ . '/../../../../../wp-config.php');

        $name = $assoc_args['name'];
        $cloneUrl = $assoc_args['site-url'];

        $dbName = DB_NAME . '_' . $name;

        // 1) Create a new Git branch here
        $createBranchCommand = 'git checkout -b ' . escapeshellarg($name);
        $process = new Process($createBranchCommand);
        $process->run();

        if (!$process->isSuccessful()) {
            WP_CLI::error("Failed creating branch on clone, message: " . $process->getErrorOutput());
        } else {
            WP_CLI::success("New Git branch created");
        }


        // 2) Update wp-config
        $wpConfigFile = ABSPATH . 'wp-config.php';
        $config = file_get_contents($wpConfigFile);

        // http://regex101.com/r/fS0zG2/1 - just remove the "g" modifier which is there for testing only
        $config = preg_replace("/^(define\\s*\\(\\s*['\"]DB_NAME['\"]\\s*\\,\\s*['\"])(.*)(['\"].*)$/m", "$1$dbName$3", $config, 1);

        file_put_contents($wpConfigFile, $config);
        WP_CLI::success("wp-config.php updated");


        // 3) Create empty db

        if (array_key_exists("force-db", $assoc_args)) {
            $dropDbCmd = 'wp db drop --yes';
            $process = new Process($dropDbCmd);
            $process->run();
            if ($process->isSuccessful()) {
                WP_CLI::success("Database dropped");
            } else {
                // Here, most probably the database could not be dropped because it didn't
                // exist. That's fine, we wanted to delete it anyway.
            }
        }


        $createDbCmd = 'wp db create';
        $process = new Process($createDbCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Failed creating database. If the problem is existing db, try running this with --force-db flag.");
            WP_CLI::error($process->getOutput()); // WPCLI uses STDOUT instead of STDERR
        } else {
            WP_CLI::success("Database created");
        }


        // 4) Create WP tables

        $createWpTablesCmd = 'wp core install --url=' . escapeshellarg($cloneUrl) . ' --title=x --admin_user=x --admin_password=x --admin_email=x@example.com';
        $process = new Process($createWpTablesCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log("Failed creating WP tables.");
            WP_CLI::error($process->getOutput());
        } else {
            WP_CLI::success("WP tables created");
        }

        // ... we need to truncate sample data (everything except `options`)


        // Remaining steps are done in `vp-internal finish-init-clone` where WP + VP is fully loaded

        $finishInitCloneCmd = 'wp --require=' . escapeshellarg(__FILE__) . ' vp-internal finish-init-clone';

        $process = new Process($finishInitCloneCmd);
        $process->run();
        if (!$process->isSuccessful()) {
            WP_CLI::log($process->getOutput());
            WP_CLI::error("Could not finish clone initialization");
        } else {
            WP_CLI::log($process->getOutput());
        }


    }

    /**
     * Finishes init-clone operation
     *
     * --truncate-options
     * : By default, options table is not truncated. This flag changes the behavior.
     *
     * @synopsis [--truncate-options]
     *
     * @subcommand finish-init-clone
     *
     */
    public function finishInitClone($args, $assoc_args) {


        // 5) Truncate tables

        /** @var wpdb $wpdb */
        global $wpdb;

        $sql = "SELECT concat('TRUNCATE TABLE `', TABLE_NAME, '`;') FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" . DB_NAME . "'";
        if ($assoc_args["truncate-options"]) {
            $sql .= ";";
        } else {
            $sql .= " AND TABLE_NAME NOT LIKE '%options';";
        }

        $results = $wpdb->get_results($sql, ARRAY_N);

        foreach ($results as $subResult) {
            $truncateCmd = $subResult[0];
            $wpdb->query($truncateCmd);
        }
        WP_CLI::success("Truncated DB tables");


        // 6) Create VersionPress tables

        global $versionPressContainer;
        /** @var \VersionPress\Initialization\Initializer $initializer */
        $initializer = $versionPressContainer->resolve(VersionPressServices::INITIALIZER);
        $initializer->createVersionPressTables();

        WP_CLI::success("VersionPress tables created");


        // 7) Run synchronization

        /** @var SynchronizationProcess $syncProcess */
        $syncProcess = $versionPressContainer->resolve(VersionPressServices::SYNCHRONIZATION_PROCESS);
        $syncProcess->synchronize();
        WP_CLI::success("Git -> db synchronization run");

    }

}

if (defined('WP_CLI') && WP_CLI) {
    WP_CLI::add_command('vp-internal', 'VersionPress\Cli\VPInternalCommand');
}