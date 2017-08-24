<?php
require 'vendor/autoload.php';

const INDENT_OUTPUT = false;

$projectsDir = __DIR__ . '/results/';
$projects = array_diff(scandir($projectsDir), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($projects));

$changesCount = 0;

//$tree = ast_dump(ast\parse_file('Sample.php', $version = 50), 0, $methods);
//exit;

foreach ($projects as $project) {
    $projectDir = $projectsDir . $project;
    if (!is_dir($projectDir)) {
        $progress->advance();
        continue;
    }
    $changes = array_diff(scandir($projectDir), ['.', '..', 'README.txt']);
    foreach ($changes as $commitId) {
        $changeDir = $projectDir . '/' . $commitId;
        $methods = ['before' => [], 'after' => []];
        foreach (['before', 'after'] as $part) {
            $files = array_diff(scandir($changeDir . '/' . $part), ['.', '..']);
            @mkdir($changeDir . '/' . $part . '-ast');
            foreach ($files as $file) {
                $tree = ast_dump(ast\parse_file($changeDir . "/$part/$file", $version = 50), 0, $methods[$part]);
                echo $tree;
                file_put_contents($changeDir . "/$part-ast/$file.txt", $tree);
            }
        }
        if (count($methods['before']) || count($methods['after'])) {
            $changedMethods = array_diff_assoc($methods['before'], $methods['after']);
            $deletedMethods = array_diff_key($methods['before'], $methods['after']);
            $addedMethods = array_diff_key($methods['after'], $methods['before']);
            var_dump($methods);
        }
    }
    $progress->advance();
}


////////////////////////// util.php


/** Dumps abstract syntax tree */
function ast_dump($ast, int $options = 0, &$methods = []): string
{
    if ($ast instanceof ast\Node) {
        $result = ast\get_kind_name($ast->kind);

//        if ($options & AST_DUMP_LINENOS) {
//            $result .= " @ $ast->lineno";
//            if (isset($ast->endLineno)) {
//                $result .= "-$ast->endLineno";
//            }
//        }

//        if (ast\kind_uses_flags($ast->kind) || $ast->flags != 0) {
//            $result .= "\n    flags: " . format_flags($ast->kind, $ast->flags);
//        }
//        if (isset($ast->name)) {
//            $result .= "\n    name: $ast->name";
//        }
//        if (isset($ast->docComment)) {
//            $result .= "\n    comment: yes";
//        }
        $isMethod = false;
        if ($result == 'AST_METHOD') {
            $isMethod = true;
            $methodName = $ast->children['name'];
            $hasDocComment = isset($ast->children['docComment']) && $ast->children['docComment'];
            $result .= $hasDocComment ? 'HAS_DOC_COMMENT' : 'NO_DOC_COMMENT';
            $declaresReturnType = !!$ast->children['returnType'];
            $result .= $declaresReturnType ? 'HAS_RETURN_TYPE' : 'NO_RETURN_TYPE';
            unset($ast->children['uses']);
        } else if ($result == 'AST_PARAM_LIST') {
            $paramDelimiter = '';
            return implode($paramDelimiter, array_map(function (ast\Node $param) {
                    return implode('', [
                        $param->children['type'] ? 'PARAM_TYPE' : 'NO_PARAM_TYPE',
                        $param->children['default'] ? 'PARAM_DEFAULT' : 'NO_PARAM_DEFAULT',
                    ]);
                }, $ast->children)) . (count($ast->children) ? $paramDelimiter : '');
        } else if ($result == 'AST_STMT_LIST') {
            $result = '';
        }

        unset($ast->children['name']);
        unset($ast->children['method']);
        unset($ast->children['docComment']);
        unset($ast->children['returnType']);
        unset($ast->children['prop']);
        unset($ast->children['__declId']);

        foreach ($ast->children as $i => $child) {
            if (INDENT_OUTPUT) {
                $result .= "\n";
            }
            $result .= "(" . str_replace("\n", "\n    ", ast_dump($child, $options, $methods)) . ')';
        }

        if ($isMethod) {
            $methods[$methodName] = $result;
        }

        return $result;
    } else if ($ast === null) {
        return 'NULL';
    } else {
        return 'SCALAR';
    }
}
