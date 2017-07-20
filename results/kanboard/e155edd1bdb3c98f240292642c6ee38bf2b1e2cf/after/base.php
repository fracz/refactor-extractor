<?php

namespace Model;

require __DIR__.'/../vendor/SimpleValidator/Validator.php';
require __DIR__.'/../vendor/SimpleValidator/Base.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/Required.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/Unique.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/MaxLength.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/MinLength.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/Integer.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/Equals.php';
require __DIR__.'/../vendor/SimpleValidator/Validators/AlphaNumeric.php';
require __DIR__.'/../vendor/PicoDb/Database.php';
require __DIR__.'/schema.php';

abstract class Base
{
    const APP_VERSION = 'master';
    const DB_VERSION  = 1;
    const DB_FILENAME = 'data/db.sqlite';

    private static $dbInstance = null;
    protected $db;

    public function __construct()
    {
        if (self::$dbInstance === null) {
            self::$dbInstance = $this->getDatabaseInstance();
        }

        $this->db = self::$dbInstance;
    }

    public function getDatabaseInstance()
    {
        $db = new \PicoDb\Database(array(
            'driver' => 'sqlite',
            'filename' => self::DB_FILENAME
        ));

        if ($db->schema()->check(self::DB_VERSION)) {
            return $db;
        }
        else {
            die('Unable to migrate database schema!');
        }
    }
}