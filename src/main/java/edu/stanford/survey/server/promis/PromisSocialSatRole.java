package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Social Sat Role 36F00430-CC4C-4977-AE8A-0787B3C53AB8.json
 */

public class PromisSocialSatRole {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0, 
  item("SRPSAT06", "In the past 7 days", "I am satisfied with my ability to do things for my family", "", 3.445, new double[] { -1.579, -0.955, -0.305, 0.636 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT07", "In the past 7 days", "I am satisfied with how much work I can do (include work at home)", "", 4.424, new double[] { -1.338, -0.799, -0.131, 0.675 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT08", "In the past 7 days", "I feel good about my ability to do things for my family", "", 3.152, new double[] { -1.65, -1.083, -0.315, 0.544 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT09", "In the past 7 days", "I am satisfied with my ability to do the work that is really important to me (include work at home)", "", 3.607, new double[] { -1.582, -0.941, -0.228, 0.562 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT21", "In the past 7 days", "I am satisfied with the amount of time I spend doing work (include work at home)", "", 3.329, new double[] { -1.492, -0.85, -0.036, 0.779 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT22", "In the past 7 days", "I am happy with how much I do for my family", "", 3.009, new double[] { -1.581, -0.967, -0.198, 0.707 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT24", "In the past 7 days", "I am satisfied with my ability to work (include work at home)", "", 4.688, new double[] { -1.433, -0.936, -0.288, 0.519 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT35", "In the past 7 days", "The quality of my work is as good as I want it to be (include work at home)", "", 3.059, new double[] { -1.486, -0.895, -0.187, 0.785 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT38", "In the past 7 days", "I am satisfied with the amount of time I spend performing my daily routines", "", 3.731, new double[] { -1.512, -0.848, 0.0080, 0.766 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT39", "In the past 7 days", "I am satisfied with my ability to do household chores/tasks", "", 4.08, new double[] { -1.429, -0.831, -0.145, 0.617 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT47", "In the past 7 days", "I am satisfied with my ability to do regular personal and household responsibilities", "", 4.377, new double[] { -1.421, -0.895, -0.215, 0.57 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT49", "In the past 7 days", "I am satisfied with my ability to perform my daily routines", "", 5.577, new double[] { -1.564, -0.92, -0.218, 0.458 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT50", "In the past 7 days", "I am satisfied with my ability to meet the needs of those who depend on me", "", 3.679, new double[] { -1.568, -1.009, -0.361, 0.552 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  ),
  item("SRPSAT51", "In the past 7 days", "I am satisfied with my ability to run errands", "", 3.274, new double[] { -1.536, -0.98, -0.377, 0.521 }, -1, "", 
    response("Not at all", 1),
    response("A little bit", 2),
    response("Somewhat", 3),
    response("Quite a bit", 4),
    response("Very much", 5)
  )
);

  public static ItemBank bank() {
    return bank;
  }
}