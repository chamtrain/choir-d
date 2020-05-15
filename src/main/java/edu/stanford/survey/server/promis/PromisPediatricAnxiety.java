package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Ped-Anxiety BEBC8474-D244-4A38-87DE-00A640DE03DE.json
 */
public class PromisPediatricAnxiety {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("2220R2", "In the past 7 days", "I felt like something awful might happen.", "", 1.71273, new double[] { -0.4315, 0.5121, 1.7544, 2.6483 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2230R1", "In the past 7 days", "I got scared really easy.", "", 1.48993, new double[] { 0.295, 1.1594, 2.0685, 2.74 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("227bR1", "In the past 7 days", "I felt scared.", "", 1.88971, new double[] { -0.2524, 0.5948, 1.7156, 2.5163 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("231R1", "In the past 7 days", "I worried about what could happen to me.", "", 1.84156, new double[] { -0.2383, 0.4826, 1.5372, 2.2129 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3021R1", "In the past 7 days", "I was worried I might die.", "", 1.71127, new double[] { 0.8595, 1.5376, 2.4354, 2.899 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3149R1", "In the past 7 days", "I woke up at night scared.", "", 1.64874, new double[] { 0.8871, 1.4252, 2.2817, 2.9425 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3150bR2", "In the past 7 days", "I worried when I went to bed at night.", "", 1.82871, new double[] { 0.2539, 0.914, 1.8276, 2.567 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3459aR1", "In the past 7 days", "I worried when I was away from home.", "", 1.32324, new double[] { 0.7683, 1.5029, 2.5932, 3.1603 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3459bR1", "In the past 7 days", "I worried when I was at home.", "", 1.64033, new double[] { 0.4015, 1.217, 2.6107, 3.2957 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3977R1", "In the past 7 days", "I was afraid of going to school.", "", 1.09274, new double[] { 1.2107, 2.0094, 3.0246, 3.958 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5044R1", "In the past 7 days", "I felt worried.", "", 1.8054, new double[] { -0.784, 0.2506, 1.5896, 2.6525 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("713R1", "In the past 7 days", "I felt nervous.", "", 1.50953, new double[] { -0.8533, 0.1793, 1.8578, 2.8535 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("953R1", "In the past 7 days", "It was hard for me to relax.", "", 1.41903, new double[] { -0.329, 0.6306, 1.8305, 2.707 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    )
  );

  public static ItemBank bank() {
    return bank;
  }
}