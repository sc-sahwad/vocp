/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.textmatcher;

import java.util.Comparator;

/**
 *
 * @author johann
 */
public class MatchComparator implements Comparator<Match> {

  public MatchComparator() {
  }

  public int compare(Match o1, Match o2) {
    int ret;
    if(o1 == null && o2 == null) {
      ret = 0;
    } else if(o1 == null) {
      ret = 1;
    } else if(o2 == null) {
      ret = -1;
    } else if(o1.score > o2.score) {
      ret = -1;
    } else if(o1.score < o2.score) {
      ret = 1;
    } else if(o1.startOffset < o2.startOffset) {
      ret = -1;
    } else if(o1.startOffset > o2.startOffset) {
      ret = 1;
    } else if(o1.endOffset < o2.endOffset) {
      ret = -1;
    } else if(o1.endOffset > o2.endOffset) {
      ret = 1;
    } else {
      ret = 0;
    }
    return ret;

  }

}
