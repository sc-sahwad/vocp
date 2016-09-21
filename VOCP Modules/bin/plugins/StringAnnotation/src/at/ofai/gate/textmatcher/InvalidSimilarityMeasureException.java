/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.ofai.gate.textmatcher;

/**
 *
 * @author johann
 */

// TODO: this needs to be implemented in some reasonable way 
public class InvalidSimilarityMeasureException extends Exception {
  
  public InvalidSimilarityMeasureException(String message) {
    super(message);
  }

  public InvalidSimilarityMeasureException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidSimilarityMeasureException(Throwable e) {
    super(e);
  }

  InvalidSimilarityMeasureException() {
  }

}
