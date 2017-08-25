<?php namespace McCool\DatabaseBackup\Procedures;

class BackupProcedure extends Procedure
{
    public function run($databaseName, $storageName, $destinationPath)
    {
        $localFilename = __DIR__.'/../../working/test.sql';

        $this->add($this->commandFactory->makeDumpDatabaseCommand($databaseName, $localFilename));
        $command->execute();
        die();
        $this->add($this->commandFactory->makeSaveFileCommand($storageName, $localFilename, $destinationPath));
        $this->execute();
    }
}