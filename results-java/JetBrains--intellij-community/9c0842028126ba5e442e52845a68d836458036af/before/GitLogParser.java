/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package git4idea.history;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import git4idea.GitVcs;
import git4idea.config.GitVersionSpecialty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * <p>Parses the 'git log' output basing on the given number of options.
 * Doesn't execute of prepare the command itself, performs only parsing.</p>
 *
 * <p>
 * Usage:
 * 1. Pass options you want to have in the output to the constructor using the {@link GitLogOption} enum constants.
 * 2. Get the custom format pattern for 'git log' by calling {@link #getPretty()}
 * 3. Call the command and retrieve the output.
 * 4. Parse the output via {@link #parse(String)} or {@link #parseOneRecord(String)} (if you want the output to be parsed line by line).</p>
 *
 * <p>The class is package visible, since it's used only in GitHistoryUtils - the class which retrieve various pieced of history information
 * in different formats from 'git log'</p>
 *
 * <p>Note that you may pass one set of options to the GitLogParser constructor and then execute git log with other set of options.
 * In that case {@link #parse(String)} will parse only those options which you've specified in the constructor.
 * Others will be ignored since the parser knows nothing about them: it just gets the 'git log' output to parse.
 * Moreover you really <b>must</b> use {@link #getPretty()} to pass "--pretty=format" pattern to 'git log' - otherwise the parser won't be able
 * to parse output of 'git log' (because special separator characters are used for that).</p>
 *
 * <p>If you use '--name-status' or '--name-only' flags in 'git log' you also <b>must</b> call {@link #parseStatusBeforeName(boolean)} with
 * true or false respectively, because it also affects the output.</p>
 *
 * @see git4idea.history.GitLogRecord
 */
class GitLogParser {
  // Single records begin with %x01, end with %03. Items of commit information (hash, committer, subject, etc.) are separated by %x02.
  // each character is declared twice - for Git pattern format and for actual character in the output.
  // separators are declared as String instead of char, because String#split() is heavily used in parsing.
  public static final String RECORD_START = "\u0001";
  public static final String RECORD_START_GIT = "%x01";
  public static final String ITEMS_SEPARATOR = "\u0002";
  private static final String ITEMS_SEPARATOR_GIT = "%x02";
  public static final String RECORD_END = "\u0003";
  private static final String RECORD_END_GIT = "%x03";

  private final String myFormat;  // pretty custom format generated in the constructor
  private final GitLogOption[] myOptions;
  private final boolean mySupportsRawBody;
  private final NameStatus myNameStatusOption;

  // --name-only, --name-status or no flag
  enum NameStatus {
    /** No flag. */
    NONE,
    /** --name-only */
    NAME,
    /** --name-status */
    STATUS
  }

  /**
   * Options which may be passed to 'git log --pretty=format:' as placeholders and then parsed from the result.
   * These are the pieces of information about a commit which we want to get from 'git log'.
   */
  enum GitLogOption {
    SHORT_HASH("h"), HASH("H"), COMMIT_TIME("ct"), AUTHOR_NAME("an"), AUTHOR_TIME("at"), AUTHOR_EMAIL("ae"), COMMITTER_NAME("cn"),
    COMMITTER_EMAIL("ce"), SUBJECT("s"), BODY("b"), SHORT_PARENTS("p"), PARENTS("P"), REF_NAMES("d"), SHORT_REF_LOG_SELECTOR("gd"),
    RAW_BODY("B");

    private String myPlaceholder;
    private GitLogOption(String placeholder) { myPlaceholder = placeholder; }
    private String getPlaceholder() { return myPlaceholder; }
  }

  /**
   * Constructs new parser with the given options and no names of changed files in the output.
   */
  GitLogParser(Project project, GitLogOption... options) {
    this(project, NameStatus.NONE, options);
  }

  /**
   * Constructs new parser with the specified options.
   * Only these options will be parsed out and thus will be available from the GitLogRecord.
   */
  GitLogParser(Project project, NameStatus nameStatusOption, GitLogOption... options) {
    myFormat = makeFormatFromOptions(options);
    myOptions = options;
    myNameStatusOption = nameStatusOption;
    mySupportsRawBody = GitVersionSpecialty.STARTED_USING_RAW_BODY_IN_FORMAT.existsIn(GitVcs.getInstance(project).getVersion());
  }

  private static String makeFormatFromOptions(GitLogOption[] options) {
    Function<GitLogOption,String> function = new Function<GitLogOption, String>() {
      @Override public String fun(GitLogOption option) {
        return "%" + option.getPlaceholder();
      }
    };
    return RECORD_START_GIT + StringUtil.join(options, function, ITEMS_SEPARATOR_GIT) + RECORD_END_GIT;
  }

  String getPretty() {
    return "--pretty=format:" + myFormat;
  }

  /**
   * Parses the output returned from 'git log' which was executed with '--pretty=format:' pattern retrieved from {@link #getPretty()}.
   * @param output 'git log' output to be parsed.
   * @return The list of {@link GitLogRecord GitLogRecords} with information for each revision.
   *         The list is sorted as usual for git log - the first is the newest, the last is the oldest.
   */
  @NotNull
  List<GitLogRecord> parse(@NotNull String output) {
    // Here is what git log returns for --pretty=tformat:^%H#%s$
    // ^2c815939f45fbcfda9583f84b14fe9d393ada790#sample commit$
    //
    // D       a.txt
    // ^b71477e9738168aa67a8d41c414f284255f81e8a#moved out$
    //
    // R100    dir/anew.txt    anew.txt
    final String[] records = output.split(RECORD_START); // split by START, because END is the end of information, but not the end of the record: file status and path follow.
    final List<GitLogRecord> res = new ArrayList<GitLogRecord>(records.length);
    for (String record : records) {
      if (!record.trim().isEmpty()) {  // record[0] is empty for sure, because we're splitting on RECORD_START. Just to play safe adding the check for all records.
        res.add(parseOneRecord(record));
      }
    }
    return res;
  }

  /**
   * Parses a single record returned by 'git log'. The record contains information from pattern and file status and path (if respective
   * flags --name-only or name-status were provided).
   * @param line record to be parsed.
   * @return GitLogRecord with information about the revision or {@code null} if the given line is empty.
   */
  @Nullable
  GitLogRecord parseOneRecord(@NotNull String line) {
    // record format:
    //
    // <record start> - this may be splitted out in parse(). But if one calls this method directly, the char will be in place.
    // <commit info, possibly multilined - if body is multilined>
    // <record end mark>
    // <blank line (optional)>
    // <name status (optional)>\t<path (optional)>\t<second path (optional)> - status is output in the case of NameStatus.STATUS,
    // paths are output if NameStatus.NAME or NameStatus.STATUS. Two paths is the rename case.
    //
    // Blank line before name-status usually appears, but it also can absent (e.g. in --pretty=oneline format), so we shouldn't rely on this.
    // Example:
    // 2c815939f45fbcfda9583f84b14fe9d393ada790<ITEM_SEPARATOR>sample commit<RECORD_END>
    //
    // D       a.txt

    if (line.isEmpty()) { return null; }
    line = removeRecordStartIndicator(line);


    // parsing status and path (if given)
    final List<String> paths = new ArrayList<String>(1);
    final boolean includeStatus = myNameStatusOption == NameStatus.STATUS;
    final List<List<String>> parts = includeStatus ? new ArrayList<List<String>>() : null;

    if (myNameStatusOption != NameStatus.NONE) {
      final String[] infoAndPath = line.split(RECORD_END);
      line = infoAndPath[0];
      if (infoAndPath.length > 1) {
        // separator is \n for paths, space for paths and status
        final List<String> nameAndPathSplit = new ArrayList<String>(Arrays.asList(infoAndPath[infoAndPath.length - 1].split("\n")));
        for (Iterator<String> it = nameAndPathSplit.iterator(); it.hasNext();) {
          if (it.next().trim().isEmpty()) {
            it.remove();
          }
        }

        for (String pathLine : nameAndPathSplit) {
          String[] partsArr;
          if (includeStatus) {
            final int idx = pathLine.indexOf("\t");
            if (idx != -1) {
              final String whatLeft = pathLine.substring(idx).trim();
              partsArr = whatLeft.split("\\t");
              final List<String> strings = new ArrayList<String>(partsArr.length + 1);
              strings.add(pathLine.substring(0, 1));
              strings.addAll(Arrays.asList(partsArr));
              parts.add(strings);
            } else {
              partsArr = pathLine.split("\\t"); // should not
            }
          } else {
            partsArr = pathLine.split("\\t");
          }
          paths.addAll(Arrays.asList(partsArr));
        }
      }
    } else {
      line = line.substring(0, line.length()-1); // removing the last character which is RECORD_END
    }

    // parsing revision information
    // we rely on the order of options
    final String[] values = line.split(ITEMS_SEPARATOR);
    final Map<GitLogOption, String> res = new HashMap<GitLogOption, String>(values.length);
    int i = 0;
    for (; i < values.length && i < myOptions.length; i++) {  // fill valid values
      res.put(myOptions[i], values[i]);
    }
    for (; i < myOptions.length; i++) {  // options which were not returned are set to blank string, extra options are ignored.
      res.put(myOptions[i], "");
    }
    return new GitLogRecord(res, paths, parts, mySupportsRawBody);
  }

  @NotNull
  private static String removeRecordStartIndicator(@NotNull String line) {
    // We may have <RECORD_START> indicator at the beginning of the line (if we called parseOneLine directly), may not (if we called parse()).
    // If we have, get rid of it.
    if (line.charAt(0) == RECORD_START.charAt(0)) {
      line = line.substring(RECORD_START.length());
    }
    return line;
  }
}