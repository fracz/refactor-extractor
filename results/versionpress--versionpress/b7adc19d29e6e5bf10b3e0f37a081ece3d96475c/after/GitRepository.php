<?php

namespace VersionPress\Git;

use Nette\Utils\Strings;
use Symfony\Component\Process\Process;
use VersionPress\Utils\FileSystem;

/**
 * Manipulates the Git repository.
 */
class GitRepository {

    private $workingDirectoryRoot;
    private $authorName = "";
    private $authorEmail = "";
    private $tempDirectory;
    private $commitMessagePrefix;

    /**
     * @param string $workingDirectoryRoot Filesystem path to working directory root (where the .git folder resides)
     * @param string $tempDirectory Directory used for commit message temp file
     * @param string $commitMessagePrefix Standard prefix applied to all commit messages
     */
    function __construct($workingDirectoryRoot, $tempDirectory = "./", $commitMessagePrefix = "[VP] ") {
        $this->workingDirectoryRoot = $workingDirectoryRoot;
        $this->tempDirectory = $tempDirectory;
        $this->commitMessagePrefix = $commitMessagePrefix;
    }

    /**
     * Stages all files under the given path. No path = stage all files in whole working directory.
     *
     * @param string|null $path Null (the default) means the whole working directory
     */
    public function stageAll($path = null) {
        $path = $path ? $path : $this->workingDirectoryRoot;
        $this->runShellCommand("git add -A %s", $path);
    }

    /**
     * Creates a commit
     *
     * @param CommitMessage $message
     * @param string $authorName
     * @param string $authorEmail
     */
    public function commit($message, $authorName, $authorEmail) {
        $this->authorName = $authorName;
        $this->authorEmail = $authorEmail;

        $subject = $message->getSubject();
        $body = $message->getBody();
        $commitMessage = $this->commitMessagePrefix . $subject;

        if ($body != null) $commitMessage .= "\n\n" . $body;

        $tempCommitMessageFilename = md5(rand());
        $tempCommitMessagePath = $this->tempDirectory . $tempCommitMessageFilename;
        file_put_contents($tempCommitMessagePath, $commitMessage);

        $this->runShellCommand("git config user.name %s && git config user.email %s", $this->authorName, $this->authorEmail);
        $this->runShellCommand("git commit -F %s", $tempCommitMessagePath);
        FileSystem::remove($tempCommitMessagePath);
    }

    /**
     * True if the working directory is versioned
     *
     * @return bool
     */
    public function isVersioned() {
        return $this->runShellCommandWithStandardOutput("git status -s") !== null;
    }

    /**
     * Initializes the repository
     */
    public function init() {
        $this->runShellCommand("git init");
    }

    /**
     * Gets last (most recent) commit hash in the repository, or an empty string is there are no commits.
     *
     * @return string Empty string or SHA1
     */
    public function getLastCommitHash() {
        $result = $this->runShellCommand("git rev-parse HEAD");
        if ($result["stderr"]) {
            return "";
        } else {
            return $result["stdout"];
        }
    }

    /**
     * Returns the initial (oldest) commit in the repo
     *
     * @return Commit
     */
    public function getInitialCommit() {
        $initialCommitHash = $this->runShellCommandWithStandardOutput("git rev-list --max-parents=0 HEAD");
        return $this->getCommit($initialCommitHash);
    }

    /**
     * Returns an array of Commits based on {@link http://git-scm.com/docs/gitrevisions gitrevisions}
     *
     * @param string $gitrevisions Empty by default, i.e., calling full 'git log'
     * @return Commit[]
     */
    public function log($gitrevisions = "") {

        $commitDelimiter = chr(29);
        $dataDelimiter = chr(30);

        $logCommand = "git log --pretty=format:\"%%H|delimiter|%%aD|delimiter|%%ar|delimiter|%%an|delimiter|%%ae|delimiter|%%s|delimiter|%%b|end|\"";
        if (!empty($gitrevisions)) {
            $logCommand .= " " . escapeshellarg($gitrevisions);
        }

        $logCommand = str_replace("|delimiter|", $dataDelimiter, $logCommand);
        $logCommand = str_replace("|end|", $commitDelimiter, $logCommand);
        $log = trim($this->runShellCommandWithStandardOutput($logCommand), $commitDelimiter);

        if ($log == "") {
            $commits = array();
        } else {
            $commits = explode($commitDelimiter, $log);
        }

        return array_map(function ($rawCommit) {
            return Commit::buildFromString(trim($rawCommit));
        }, $commits);

    }

    /**
     * Returns list of files that were modified in given {@link http://git-scm.com/docs/gitrevisions gitrevisions}
     *
     * @param string $gitrevisions
     * @return string[]
     */
    public function getModifiedFiles($gitrevisions) {
        $result = $this->runShellCommandWithStandardOutput("git diff --name-only %s", $gitrevisions);
        $files = explode("\n", $result);
        return $files;
    }

    /**
     * Like getModifiedFiles() but also returns the status of each file ("A" for added,
     * "M" for modified, "D" for deleted and "R" for renamed).
     *
     * @param string $gitrevisions See gitrevisions
     * @return array Array of things like `array("status" => "M", "path" => "wp-content/vpdb/something.ini" )`
     */
    public function getModifiedFilesWithStatus($gitrevisions) {
        $command = 'git diff --name-status %s';
        $output = $this->runShellCommandWithStandardOutput($command, $gitrevisions);
        $result = array();

        foreach (explode("\n", $output) as $line) {
            list($status, $path) = explode("\t", $line);
            $result[] = array("status" => $status, "path" => $path);
        }

        return $result;

    }

    /**
     * Reverts all changes up to a given commit - performs a "rollback"
     *
     * @param $commitHash
     */
    public function revertAll($commitHash) {
        $commitRange = sprintf("%s..HEAD", $commitHash);
        $this->runShellCommand("git revert -n %s", $commitRange);
    }

    /**
     * Reverts a single commit. If there is a conflict, aborts the revert and returns false.
     *
     * @param $commitHash
     * @return bool True if it succeeded, false if there was a conflict
     */
    public function revert($commitHash) {
        $output = $this->runShellCommandWithErrorOutput("git revert -n %s", $commitHash);

        if ($output !== null) { // revert conflict
            $this->abortRevert();
            return false;
        }

        return true;
    }

    /**
     * Aborts a revert, e.g., if there was a conflict
     */
    public function abortRevert() {
        $this->runShellCommand("git revert --abort");
    }

    /**
     * Returns true if $commitHash was created after the $afterWhichCommitHash commit ("after" meaning
     * that $commitHash is more recent commit, a child of $afterWhat). Same two commits return false.
     *
     * @param $commitHash
     * @param $afterWhichCommitHash
     * @return bool
     */
    public function wasCreatedAfter($commitHash, $afterWhichCommitHash) {
        $cmd = "git log $afterWhichCommitHash..$commitHash --oneline";
        return $this->runShellCommandWithStandardOutput($cmd) != null;
    }

    /**
     * Returns child (newer) commit. Assumes there is only a single child commit.
     *
     * @param $commitHash
     * @return mixed
     */
    public function getChildCommit($commitHash) {
        $cmd = "git log --reverse --ancestry-path --format=%%H $commitHash^..";
        $result = $this->runShellCommandWithStandardOutput($cmd);
        list($childHash) = explode("\n", $result);
        return $childHash;
    }

    /**
     * Counts number of commits
     *
     * @param string $startRevision Where to start. NULL means the initial commit (repo start)
     * @param string $endRevision Where to end. This will typically be HEAD.
     * @return int
     */
    public function getNumberOfCommits($startRevision = null, $endRevision = "HEAD") {
        $revRange = empty($startRevision) ? $endRevision : "$startRevision..$endRevision";
        return intval($this->runShellCommandWithStandardOutput("git rev-list %s --count", $revRange));
    }

    /**
     * Returns true if there is something to commit
     *
     * @return bool
     */
    public function willCommit() {
        $status = $this->runShellCommandWithStandardOutput("git status -s");
        return Strings::match($status, "~^[AMD].*~") !== null;
    }

    /**
     * Gets commit object based on its hash
     *
     * @param $commitHash
     * @return Commit
     */
    public function getCommit($commitHash) {
        $logWithInitialCommit = $this->log($commitHash);
        return $logWithInitialCommit[0];
    }

    /**
     * Returns git status in short format, something like:
     *
     *     A path1.txt
     *     M path2.txt
     *
     * Clean working directory returns an empty string.
     *
     * @return string
     */
    public function getStatus() {
        $gitCmd = "git status --porcelain";
        $output = $this->runShellCommandWithStandardOutput($gitCmd);
        return $output;
    }


    /**
     * Invokes {@see runShellCommand()} and returns its stdout output. The params are the same,
     * only the return type is string instead of an array.
     *
     * @see runShellCommand()
     * @see runShellCommandWithErrorOutput()
     *
     * @param string $command
     * @param string $args
     * @return string
     */
    private function runShellCommandWithStandardOutput($command, $args = '') {
        $result = call_user_func_array(array($this, 'runShellCommand'), func_get_args());
        return $result['stdout'];
    }

    /**
     * Invokes {@see runShellCommand()} and returns its stderr output. The params are the same,
     * only the return type is string instead of an array.
     *
     * @see runShellCommand()
     * @see runShellCommandWithStandardOutput()
     *
     * @param string $command
     * @param string $args
     * @return string
     */
    private function runShellCommandWithErrorOutput($command, $args = '') {
        $result = call_user_func_array(array($this, 'runShellCommand'), func_get_args());
        return $result['stderr'];
    }

    /**
     * Run a Git command, either fully specified (e.g., 'git log') or just by the name (e.g., 'log').
     * The comamnd can contain `sprintf()` markers, e.g., '%s', which are then replaced by shell-escaped
     * args provided as further parameters to this method.
     *
     * Note: shell-escaping is actually pretty important even for things that are not paths, like revisions.
     * For example, `git log HEAD^` will not work on Windows, `git log "HEAD^"` will. So the right
     * approach is to provide `git log %s` as the $command and rev range as $args.
     *
     * @param string $command E.g., 'git log' or 'git add %s' (path will be shell-escaped) or just 'log'
     *   (the "git " part is optional).
     * @param string $args Will be shell-escaped and replace sprintf markers in $command
     * @return array array('stdout' => , 'stderr' => )
     */
    private function runShellCommand($command, $args = '') {

        if (!Strings::startsWith($command, "git ")) {
            $command = "git " . $command;
        }

        $functionArgs = func_get_args();
        array_shift($functionArgs); // Remove $command
        $escapedArgs = @array_map("escapeshellarg", $functionArgs);
        $commandWithArguments = vsprintf($command, $escapedArgs);

        $result = $this->runProcess($commandWithArguments);
        return $result;
    }

    /**
     * Low-level helper, generally use runShellCommand()
     *
     * @param $cmd
     * @return array
     */
    private function runProcess($cmd) {
        /*
         * MAMP / XAMPP issue on Mac OS X,
         * see http://jira.agilio.cz/browse/WP-106.
         *
         * http://stackoverflow.com/a/16903162/1243495
         */
        $dyldLibraryPath = getenv("DYLD_LIBRARY_PATH");
        if ($dyldLibraryPath != "") {
            putenv("DYLD_LIBRARY_PATH=");
        }

        $process = new Process($cmd, $this->workingDirectoryRoot);
        $process->run();

        $result = array(
            'stdout' => $process->getOutput(),
            'stderr' => $process->getErrorOutput()
        );

        putenv("DYLD_LIBRARY_PATH=$dyldLibraryPath");

        if ($result['stdout'] !== null) $result['stdout'] = trim($result['stdout']);
        if ($result['stderr'] !== null) $result['stderr'] = trim($result['stderr']);

        return $result;
    }

}