/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ofai.gate.textmatcher;

import java.util.*;

import gate.creole.*;
import gate.creole.metadata.*;

/**
 * This class exposes functions to compare a text string against several
 * shorter strings.
 * There are several parameters that can be set for a MultiString2Text 
 * instance and subclasses for special kinds of matching strategies or
 * matching functions might add additional ones.
 * There are also several functions for retrieving information about 
 * matches. 
 *
 *
 * NOTE: the wholeWordsOnly parameter is kind of misleading: if this is set
 * to true, string matching is only attempted at non-whitespace positions
 * immediately after whitespace or at the beginning of the string.
 * However, the matching length is still determined by the match pattern,
 * not the length of the word at that position.
 *
 * @author johann
 */
public abstract class MultiString2Text 
    extends AbstractLanguageResource
    //implements gate.gui.ActionsPublisher
{

  protected Vector<String> searchStrings = null;
  protected Boolean ignoreCase = false;
  protected Boolean wholeWordsOnly = false;
  protected Boolean longestMatchOnly = true;
  protected Boolean noOverlap = false;
  protected double minScore = 0;
  protected int bestN = 1;
  protected int startOffset = 0;
  protected int endOffset = 0;     // 0 means use the end of the string
  // the following is changed by the implementing subclass
  protected int nMatches = 0;
  protected SortedSet<Match> matches = null;
  protected MatchComparator matchComparator = new MatchComparator();

  
  MultiString2Text() {
    //actionList.add(new ReInitAction());
  }

  public void setSearchStrings(Vector<String> strings) {
    searchStrings = strings;
  }

  /**
   * Perform the matching on text using the current array of strings
   * and the current parameters. After this the matcher can be queried
   * to get information about any matches that occured.
   * Returns the number of matches that were found.
   * @param text
   * @return
   */
  abstract public int match(String text) throws InvalidSimilarityMeasureException;

  @CreoleParameter(
    comment = "Ignore case when doing a comparison?",
    defaultValue = "true")
  public void setIgnoreCase(Boolean ignore) {
    ignoreCase = ignore;
  }
  public Boolean getIgnoreCase() {
    return ignoreCase;
  }

  @CreoleParameter(
    comment = "Compare only after word boundaries?",
    defaultValue = "true")
  public void setWholeWordsOnly(Boolean truefalse) {
    wholeWordsOnly = truefalse;
  }
  public Boolean getWholeWordsOnly() {
    return wholeWordsOnly;
  }

  @CreoleParameter(
    comment = "Only consider the pattern that makes the longest match at a position?",
    defaultValue = "true")
  public void setLongestMatchOnly(Boolean longest) {
    longestMatchOnly = longest;
  }
  public Boolean getLongestMatchOnly() {
    return longestMatchOnly;
  }

  @CreoleParameter(
    comment = "Ignore matches that overlap with better matches?",
    defaultValue = "true")
  public void setNoOverlap(Boolean longest) {
    noOverlap = longest;
  }
  public Boolean getNoOverlap() {
    return noOverlap;
  }

  @CreoleParameter(
    comment = "The minimum score for a match to get considered",
    defaultValue = "0.9")
  public void setMinScore(Double score) {
    minScore = score;
  }
  public Double getMinScore() {
    return minScore;
  }

  @CreoleParameter(
    comment = "How many of the best matches to take - 0 for all",
    defaultValue = "0")
  public void setBestN(Integer bestn) {
    bestN = bestn;
  }
  public Integer getBestN() {
    return bestN;
  }

  // TODO: not implemented
  /*
  public void setStartOffset(int offset) {
    startOffset = offset;
  }

  public void setEndOffset(int offset) {
    endOffset = offset;
  }

  */
  
  protected void checkParms() {
    if (searchStrings == null || searchStrings.size() == 0) {
      throw new RuntimeException("No strings defined for matching");
    }
  }

  /** 
   * This should get called at the beginning of the match method 
   * in every implementing subclass.
   * 
   */
  protected void beforeMatch() {
    checkParms();
    this.matches = new TreeSet<Match>(matchComparator);
  }

  /**
   * This should get called at the end of the match method in every
   * implementing subclass.
   */
  protected void afterMatch() {
    // if bestN is specified, filter
    // if noOverlap is true, filter
    // Overall approach: start with best match and copy over. If noOverlap
    // is true, ignore all matches that overlap with one we already picked.
    // In any case, stop when we have enough matches.
    
    if(matches.size() == 0) {
      return;
    }
    SortedSet<Match> newMatches = new TreeSet<Match>(matchComparator);
    Iterator<Match> i = matches.iterator();
    Match m = i.next();
    newMatches.add(m);
    int start = m.getStartOffset();
    int end   = m.getEndOffset();
    int needed = bestN == 0 ? matches.size() : bestN;
    int n = 1;
    while(i.hasNext() && n < needed) {
      m = i.next();
      // if the nect match(es) with lower scores overlap(s), ignore
      if(noOverlap &&
          ( (m.getStartOffset() >= start && m.getStartOffset() <= end) ||
            (m.getEndOffset()   >= start && m.getEndOffset()   <= end)
          )
        ) {
        continue;
      }
      newMatches.add(m);
      n = n + 1;
    }
    matches = newMatches;
    
  }

  public Iterator<Match> iterator() {
    return matches.iterator();
  }

  public Match[] getMatches() {
    return matches.toArray(new Match[0]);
  }

  protected void addMatch(double score, int index, int from, int to, String cand) {
    Match match = new Match(score,score,index,from,to,cand);
    matches.add(match);
  }

  // The code below was the start for implementing a right-click menu
  // option that would allow to modify parameters. This is now done more
  // easily with a VR that displays the same dialog as the one that shows
  // the initialization parameters, but allows the user to modify the
  // parameters.
  
  // this is needed to implement gate.gui.ActionsPublisher:
  // We want to add an option that will allow the user to change the parameters
  // of the object and re-initialize the object with the new parameters.
  /*
  protected List<javax.swing.AbstractAction> actionList = new ArrayList<javax.swing.AbstractAction>();

  public List getActions(){
    return actionList;
  }
  */
   /** Run the edit/reinitialization action */
  /*
  class ReInitAction extends javax.swing.AbstractAction {
    gate.gui.ResourceParametersEditor parametersEditor;
    ReInitAction(){
      super("Change/Reinitialize");
      super.putValue(SHORT_DESCRIPTION, "<html>Edit parameters and reinitialize</html>");
    }

    public void actionPerformed(java.awt.event.ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){

        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable,
                                 "Reinitialize");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)
  }//class RunAction
  */
}
