<?php
require 'vendor/autoload.php';

$astDir = __DIR__ . '/input/ast/';

$asts = ['added', 'changed-before', 'changed-after', 'deleted'];

foreach ($asts as $ast) {
    $input = $astDir . $ast . '.txt';

    $rows = explode(PHP_EOL, file_get_contents($input));

    $histogram = [];

    foreach ($rows as $row) {
        $tokenCount = substr_count($row, ',') + 1;
        if (!isset($histogram[$tokenCount])) {
            $histogram[$tokenCount] = 1;
        } else {
            ++$histogram[$tokenCount];
        }
    }

    ksort($histogram);

    $results = '';

    foreach ($histogram as $tokenCount => $freq) {
        $results .= $tokenCount . "\t" . $freq . PHP_EOL;
    }

    file_put_contents($astDir . $ast . '-histogram.txt', $results);

    unset($rows);
    unset($histogram);
    unset($results);
}
