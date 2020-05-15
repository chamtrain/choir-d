package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import static edu.stanford.survey.server.ItemBanks.*;
/**
 * Item bank for PROMIS generated from: Alcohol Negative Consequences A3A571A6-1E62-4768-B8C1-AB99CDBF867F.json
 */

public class PromisAlcoholNegativeConsequences {

  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0, 
  item("AlcoholScreener", "In the past 30 days", "Did you drink any type of alcoholic beverage?", "", 0.0, new double[] { 0.0 }, -1, "", 
    response("Yes", 1),
    response("No", 1)
  ),
  item("NECO01", "In the past 30 days", "I worried when I drank.", "", 2.89772, new double[] { -0.1027, 0.4783, 1.1047, 1.6926 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO02", "In the past 30 days", "I felt angry when I drank.", "", 3.88528, new double[] { -0.0717, 0.4207, 1.0341, 1.4301 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO03", "In the past 30 days", "I felt nervous when I drank.", "", 2.69854, new double[] { 0.1729, 0.7125, 1.4106, 1.8418 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO04", "In the past 30 days", "My problems seemed worse when I drank.", "", 3.48261, new double[] { -0.079, 0.411, 0.9105, 1.3376 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO05", "In the past 30 days", "I had trouble keeping appointments after I drank.", "", 4.75344, new double[] { 0.1298, 0.4683, 0.8224, 1.2348 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO06", "In the past 30 days", "I got confused when I drank.", "", 3.63067, new double[] { -0.0874, 0.4368, 1.0388, 1.5107 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO07", "In the past 30 days", "I took risks when I drank.", "", 4.00087, new double[] { -0.2338, 0.1782, 0.7247, 1.4057 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO08", "In the past 30 days", "I was critical of myself when I drank.", "", 3.19886, new double[] { -0.2352, 0.2959, 0.8933, 1.4345 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO09", "In the past 30 days", "I felt guilty when I drank.", "", 3.51449, new double[] { -0.1062, 0.3243, 0.8662, 1.2489 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO10", "In the past 30 days", "I had a headache after I drank.", "", 2.15394, new double[] { -0.4971, 0.2036, 1.0526, 1.6811 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO11", "In the past 30 days", "I got sick when I drank.", "", 2.56516, new double[] { 0.0701, 0.6691, 1.2168, 1.8625 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO12", "In the past 30 days", "I felt anxious when I drank.", "", 3.14642, new double[] { 0.0102, 0.5552, 1.1791, 1.6429 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO13", "In the past 30 days", "I was clumsy when I drank.", "", 3.21694, new double[] { -0.4716, 0.1413, 0.8783, 1.4826 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO14", "In the past 30 days", "I was unreliable after I drank.", "", 4.80168, new double[] { -0.097, 0.3272, 0.8293, 1.2874 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO15", "In the past 30 days", "Others complained about my drinking.", "", 4.77806, new double[] { 0.0805, 0.4429, 0.8978, 1.2664 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO16", "In the past 30 days", "I was criticized about my drinking.", "", 4.67549, new double[] { 0.0217, 0.3697, 0.8036, 1.2929 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO17", "In the past 30 days", "I got in an argument when I drank.", "", 3.95623, new double[] { -0.0958, 0.4381, 1.004, 1.4315 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO18", "In the past 30 days", "I was loud when I drank.", "", 2.89736, new double[] { -0.403, 0.1434, 0.8696, 1.3058 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO19", "In the past 30 days", "I felt sad when I drank.", "", 3.37919, new double[] { -0.1839, 0.3282, 0.973, 1.4789 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO20", "In the past 30 days", "Drinking created problems between me and others.", "", 5.53954, new double[] { -0.0578, 0.356, 0.8007, 1.1856 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO21", "In the past 30 days", "I said or did embarrassing things when I drank.", "", 4.03379, new double[] { -0.3493, 0.1512, 0.8847, 1.354 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO22", "In the past 30 days", "I lied about my drinking.", "", 3.86743, new double[] { 0.1947, 0.4625, 0.9062, 1.3198 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO23", "In the past 30 days", "I disappointed others when I drank.", "", 5.01452, new double[] { -0.0754, 0.3131, 0.7697, 1.2118 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO24", "In the past 30 days", "Others had trouble counting on me when I drank.", "", 4.90984, new double[] { 0.067, 0.4532, 0.8419, 1.2679 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO25", "In the past 30 days", "I looked sloppy when I drank.", "", 3.44111, new double[] { -0.1053, 0.4101, 1.0073, 1.5413 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO26", "In the past 30 days", "I felt dizzy when I drank.", "", 2.23888, new double[] { -0.2706, 0.3551, 1.1899, 1.8579 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO27", "In the past 30 days", "I had a hangover after I drank.", "", 2.46134, new double[] { -0.4171, 0.2699, 0.991, 1.5166 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO28", "In the past 30 days", "I used poor judgment when I drank.", "", 4.70376, new double[] { -0.3791, 0.1126, 0.6733, 1.1545 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO29", "In the past 30 days", "I had trouble getting things done after I drank.", "", 3.22003, new double[] { -0.319, 0.1903, 0.8167, 1.3582 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO30", "In the past 30 days", "I was inconsiderate when I drank.", "", 4.2962, new double[] { -0.1724, 0.3817, 0.9232, 1.4012 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  ),
  item("NECO31", "In the past 30 days", "I had trouble trusting other people when I drank.", "", 3.64057, new double[] { -0.0839, 0.431, 0.9047, 1.3746 }, -1, "", 
    response("Never", 1),
    response("Rarely", 2),
    response("Sometimes", 3),
    response("Often", 4),
    response("Almost Always", 5)
  )
);

  public static ItemBank bank() {
    return bank;
  }
}