<?php
require 'vendor/autoload.php';

$maxLength = 2000;
$astDir = __DIR__ . '/input/ast/';

$asts = ['added', 'changed-before', 'changed-after', 'deleted'];

foreach ($asts as $ast) {
    $input = $astDir . $ast . '.txt';

    $rows = explode(PHP_EOL, file_get_contents($input));
    $notTooLongRows = array_filter($rows, function($row) use ($maxLength) {
        $tokenCount = substr_count($row, ',') + 1;
        return $tokenCount <= $maxLength;
    });

    shuffle($notTooLongRows);

    file_put_contents($astDir . $ast . '-max-' . $maxLength . '.txt', implode(PHP_EOL, $notTooLongRows));

    unset($rows);
    unset($notTooLongRows);
}
