<?php
require 'vendor/autoload.php';

const INDENT_OUTPUT = true;
const DIFF_SEPARATOR = '||||||||';

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
        $filesBefore = array_diff(scandir($changeDir . '/before'), ['.', '..']);
        @mkdir($changeDir . '/diffs');
        foreach ($filesBefore as $file) {
            if (file_exists($changeDir . "/after/$file")) {
                $methods = ['before' => [], 'after' => [], 'beforeLines' => [], 'afterLines' => []];
                try {
                    $treeBefore = ast_dump(ast\parse_file($changeDir . "/before/$file", $version = 50), 0, $methods['before'], $methods['beforeLines']);
                    $treeAfter = ast_dump(ast\parse_file($changeDir . "/after/$file", $version = 50), 0, $methods['after'], $methods['afterLines']);
                } catch (Error $e) {
                    continue;
                }
                if (count($methods['before']) || count($methods['after'])) {
                    $deletedMethods = array_diff_key($methods['before'], $methods['after']);
                    $addedMethods = array_diff_key($methods['after'], $methods['before']);
                    $changedMethods = array_diff_assoc($methods['before'], $methods['after']);
                    $changedMethods = array_diff_key($changedMethods, $deletedMethods);
                    $changedMethods = array_diff_key($changedMethods, $addedMethods);
                    foreach ($changedMethods as $changedMethodName => $changedMethodAst) {
                        $diff = implode(DIFF_SEPARATOR, [
                            implode('', array_slice(file($changeDir . "/before/$file"), $methods['beforeLines'][$changedMethodName][0] - 1, $methods['beforeLines'][$changedMethodName][1] - $methods['beforeLines'][$changedMethodName][0])),
                            implode('', array_slice(file($changeDir . "/after/$file"), $methods['afterLines'][$changedMethodName][0] - 1, $methods['afterLines'][$changedMethodName][1] - $methods['afterLines'][$changedMethodName][0])),
                            $methods['before'][$changedMethodName],
                            $methods['after'][$changedMethodName],
                        ]);
                        file_put_contents($changeDir . '/diffs/' . $file . $changedMethodName . '.txt', $diff);
                    }
                }
            }
//                file_put_contents($changeDir . "/$part-ast/$file.txt", $tree);
        }
    }
    $progress->advance();
}


////////////////////////// util.php


/** Dumps abstract syntax tree */
function ast_dump($ast, int $options = 0, &$methods = [], &$methodLines = [], $nextChildLine = 1): string
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
            $methodStartsAtLine = $ast->lineno;
            if (is_array($ast->children['stmts']) && is_object(end($ast->children['stmts']->children))) {
                $methodEndsAtLine = end($ast->children['stmts']->children)->lineno + 1;
            } else {
                $methodEndsAtLine = $methodStartsAtLine + 2; // empty method
            }
            $methodName = $ast->children['name'];
            $hasDocComment = isset($ast->children['docComment']) && $ast->children['docComment'];
            $result = '';
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
        } else if (in_array($result, ['AST_STMT_LIST', 'AST_NAME', 'AST_ARG_LIST'])) {
            $result = '';
        }

        unset($ast->children['name']);
        unset($ast->children['method']);
        unset($ast->children['docComment']);
        unset($ast->children['returnType']);
        unset($ast->children['prop']);
        unset($ast->children['__declId']);

        foreach ($ast->children as $i => $child) {
            $nextMyChildLine = PHP_INT_MAX;
            if (is_int($i) && isset($ast->children[$i + 1]) && is_object($ast->children[$i + 1])) {
                $nextMyChildLine = $ast->children[$i + 1]->lineno;
            }
            $childResult = ast_dump($child, $options, $methods, $methodLines, $nextMyChildLine);
            if (trim($childResult)) {
                if (INDENT_OUTPUT) {
                    $result .= "\n";
                }
                $result .= "(" . str_replace("\n", "\n    ", $childResult) . ')';
            }
        }


        if ($isMethod) {
            $methods[$methodName] = $result;
            $methodLines[$methodName] = [$methodStartsAtLine, max($methodEndsAtLine, $nextChildLine)];
        }

        return $result;
    } else if ($ast === null) {
        return 'NULL';
    } else {
        return 'SCALAR';
    }
}
