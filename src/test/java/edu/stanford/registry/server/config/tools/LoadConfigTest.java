package edu.stanford.registry.server.config.tools;

import org.junit.Assert;
import org.junit.Test;

public class LoadConfigTest {

  @Test
  public void testCommandsFireWithMinimumInput() {
    ensureEveryCommandReachable(0);
  }

  @Test
  public void testCommandsFireWithMoreThanMinimumInput() {
    ensureEveryCommandReachable(2);
  }

  @Test
  public void testCommandsFireWithMaximumInput() {
    ensureEveryCommandReachable(20);
  }

  String getSubstring(String word, int minLen, int more) {
    int len = minLen + more;
    word = (word.length() <= len) ? word : word.substring(0, len);
    return word.toLowerCase();
  }

  final String bogusString = "XXX";

  void firstMatchingCmdShouldBe(LoadConfig.Command[] commands, String input, LoadConfig.Command theOneSought) {
    boolean shouldBeFound = !theOneSought.cmd.equals(bogusString);
    LoadConfig.Command theOneFound = null;
    for (LoadConfig.Command hit: commands) {
      if (!hit.matches(input))
        continue;
      theOneFound = hit;
      break;
    }
    String which = "Case "+input;
    if (shouldBeFound)
      Assert.assertEquals(which+" should ==", theOneSought, theOneFound);
    else
      Assert.assertNotEquals(which+" should !=", theOneSought, theOneFound);
  }

  public void ensureEveryCommandReachable(int more) {
    LoadConfig lc = new LoadConfig("nobody");
    LoadConfig.params = new LoadConfig.Params(null, new String[0]);
    lc.setup(null);

    LoadConfig.Command bogus = new LoadConfig.Command("XXX", "", "") {
      @Override public void run(String arg) { }
    };
    LoadConfig.Command theOneSought = null;
    LoadConfig.Command commands[] = lc.createCommands();
    for (int i = 0;  i < commands.length+1;  i++) {
      theOneSought = (i < commands.length) ? commands[i] : bogus;
      if (theOneSought instanceof LoadConfig.LineSepCmd)
        continue;

      String word = theOneSought.cmd;
      word = getSubstring(word, theOneSought.getMinLength(word), more);
      firstMatchingCmdShouldBe(commands, word, theOneSought);

      word = theOneSought.cmd.replaceAll("-", "");
      word = getSubstring(word, theOneSought.getMinLength(word), more);
      firstMatchingCmdShouldBe(commands, word, theOneSought);
    }
  }
}
