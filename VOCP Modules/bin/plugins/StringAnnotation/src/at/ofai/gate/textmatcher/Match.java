/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.textmatcher;

/**
 *
 * @author johann
 */

// NOTE: this does not implement Comparable<Match> for sorting because
// it would be hard to make euqals compatible with compareTO == 0 and
// still implement a meaningful equals effectively.
public class Match {

  protected double stringScore;
  protected double score;
  protected int index;
  protected int startOffset;
  protected int endOffset;
  protected String string = null;

  Match(double ss, double s, int i, int start, int end, String str) {
    stringScore = ss;
    score = s;
    index = i;
    startOffset = start;
    endOffset = end;
    string = str;
  }

  public double getStringScore() {
    return stringScore;
  }
  public double getScore() {
    return score;
  }
  public int getIndex() {
    return index;
  }
  public int getStartOffset() {
    return startOffset;
  }
  public int getEndOffset() {
    return endOffset;
  }
  public String getString() {
    return string;
  }
  

}
