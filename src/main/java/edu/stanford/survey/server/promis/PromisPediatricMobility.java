package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Ped-Mobility 8A02EC35-DC30-455F-8783-B5F28206A4A7.json
 */
public class PromisPediatricMobility {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("2117R1", "In the past 7 days", "I could ride a bike.", "", 1.67305, new double[] { -2.2669, -2.0276, -1.6839, -1.1643 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2118R1", "In the past 7 days", "I could get in and out of a car.", "", 2.23977, new double[] { -3.0632, -2.6961, -2.3989, -1.7995 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("219R1", "In the past 7 days", "I could walk more than one block.", "", 1.8078, new double[] { -2.8718, -2.3949, -1.7545, -0.977 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2202R2", "In the past 7 days", "I could walk across the room.", "", 2.8, new double[] { -2.93, -2.15, -1.66 }, -1, "", 
      response("With no trouble", 4),
      response("With a little trouble", 3),
      response("With some trouble", 2),
      response("With a lot of trouble", 1),
      response("Not able to do", 1)
    ),
    item("235R1", "In the past 7 days", "I could do sports and exercise that other kids my age could do.", "", 3.10561, new double[] { -1.9157, -1.7466, -1.1439, -0.4505 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("236R1", "In the past 7 days", "I could keep up when I played with other kids.", "", 1.96391, new double[] { -2.8968, -2.4054, -1.6295, -0.4941 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2642aR1", "In the past 7 days", "I could get out of bed by myself.", "", 0.87564, new double[] { -5.5369, -4.4371, -3.4812, -2.0724 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2642bR1", "In the past 7 days", "I could get into bed by myself.", "", 2.2787, new double[] { -3.2438, -2.7815, -2.3214, -1.8352 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2646R1", "In the past 7 days", "I could stand up by myself.", "", 2.99577, new double[] { -2.7632, -2.5526, -1.9278, -1.4313 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2647R2", "In the past 7 days", "I could get down on my knees without holding on to something.", "", 1.91916, new double[] { -3.019, -2.7619, -2.2276, -1.3815 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2707R2", "In the past 7 days", "I could walk up stairs without holding on to anything.", "", 1.97407, new double[] { -2.7919, -2.3973, -1.9598, -1.2833 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("2709R1", "In the past 7 days", "I used a wheelchair to get around.", "", 1.82474, new double[] { -3.545, -3.1117, -2.4661, -2.094 }, -1, "", 
      response("Never", 5),
      response("Almost Never", 4),
      response("Sometimes", 3),
      response("Often", 2),
      response("Almost Always", 1)
    ),
    item("2715aR2", "In the past 7 days", "I used a walker, cane or crutches to get around.", "", 1.67179, new double[] { -3.5582, -3.3764, -2.8803, -2.4764 }, -1, "", 
      response("Never", 5),
      response("Almost Never", 4),
      response("Sometimes", 3),
      response("Often", 2),
      response("Almost Always", 1)
    ),
    item("3799R1", "In the past 7 days", "I could carry my books in my backpack.", "", 1.75683, new double[] { -3.0973, -2.753, -2.1873, -1.3119 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("3892R1", "In the past 7 days", "I could move my legs.", "", 3.26629, new double[] { -3.1165, -2.6533, -1.946, -1.3203 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("4079R1", "In the past 7 days", "I could get up from a regular toilet.", "", 1.41191, new double[] { -4.032, -3.9157, -3.4276, -2.6133 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("4124R1", "In the past 7 days", "I could get up from the floor.", "", 2.61934, new double[] { -3.176, -2.5641, -2.018, -1.0124 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("4137R1", "In the past 7 days", "I could go up one step.", "", 1.83541, new double[] { -4.4437, -3.6329, -3.0079, -2.2396 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("4185R1", "In the past 7 days", "I could stand up on my tiptoes.", "", 1.81861, new double[] { -3.1796, -2.4913, -1.9049, -0.9491 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("4190R1", "In the past 7 days", "I could turn my head all the way to the side.", "", 1.16044, new double[] { -4.4497, -3.9653, -3.3208, -2.2787 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("5023R1", "In the past 7 days", "I have been physically able to do the activities I enjoy most.", "", 2.36161, new double[] { -2.3493, -1.9722, -1.3321, -0.4965 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("5200bR1", "In the past 7 days", "I could run a mile.", "", 1.13124, new double[] { -2.6427, -1.7768, -0.5217, 0.8831 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    ),
    item("676R1", "In the past 7 days", "I could bend over to pick something up.", "", 2.0112, new double[] { -3.6744, -3.2866, -2.3484, -1.4788 }, -1, "", 
      response("With no trouble", 5),
      response("With a little trouble", 4),
      response("With some trouble", 3),
      response("With a lot of trouble", 2),
      response("Not able to do", 1)
    )
  );

  public static ItemBank bank() {
    return bank;
  }
}