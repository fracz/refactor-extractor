<?php
// This class was automatically generated by build task
// You can change it manually, but it will be overwritten on next build

use Codeception\Maybe;
use Codeception\Module\Filesystem;
use Codeception\Module\TestHelper;

class TestGuy extends \Codeception\AbstractGuy
{

   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::amInPath()
    */
    public function amInPath($path) {
        $this->scenario->condition('amInPath', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::openFile()
    */
    public function openFile($filename) {
        $this->scenario->action('openFile', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::deleteFile()
    */
    public function deleteFile($filename) {
        $this->scenario->action('deleteFile', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::deleteDir()
    */
    public function deleteDir($dirname) {
        $this->scenario->action('deleteDir', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::copyDir()
    */
    public function copyDir($src, $dst) {
        $this->scenario->action('copyDir', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::seeInThisFile()
    */
    public function seeInThisFile($text) {
        $this->scenario->assertion('seeInThisFile', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::dontSeeInThisFile()
    */
    public function dontSeeInThisFile($text) {
        $this->scenario->action('dontSeeInThisFile', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::deleteThisFile()
    */
    public function deleteThisFile() {
        $this->scenario->action('deleteThisFile', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }


   /**
    * This method is generated. DO NOT EDIT.
    *
    * @see Filesystem::seeFileFound()
    */
    public function seeFileFound($filename, $path = null) {
        $this->scenario->assertion('seeFileFound', func_get_args());
        if ($this->scenario->running()) {
            $result = $this->scenario->runStep();
            return new Maybe($result);
        }
        return new Maybe();
    }
}
