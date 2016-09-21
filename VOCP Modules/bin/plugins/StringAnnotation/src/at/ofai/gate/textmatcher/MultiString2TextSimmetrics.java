/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ofai.gate.textmatcher;

import java.util.*;
import gate.creole.metadata.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.*;

/**
 * A MultiString2Text matcher that makes it easy to use the string
 * matching measures implemented in the Simmetrics library.
 *
 * @author johann
 */

/*
@CreoleResource(
  name = "Matcher:Simmetrics",
  comment = "String matching functions from the Simmetrics library",
  helpURL = "http://www.ofai.at/~johann.petrak/GATE/OFAITextMatcher/hel.html",
  isPrivate = false
  //icon = "",
  //interfaceName = "",
  //autoInstances =
  )
 * 
 */
public class MultiString2TextSimmetrics extends MultiString2Text {

  public static final long serialVersionUID = 1L;
  Object metric = null;

  String measureName = null;

  /**
   * The constructor takes a string that must be the method name of the
   * Simmetrics similarity measure. For now we do not consider tokenizers.
   * @param measure
   */
  public MultiString2TextSimmetrics(String measure)
  throws InvalidSimilarityMeasureException
  {
    setMeasureClassname(measure);
  }

  public MultiString2TextSimmetrics()
  throws InvalidSimilarityMeasureException
  {
  }

  @CreoleParameter(
    comment = "The simmetrics measure classname",
    defaultValue = "Levenshtein")
  public void setMeasureClassname(String name)
    throws InvalidSimilarityMeasureException {
      // try to find the class for the measure and create an instance.
    measureName = "";
    String className = "";
    className = name;
    try {
      Class theClass = Class.forName(className);
      metric = theClass.newInstance();
      measureName = name;
    } catch (Exception e) {
      // ignore
    }
    className = "at.ofai.gate.textmatcher." + name;
    try {
      Class theClass = Class.forName(className);
      metric = theClass.newInstance();
      measureName = name;
    } catch (Exception e) {
      // ignore
    }
    className = "uk.ac.shef.wit.simmetrics.similaritymetrics." + name;
    try {
      Class theClass = Class.forName(className);
      metric = theClass.newInstance();
      measureName = name;
    } catch (Exception e) {
      // ignore
    }
    if(measureName.equals("")) {
      throw new InvalidSimilarityMeasureException("Classname "+name+" not found");
    }
  }
  public String getMeasureClassname() {
    return measureName;
  }

  public int match(String text)  throws InvalidSimilarityMeasureException {
    beforeMatch();
    if(metric == null) {
       System.err.println("No similarity measure classname specified");
       throw new InvalidSimilarityMeasureException();     
    }

    // if lowercase convert text to lowercase
    if (this.ignoreCase) {
      text = text.toLowerCase();
    }
    int textLength = text.length();
    // for each position in the original text up to length - shortest pattern
    // TODO: implement startOffset/endOffset
    for (int textpos = 0; textpos < textLength; textpos++) {
      //   bestscore for this position is 0
      //   for each windowsize(not yet)
      //      create text_cand from offset, lentgth=l(cand)+-windowsize
      //      for each candidate string
      // if the wholeWordsOnly parameter is true, skip all positions that
      // are not both: non-whitespace and immediately after whitespace or
      // at the beginning
      // i.e. skip if:
      //  - we are at whitespace
      //  - if we are not at the first position and the preceding char is not whitespace
      char ch = text.charAt(textpos);
      if(wholeWordsOnly &&
          (Character.isWhitespace(ch) ||
           textpos > 0 && !Character.isWhitespace(text.charAt(textpos-1)) ) ) {
        continue;
      }
      Vector<Double> thisScores = new Vector<Double>();
      Vector<Integer> thisCandIndexes = new Vector<Integer>();
      Vector<String> thisCand = new Vector<String>();
      int candindex = 0;
      int longestLength = 0;
      for (candindex = 0; candindex < searchStrings.size(); candindex++) {

        String cand = searchStrings.get(candindex);
        // it does not make sense to match against the empty string ...
        if(cand.length() == 0) {
          continue;
        }
        //         create window by creating text_cand based on size of pattern+- window
        int candLength = cand.length();

        // skip the match if the remaining area in text is shorter
        // than the candidate.
        int textend = textpos + candLength;
        if (textend > text.length()) {
          continue;
        }
        String textcand = text.substring(textpos, textend);

        //         if lowercase, convert cand to lowercase
        if (this.ignoreCase) {
          cand = cand.toLowerCase();
        }
        //         match(text(here,length(cand)) against cand
        float tmpscore = ((InterfaceStringMetric) metric).getSimilarity(textcand, cand);
        Double score = new Double(tmpscore);
        //         if score > minscore
        if (score >= minScore) {
          //            add to this_matches vector: score, candindex
          thisScores.add(score);
          thisCandIndexes.add(candindex);
          thisCand.add(cand);
          if (longestLength < candLength) {
            longestLength = candLength;
          }
        }
      } // for cand in searchStrings
      if (thisScores.size() > 0) {

        for (int i = 0; i < thisScores.size(); i++) {
          if (this.longestMatchOnly) {
            if (thisCand.get(i).length() == longestLength) {
              addMatch(
                  thisScores.get(i),
                  thisCandIndexes.get(i),
                  textpos,
                  textpos + thisCand.get(i).length(),
                  thisCand.get(i));
            }
          } else {
            addMatch(
                thisScores.get(i),
                thisCandIndexes.get(i),
                textpos,
                textpos + thisCand.get(i).length(),
                thisCand.get(i));
          }
        }
      }
    } // end for textpos
    afterMatch();
    return matches.size();
  }
}
