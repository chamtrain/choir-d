package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Ped-Peer Relations 9DA933EC-4E3B-4409-8785-9D692010B7A2.json
 */
public class PromisPediatricPeerRelations {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("1147R1", "In the past 7 days", "I was good at making friends.", "", 1.75929, new double[] { -2.6899, -2.1918, -1.079, -0.1481 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("210R1", "In the past 7 days", "Other kids wanted to be with me.", "", 1.82751, new double[] { -2.5084, -2.1507, -0.7006, 0.3684 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("233R2", "In the past 7 days", "Other kids wanted to be my friend.", "", 1.5447, new double[] { -2.6506, -2.1054, -0.7832, 0.4354 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2964R1", "In the past 7 days", "I was able to have fun with my friends.", "", 1.68884, new double[] { -2.8374, -2.5996, -1.8093, -0.8281 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5018R1", "In the past 7 days", "I felt accepted by other kids my age.", "", 2.00436, new double[] { -1.9282, -1.5688, -0.8036, -0.0481 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5052R1", "In the past 7 days", "I spent time with my friends.", "", 1.27161, new double[] { -3.2718, -2.8011, -1.4779, -0.125 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5055R1", "In the past 7 days", "My friends and I helped each other out.", "", 1.74234, new double[] { -3.0064, -2.5259, -1.3561, -0.4823 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5056R1", "In the past 7 days", "I was able to talk about everything with my friends.", "", 1.94265, new double[] { -2.1794, -1.7575, -0.5905, 0.0958 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5058R1", "In the past 7 days", "I was able to count on my friends.", "", 2.69421, new double[] { -1.9937, -1.7193, -0.8775, -0.1921 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5150R1", "In the past 7 days", "I shared with other kids (food, games, pens, etc.).", "", 1.42171, new double[] { -2.6242, -2.1622, -0.8442, 0.1329 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("5152R1", "In the past 7 days", "I played alone and kept to myself.", "", 0.65301, new double[] { 0.2151, -1.3804, -3.4688, -4.9442 }, -1, "", 
      response("Never", 5),
      response("Almost Never", 4),
      response("Sometimes", 3),
      response("Often", 2),
      response("Almost Always", 1)
    ),
    item("726aR2", "In the past 7 days", "I felt good about my friendships.", "", 1.75613, new double[] { -3.0373, -2.6141, -1.5644, -0.5771 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("733R1", "In the past 7 days", "I was a good friend.", "", 2.05682, new double[] { -3.1688, -2.9195, -1.7444, -0.7781 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9019", "In the past 7 days", "I liked being around other kids my age.", "", 1.48948, new double[] { -3.0017, -2.6557, -1.4026, -0.5918 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9020R1", "In the past 7 days", "Other kids wanted to talk to me.", "", 1.89534, new double[] { -2.9605, -2.2454, -1.005, -0.0636 }, -1, "", 
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