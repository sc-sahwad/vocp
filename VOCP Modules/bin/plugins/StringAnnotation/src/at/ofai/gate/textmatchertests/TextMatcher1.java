/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ofai.gate.textmatchertests;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;
import at.ofai.gate.textmatcher.*;

/**
 * Try out some functionality of the textmatcher package.
 * @author johann
 */
public class TextMatcher1 {

  public static void main(String[] args) {
    test1(args);
  }

  public static void test1(String[] args) {
    // create Options object
    Options options = new Options();
    Option o_bestn   = OptionBuilder
        .withArgName( "bestn" )
        .hasOptionalArg()
        .withDescription(  "Only pick best N matches, 0=all" )
        .create( "bestn" );
    Option o_minscore   = OptionBuilder
        .withArgName( "minscore" )
        .hasOptionalArg()
        .withDescription(  "Minimum score, 0.0 - 1.0" )
        .create( "minscore" );
    Option o_longestonly   = OptionBuilder
        .withArgName( "longestonly" )
        .hasOptionalArg()
        .withDescription(  "0 = no, 1 = yes" )
        .create( "longestonly" );
    Option o_nooverlap   = OptionBuilder
        .withArgName( "nooverlap" )
        .hasOptionalArg()
        .withDescription(  "0 = no, 1 = yes" )
        .create( "nooverlap" );
    Option o_ignorecase   = OptionBuilder
        .withArgName( "ignorecase" )
        .hasOptionalArg()
        .withDescription(  "0 = no, 1 = yes" )
        .create( "ignorecase" );

    // add t option
    options.addOption(o_bestn);
    options.addOption(o_minscore);
    options.addOption(o_longestonly);
    options.addOption(o_nooverlap);
    options.addOption(o_ignorecase);
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException ex) {
      System.err.println("Error parsing command line: "+ex);
      System.exit(1);
    }

    int bestN = 0;
    if(cmd.hasOption( "bestn" ) ) {
      bestN  = Integer.parseInt(cmd.getOptionValue( "bestn" ));
    }
    double minScore = 0.0;
    if(cmd.hasOption( "minscore" ) ) {
      minScore  = Double.parseDouble(cmd.getOptionValue( "minscore" ));
    }
    boolean longestMatchOnly = false;
    if(cmd.hasOption( "longestonly" ) ) {
      longestMatchOnly  = Integer.parseInt(cmd.getOptionValue( "longestonly" )) == 1;
    }
    boolean noOverlap = false;
    if(cmd.hasOption( "nooverlap" ) ) {
      noOverlap  = Integer.parseInt(cmd.getOptionValue( "nooverlap" )) == 1;
    }
    boolean ignoreCase = false;
    if(cmd.hasOption( "ignorecase" ) ) {
      ignoreCase  = Integer.parseInt(cmd.getOptionValue( "ignorecase" )) == 1;
    }
    // create an instance of a matcher, initialize some random strings
    // and perform the matching
    Vector<String> measures = new Vector<String>();
    measures.add("CosineSimilarity");
    measures.add("EuclideanDistance");
    measures.add("Levenshtein");
    //measures.add("invaludmeasurehehe");
    Vector<String> patterns = new Vector<String>();
    patterns.add("word");
    patterns.add("two words");
    patterns.add("word and word");
    patterns.add("something else");
    patterns.add("somethingelse");
    String text = "This is a word and there are other  words and something else too";

    for (String measure : measures) {
      MultiString2Text myMatcher = null;
      try {
        myMatcher = new MultiString2TextSimmetrics(measure);
      } catch (InvalidSimilarityMeasureException e) {
        continue;
      }
      myMatcher.setSearchStrings(patterns);
      myMatcher.setMinScore(minScore);
      myMatcher.setBestN(bestN);
      myMatcher.setLongestMatchOnly(longestMatchOnly);
      myMatcher.setNoOverlap(noOverlap);
      myMatcher.setIgnoreCase(ignoreCase);

      System.out.println("Running measure " + measure);
      int n = 0;
      try {
        n = myMatcher.match(text);
      } catch (InvalidSimilarityMeasureException ex) {
        System.err.println("Invalid measure exception: "+ex);
        System.exit(1);
      }
      System.out.println("Found " + n + "matches:");
      Iterator<Match> i = myMatcher.iterator();
      while (i.hasNext()) {
        Match m = i.next();
        System.out.println(
            "Score=" + m.getScore() +
            " from=" + m.getStartOffset() +
            " cand=" + m.getString() +
            " text=" + text.substring(m.getStartOffset(), m.getEndOffset()) +
            " idx=" + m.getIndex());
      }
    }
  }
}
