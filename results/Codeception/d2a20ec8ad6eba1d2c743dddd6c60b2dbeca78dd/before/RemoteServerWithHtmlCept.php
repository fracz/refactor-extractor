<?php
$I = new CliGuy($scenario);
$I->wantTo('try generate remote codecoverage xml report');
$I->amInPath('tests/data/sandbox');
$I->executeCommand('run remote_server --coverage --html');
$I->seeFileFound('index.html','tests/_log/remote_server.remote.coverage');
