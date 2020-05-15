package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS generated from: Ped-Pain Interference AA295C4F-2E21-414D-AC90-8D6A3AE1F39F.json
 */
public class PromisPediatricPainInterference {

  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
    item("1698bR1", "In the past 7 days", "I felt angry when I had pain.", "", 1.61725, new double[] { -0.0142, 0.6588, 1.5582, 2.239 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("1701R1", "In the past 7 days", "It was hard to get along with other people when I had pain.", "", 1.34109, new double[] { -0.2413, 0.6026, 1.7654, 2.7381 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("1703R1", "In the past 7 days", "It was hard to have fun when I had pain.", "", 2.30637, new double[] { -0.4934, 1.0E-4, 1.018, 1.7084 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2032R1", "In the past 7 days", "I missed school when I had pain.", "", 1.25977, new double[] { 0.1316, 0.9297, 2.2971, 3.0189 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2035R1", "In the past 7 days", "I had trouble doing schoolwork when I had pain.", "", 1.93992, new double[] { -0.2325, 0.4618, 1.4679, 2.1575 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2045R1", "In the past 7 days", "It was hard for me to run when I had pain.", "", 1.8904, new double[] { -0.8473, -0.2488, 0.8516, 1.6307 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2049R1", "In the past 7 days", "It was hard for me to walk one block when I had pain.", "", 2.13815, new double[] { 0.2829, 0.7895, 1.4955, 1.9709 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("2180R1", "In the past 7 days", "It was hard to stay standing when I had pain.", "", 2.34751, new double[] { -0.1784, 0.4378, 1.4046, 1.968 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3582R1", "In the past 7 days", "I hurt a lot.", "", 1.40581, new double[] { -0.4791, 0.7647, 2.1721, 3.0401 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("3793R1", "In the past 7 days", "I had trouble sleeping when I had pain.", "", 2.34984, new double[] { -0.2299, 0.3065, 1.1679, 1.6917 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9004", "In the past 7 days", "It was hard for me to pay attention when I had pain.", "", 2.34775, new double[] { -0.249, 0.3216, 1.332, 2.0299 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9007", "In the past 7 days", "It was hard for me to remember things when I had pain.", "", 1.49916, new double[] { 0.293, 1.0773, 2.1161, 3.5523 }, -1, "", 
      response("Never", 1),
      response("Almost Never", 2),
      response("Sometimes", 3),
      response("Often", 4),
      response("Almost Always", 5)
    ),
    item("9009", "In the past 7 days", "I hurt all over my body.", "", 1.82395, new double[] { 0.4859, 1.193, 2.0533, 2.7193 }, -1, "", 
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