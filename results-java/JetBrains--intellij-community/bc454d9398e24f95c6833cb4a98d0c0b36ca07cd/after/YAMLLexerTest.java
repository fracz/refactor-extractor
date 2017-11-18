package org.jetbrains.yaml.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public class YAMLLexerTest extends LexerTestCase {
  @Override
  protected Lexer createLexer() {
    return new YAMLFlexLexer();
  }

  @Override
  protected String getDirPath() {
    return "plugins/yaml/testSrc/org/jetbrains/yaml/lexer/data";
  }

  public void test2docs() {
    doTest("# Ranking of 1998 home runs\n" +
           "---\n" +
           "- Mark McGwire\n" +
           "- Sammy Sosa\n" +
           "- Ken Griffey\n" +
           "\n" +
           "# Team ranking\n" +
           "---\n" +
           "- Chicago Cubs\n" +
           "- St Louis Cardinals");
  }

  public void testColorspage(){
    doTest("# Read about fixtures at http://ar.rubyonrails.org/classes/Fixtures.html\n" +
           "static_sidebar:\n" +
           "  id: \"foo\"\n" +
           "  name: 'side_bar'\n" +
           "  staged_position: 1\n" +
           "  blog_id: 1\n" +
           "  config: |+\n" +
           "    --- !map:HashWithIndifferentAccess\n" +
           "      title: Static Sidebar\n" +
           "      body: The body of a static sidebar\n" +
           "  type: StaticSidebar\n" +
           "  type: > some_type_here");

  }

  public void testDocuments(){
    doTest("---\n" +
           "RubyMine Develoers:\n" +
           "    name: yole\n" +
           "    name: romeo\n" +
           "    name: den\n" +
           "    name: oleg\n" +
           "---\n" +
           "RubyMine Versions:\n" +
           "    - 1.0\n" +
           "    - 1.1\n" +
           "    - 1.1.1\n" +
           "    - 1.5 EAP\n" +
           "    - 1.5");
  }

  public void testIndentation(){
    doTest("name: Mark McGwire\n" +
           "accomplishment: >\n" +
           "  Mark set a major league\n" +
           "  home run record in 1998.\n" +
           "stats: |\n" +
           "  65 Home Runs\n" +
           "  0.278 Batting Average");
  }


  public void testMap_between_seq(){
    doTest("? - Detroit Tigers\n" +
           "  - Chicago cubs\n" +
           ":\n" +
           "  - 2001-07-23\n" +
           "\n" +
           "? [ New York Yankees,\n" +
           "    Atlanta Braves ]\n" +
           ": [ 2001-07-02, 2001-08-12,\n" +
           "    2001-08-14 ]");
  }

  public void testMap_map(){
    doTest("Mark McGwire: {hr: 65, avg: 0.278}\n" +
           "Sammy Sosa: {\n" +
           "    hr: 63,\n" +
           "    avg: 0.288\n" +
           "  }");
  }

  public void testQuoted_scalars(){
    doTest("unicode: \"Sosa did fine.\\u263A\"\n" +
           "control: \"\\b1998\\t1999\\t2000\\n\"\n" +
           "hex esc: \"\\x0d\\x0a is \\r\\n\"\n" +
           "\n" +
           "single: '\"Howdy!\" he cried.'\n" +
           "quoted: ' # not a ''comment''.'\n" +
           "tie-fighter: '|\\-*-/|'");
  }

  public void testSample_log(){
    doTest("Date: 2001-11-23\n" +
           "Stack:\n" +
           "  - file: TopClass.py\n" +
           "    line: 23\n" +
           "    code: |\n" +
           "      x = MoreObject(\"345\\n\")\n" +
           "  - file: MoreClass.py\n" +
           "    line: 58\n" +
           "    code: |-\n" +
           "      foo = bar");
  }

  public void testSeq_seq(){
    doTest("- [name        , hr, avg  ]\n" +
           "- [Mark McGwire, 65, 0.278]\n" +
           "- [Sammy Sosa  , 63, 0.288]");
  }

  public void testSequence_mappings(){
    doTest("-\n" +
           "  name: Mark McGwire\n" +
           "  hr:   65\n" +
           "  avg:  0.278\n" +
           "-\n" +
           "  name: Sammy Sosa\n" +
           "  hr:   63\n" +
           "  avg:  0.288");
  }

  public void testWrong_string_highlighting(){
    doTest("status:\n" +
           "    draft: \"Brouillon\"  \n" +
           "    reviewed: \"Revise\"\n" +
           "    hidden: \"Cache\"\n" +
           "    published: \"Public\"");
  }

  public void testValue_injection(){
    doTest("key:\n" + "    one: 1 text\n" + "    other: {{count}} text");
  }

  public void testValue_injection_2(){
    doTest("key:\n" + "    one: 1 text\n" + "    other: some {{count}} text");
  }

  public void testComma(){
    doTest("key: Hello, how are you?");
  }

  public void testIndex(){
    doTest("indexes:\n" +
           "\n" +
           "- kind: MessageIndex\n" +
           "  properties:\n" +
           "  - name: group_name\n" +
           "  - name: created_at\n" +
           "    direction: desc");
  }

  public void testKeydots(){
    doTest("my.testevent.pre:\n" +
           "  type:     sfEvent");

  }

  public void testColons74100(){
    doTest("server1: http://localhost:9876\n" +
           "server2:\n" +
           "  http://localhost:8765\n" +
           "server3: test");

  }

  public void testOnlyyamlkey(){
    doTest("foo:");
  }

  public void testKey_parens(){
    doTest("comment(c):");
  }

  public void testKey_trailing_space(){
    doTest("foo   :");
  }

  public void testComments(){
    doTest("en:\n" +
           "  aaa: \"AAA\"\n" +
           "#  bbb:\n" +
           "#    ccc: \"CCC\"\n" +
           "installation:\n" +
           "  name: [] # todo: \"Not specified\"\n" +
           "en #comment: TODO");
  }

  public void testNon_comment() {
    doTest("hello: hello#my_non_comment");
  }

  public void testNon_comment2() {
    doTest("foo#non_comment: text");
  }

  public void testKey_with_brackets() {
    doTest("foo[in_brackets]: text");
  }
}