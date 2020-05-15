package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Ped-Depressive Sx 896BB80C-0540-41C0-8A2B-69DCEDC7B6B9.json
 */
public class PromisPediatricDepressiveSx {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("2227R1", "In the past 7 days", "I didn't care about anything.", "", 1.03356, new double[] { 0.0494, 1.1158, 2.6523, 3.6465 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("228R1", "In the past 7 days", "I felt sad.", "", 1.90183, new double[] { -0.7467, 0.2707, 1.7397, 2.7537 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2697R1", "In the past 7 days", "I wanted to be by myself.", "", 0.73943, new double[] { -1.8801, -0.7711, 1.1002, 2.0965 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3952aR2", "In the past 7 days", "It was hard for me to have fun.", "", 1.71154, new double[] { 0.3061, 1.0867, 2.261, 2.9965 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("461R1", "In the past 7 days", "I felt alone.", "", 2.10701, new double[] { 0.3142, 0.9805, 1.9058, 2.5769 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("488R1", "In the past 7 days", "I could not stop feeling sad.", "", 2.53484, new double[] { 0.6094, 1.1281, 1.923, 2.4619 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5035R1", "In the past 7 days", "I felt like I couldn't do anything right.", "", 2.42385, new double[] { 0.0599, 0.7987, 1.7014, 2.3208 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5041R1", "In the past 7 days", "I felt everything in my life went wrong.", "", 2.46047, new double[] { 0.35, 0.9589, 1.7392, 2.1924 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5047R1", "In the past 7 days", "I felt stressed.", "", 1.27491, new double[] { -0.9159, -0.022, 1.5411, 2.606 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("679aR2", "In the past 7 days", "Being sad made it hard for me to do things with my friends.", "", 1.86893, new double[] { 0.3555, 0.9997, 1.8656, 2.4504 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("711R1", "In the past 7 days", "I felt lonely.", "", 2.04306, new double[] { -0.1657, 0.6286, 1.7399, 2.3899 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("712R1", "In the past 7 days", "I felt unhappy.", "", 2.13731, new double[] { -0.6324, 0.4584, 1.6838, 2.423 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9001", "In the past 7 days", "I felt too sad to eat.", "", 1.44872, new double[] { 1.0166, 1.7017, 2.623, 3.4136 }, -1, "", 
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