/*
 * OFAILocalGazetteer.java
 * 
 * Copyright (c) 2006, 2007, Austrian Research Institute for Artificial Intelligence 
 *
 * This is free software, licenced under the GNU Library General Public License,
 * Version 2, June1991. See http://www.gnu.org/licenses/gpl.txt
 * 
 * Author: Johann Petrak
 *
 *
 */
package at.ofai.gate.textmatcher;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.creole.metadata.*;


/** 
 * This PR acts like a gazetteer but can use a different set of gazetteer
 * entries for each document. Before this PR can be run, the list of gazetteer
 * entries and their corresponding types has to be stored in a vector of
 * vectors where the contained vectors contain exactly two strings each.
 * This vector has to be stored as the value of a feature for some annotation
 * and the annotation type and feature name can be specified for runtime
 * parameters tableAnnType and tableAnnFeature.
 * 
 * The PR processes annotations of type containingAnnotationType and generates
 * a text portion for each of those annotations, depending on whether and which
 * contained annotations are specified.
 * If no contained annotations are specified, the document text for the
 * containing annotation is used.
 * Contained annotations can be specified by either just the type name or
 * a type name, followed by a dot, followed by a feature name.
 * If contained annotations are specified the result text is a concatenation
 * of the first contained annotation text or feature that matches at each
 * position.
 *
 * @author Johann Petrak
 */

/*
 * 
 * disable the display of this PR in the GUI for now ...
@CreoleResource(
  name = "OFAILocalGazetteer",
  comment = "Match a set of document-specific patterns flexibly.",
  helpURL = "http://www.ofai.at/~johann.petrak/GATE/OFAITextMatcher/help.html",
  isPrivate = false
  //icon = "",
  //interfaceName = "",
  //autoInstances =
  )
 */
public class OFAILocalGazetteer
    extends gate.creole.AbstractLanguageAnalyser
    implements ControllerAwarePR
{

  private static final long serialVersionUID = 1L;
  private static final String TMP_AS_NAME = "OFAILocalGazetteer_tmp01";
  private static final int INBETWEEN_SPACE = 0;
  private static final int INBETWEEN_NOTHING = 1;
  
  static boolean isShownVersion = false;


  protected String inputAnnotationSetName;
  @RunTime
  @CreoleParameter(
    comment = "Name of the input annotation set",
    defaultValue = "")
  public void setInputAnnotationSetName(String annotationSetName) {
    this.inputAnnotationSetName = annotationSetName;
  }
  public String getInputAnnotationSetName() {
    return inputAnnotationSetName;
  }

  protected String containingAnnotationType;
  @RunTime
  @CreoleParameter(
    comment = "Type of the containing annotations",
    defaultValue = "Sentence")
  public void setContainingAnnotationType(String annotationType) {
    this.containingAnnotationType = annotationType;
  }
  public String getContainingAnnotationType() {
    return containingAnnotationType;
  }

  protected String outputAnnotationSetName;
  @RunTime
  @CreoleParameter(
    comment = "The name of the output annotation set",
    defaultValue = "Levenshtein")
  public void setOutputAnnotationSetName(String annotationSetName) {
    this.outputAnnotationSetName = annotationSetName;
  }
  public String getOutputAnnotationSetName() {
    return outputAnnotationSetName;
  }
  
  // the annotation type to assign to local NERs 
  protected String outputAnnotationType;
  @RunTime
  @CreoleParameter(
    comment = "The type of the annotations that will be created",
    defaultValue = "LookupLocal")
  public void setOutputAnnotationType(String annotationType) {
    this.outputAnnotationType = annotationType;
  }
  public String getOutputAnnotationType() {
    return outputAnnotationType;
  }

  // If the list is empty, the document string for the containing ann is
  // used, otherwise the type/feature with decreasing priority.
  // If only a type is specified, the document string for the annotation of
  // this type is used.
  private java.util.List<String> inputFeatureNames;
  @RunTime
  @Optional
  @CreoleParameter(
    comment = "List of types or type.feature of the contained annotations",
    collectionElementType = java.lang.String.class
    )
  public void setInputFeatureNames(java.util.List<String> names) {
    inputFeatureNames = names;
  }
  public java.util.List<String> getInputFeatureNames() {
    return inputFeatureNames;
  }

  private int inBetween = INBETWEEN_SPACE;
  @RunTime
  @CreoleParameter(
    comment = "What to insert between the text obtained from the contained annotations: one of 'space' or 'nothing'",
    defaultValue = "space")
  public void setInBetween(String to) {
    if(to.equals("space")) {
      inBetween = INBETWEEN_SPACE;
    } else if (to.equals("nothing")) {
      inBetween = INBETWEEN_NOTHING;
    } else {
      throw new GateRuntimeException("Only 'space' and 'nothing' allowed as in-between values");
    }
  }
  public String getInBetween() {
    return inBetween == INBETWEEN_SPACE ? "space" : "nothing";
  }

  private MultiString2Text textMatcherLR;
  @RunTime
  @Optional
  @CreoleParameter(
    comment = "The Matcher LR to use"
  )
  public void setTextMatcherLR(MultiString2Text matcher) {
    textMatcherLR = matcher;
  }
  public MultiString2Text getTextMatcherLR() {
    return textMatcherLR;
  }



  protected String tableAnnSet;
  @RunTime
  @CreoleParameter(
    comment = "The name of the annotation set that contains the table of patterns",
    defaultValue = "")
  public void setTableAnnSet(String name) {
    tableAnnSet = name;
  }
  public String getTableAnnSet() {
    return tableAnnSet;
  }
  protected String tableAnnType;
  @RunTime
  @CreoleParameter(
    comment = "The type of the annotation set that contains the table of patterns",
    defaultValue = "LocalNER")
  public void setTableAnnType(String type) {
    tableAnnType = type;
  }
  public String getTableAnnType() {
    return tableAnnType;
  }
  protected String tableAnnFeature;
  @RunTime
  @CreoleParameter(
    comment = "The feature that containes the table of patterns",
    defaultValue = "pattern_table")
  public void setTableAnnFeature(String feature) {
    tableAnnFeature = feature;
  }
  public String getTableAnnFeature() {
    return tableAnnFeature;
  }

  // TODO: what should be the exact meaning of this???
  // not yet implemented!
  /*
  protected Boolean wholeWordsOnly;
  @RunTime
  @CreoleParameter(
    comment = "",
    defaultValue = "Levenshtein")
  public void setWholeWordsOnly(Boolean yesorno) {
      wholeWordsOnly = yesorno;
  }
  public Boolean getWholeWordsOnly() {
      return wholeWordsOnly;
  }
 */

  public void controllerExecutionStarted(gate.Controller c)
    throws ExecutionException {
    startup();
  }
  public void controllerExecutionFinished(gate.Controller c)
    throws ExecutionException {
    shutdown();
  }
  public void controllerExecutionAborted(gate.Controller c, java.lang.Throwable t)
    throws ExecutionException {
    
  }

  public OFAILocalGazetteer(){
  }

  public void startup() throws ExecutionException {
    
    //System.out.println("This is the startup for the corpus");
 }
  
  public void shutdown() {
    
    //System.out.println("This is the shutdown method of OFAILocalNER");
  }

  @Override
  public Resource init() throws ResourceInstantiationException {
    /* do the init stuff we need specially for this subclass */
    if(!isShownVersion) {
      System.out.println("Plugin version: $Id: OFAILocalGazetteer.java 353 2009-01-18 23:08:26Z johann $");
      isShownVersion = true;
    }
    // TODO: do parameter checking here
    return super.init();
  }
  

  /**
   * This method runs the gazetteer.
   */
    
  //@throws gate.creole.ExecutionException;
  public void execute() throws ExecutionException{
    interrupted = false;
    int totalmatches = 0;
    AnnotationSet outputAS;  // where to place the generated anns
    AnnotationSet inputAS;
    //check the input
    if(document == null) {
      throw new ExecutionException(
        "No document to process!"
      );
    }

    // get the annset for the result annotations
    if(outputAnnotationSetName == null ||
       outputAnnotationSetName.equals("")) {
      outputAS = document.getAnnotations();
    } else {
      outputAS = document.getAnnotations(outputAnnotationSetName);
    }

    // First get the table feature .. if we do not find it, we can skip
    // processing this document
    // 1-get the set
    AnnotationSet tableSet;
    if(tableAnnSet == null || tableAnnSet.equals("")) {
      tableSet = document.getAnnotations();
    } else {
      //System.out.println("Getting annotations for tableAnnSet: "+tableAnnSet);
      tableSet = document.getAnnotations(tableAnnSet);
    }
    //System.out.println("Number of anns for tableAnnSet: "+tableSet.size());
    // filter the type
    // we assume that there is only one annotation of this max, if there
    // are more, we take whatever the iterator returns first
    // If there is none, we are done.
    tableSet = tableSet.get(tableAnnType);
    if(tableSet.size() == 0) {
      System.out.println("No annotation type "+tableAnnType+
          " for table of search patterns found, skipping document "+
          document.getName());
      return;
    }
    Annotation ta = tableSet.iterator().next();
    // TODO: check if the content of the feature can be converted
    Vector<Vector<String>> table = (Vector<Vector<String>>)ta.getFeatures().get(tableAnnFeature);

    // check if there is a table at all and if yes, if it is non-empty
    if(table == null) {
      System.out.println("Feature for table of patterns not found, skipping document "+document.getName());
      return;
    }

    if(table.size() == 0) {
      System.out.println("Table of patterns is empty, skipping document "+document.getName());
      return;
    }

    // extract the patterns and classes columns
    Vector<String> patterns = new Vector<String>(table.size());
    Vector<String> classes  = new Vector<String>(table.size());
    for(int i = 0; i<table.size(); i++) {
      patterns.add(table.get(i).get(0));
      classes.add(table.get(i).get(1));
    }

    // set the patterns in the matcher object
    textMatcherLR.setSearchStrings(patterns);

    fireStatusChanged("Performing look-up in " + document.getName() + "...");
    
    // iterate over all containing annotations in the inputAS
    //   for a containing annotation
    //      get the text:
    //         if inputFeatures is empty use the document string covered
    //         otherwise create the text:
    //
    // TODO: for now we just use the text covered by the containing
    // annotation

    if(inputAnnotationSetName == null ||
       inputAnnotationSetName.equals("")) {
      inputAS = document.getAnnotations();
    } else {
      inputAS = document.getAnnotations(inputAnnotationSetName);
    }
    
    AnnotationSet inputAnnotations = inputAS.get(containingAnnotationType);
    Iterator<Annotation> it = inputAnnotations.iterator();
    String content = document.getContent().toString();

    // this is a temporary AS used if we want to do matches withing contained
    // annotations or features of contained annotations.
    // Since we need to modify and add to the AS we need to create a new
    // set that is attached to the document (the derived sets are immutable).
    // After the set has been used it is deleted again.
    // If we do not need the set we do not create it at all.
    AnnotationSet tmpAS = null;
    Vector<String> containedTypeNames = new Vector<String>();
    Vector<String> containedFeatureNames = new Vector<String>();
    if(inputFeatureNames != null && inputFeatureNames.size() > 0) {
      HashSet<String> seenASNames = new HashSet<String>();
      tmpAS = document.getAnnotations(TMP_AS_NAME);
      Iterator<String> if_it = inputFeatureNames.iterator();
      while(if_it.hasNext()) {
        String annspec = if_it.next();
        // we allow two different types of specification: without a dot
        // will indicate a simple annotation type and with a single dot
        // will indicate a type/feature combination
        // TODO we do not test if there is more than one dot, we simply
        // take the first one.
        String annspec_type = null;
        String annspec_feature = null;
        int dotindex = annspec.indexOf(".");
        if(dotindex > -1) {
          annspec_type = annspec.substring(0,dotindex);
          if(dotindex == (annspec.length()-1)) {
            throw new GateRuntimeException("Contained annotation specification "+annspec+" ends with a dot");
          }
          annspec_feature = annspec.substring(dotindex+1);
          //System.out.println("Found type and feature: "+annspec_type+"/"+annspec_feature);
        } else {
          annspec_type = annspec;
          //System.out.println("Found type: "+annspec_type);
       }
        containedTypeNames.add(annspec_type);
        containedFeatureNames.add(annspec_feature);
        // if we have not yet added that type, do it now.
        //System.out.println("Members in hashset: "+seenASNames.size());
        if(!seenASNames.contains(annspec_type)) {
          seenASNames.add(annspec_type);
          AnnotationSet selectedAS = inputAS.get(annspec_type);
          if(selectedAS == null) {
            System.out.println("Type not found in inputAS: "+annspec_type);
          } else {
            //System.out.println("Type added: "+annspec_type);
            tmpAS.addAll(inputAS.get(annspec_type));
          }
        }
      }
    }

    while(it.hasNext()) {
      Annotation containing = it.next();
      Vector<Integer> from_offsets = new Vector<Integer>();
      Vector<Integer> to_offsets = new Vector<Integer>();
      String text;
      int containing_start = containing.getStartNode().getOffset().intValue();
      int containing_end   = containing.getEndNode().getOffset().intValue();
      //System.out.println("Found containing annotation from "+containing_start+" to "+containing_end);
      if(containedTypeNames.size() == 0) {
        // we do not use any contained annotations, just extract the document text
        text = content.substring(
            containing_start, containing_end);
      } else {
        // get those annotations in tmpAS that are located within the extent
        // of the current containing annoation
        AnnotationSet withinAS =
            tmpAS.getContained(
              containing.getStartNode().getOffset(),
              containing.getEndNode().getOffset());
        Iterator<Annotation> within_it = getSortedAnnotationsIterator(withinAS);
        if(within_it == null) {
          continue;
        }
        int cur_off = -1;
        int cur_end = -1;
        StringBuffer textbuf = new StringBuffer();
        while(within_it.hasNext()) {
          Annotation cur = within_it.next();
          // skip the annotation if we are not after the start of the last cur
          // offset or not after the end of the last current
          if(cur.getStartNode().getOffset().intValue() <= cur_off) {
            continue;
          }
          if(cur.getStartNode().getOffset().intValue() <= cur_end) {
            continue;
          }
          // now go throw the table of types and features and test if this
          // annotation matches:
          for(int i = 0; i<containedTypeNames.size(); i++) {
            if(containedTypeNames.get(i).equals(cur.getType())) {
              // could be a candidate, now if we also need a feature name,
              // check if the feature exists in this annotation.
              if(containedFeatureNames.get(i) != null) {
                FeatureMap fm = cur.getFeatures();
                String featurevalue = (String)fm.get(containedFeatureNames.get(i));
                if(featurevalue != null) {
                  // the featurevalue is the string that will be used for
                  // concatenation:
                  // append the feature value to the text and optionally
                  // also append the inbetween value
                  // In theory the last inbetween value should not be there but
                  // since we map it to the last position of the last string
                  // anyways it does not make a difference.
                  textbuf.append(featurevalue);
                  cur_off = cur.getStartNode().getOffset().intValue();
                  cur_end = cur.getEndNode().getOffset().intValue();
                  for(i=0; i<featurevalue.length(); i++) {
                    from_offsets.add(cur_off);
                    to_offsets.add(cur_end);
                  }
                  if(inBetween == INBETWEEN_SPACE) {
                    textbuf.append(" ");
                    from_offsets.add(cur_off);
                    to_offsets.add(cur_end);
                  }

                  break;
                } // else we do need a featurevalue but it is not there: continue
              } else {
                // TODO: we only need to concatenate the document text for this ann
                // unless first, also concatenate the between value
                // set the mapping offsets for the between value and the concatenated string
                // adapt cur_off and cur_end
                cur_off = cur.getStartNode().getOffset().intValue();
                cur_end = cur.getEndNode().getOffset().intValue();
                String thisstring = content.substring(cur_off, cur_end);
                textbuf.append(thisstring);
                for(i=0; i<thisstring.length(); i++) {
                  from_offsets.add(cur_off+i);
                  to_offsets.add(cur_off+i);
                }
                if(inBetween == INBETWEEN_SPACE) {
                  textbuf.append(" ");
                  from_offsets.add(cur_off+thisstring.length()-1);
                  to_offsets.add(cur_off+thisstring.length()-1);
                }
               break;
              }
            }
          }
        }
        text = textbuf.toString();
      }
      int nrmatches;
      try {
        //System.out.println("Trying match for string: >"+text+"<");
        nrmatches = textMatcherLR.match(text);
      } catch (InvalidSimilarityMeasureException ex) {
        throw new GateRuntimeException("Similarity measure not set or unknown: "+ex);
      }
      if(nrmatches > 0) {
        totalmatches = totalmatches + nrmatches;
        Match[] matches = textMatcherLR.getMatches();
        for (Match match : matches) {
          // check if we have contained annotations, and determince
          // the from/to offsets accordingly
          int from = -1;
          int to   = -1;
          if(containedTypeNames.size() == 0) {
            from = match.getStartOffset() + containing_start;
            to   = match.getEndOffset() + containing_start;
          } else {
            //System.out.println("Match start offset is: "+match.getStartOffset());
            //System.out.println("Match end offset is: "+match.getEndOffset());
            //System.out.println("Length from_offsets: "+from_offsets.size());
            //System.out.println("Length to_offsets: "+to_offsets.size());
            from = from_offsets.get(match.getStartOffset());
            to   = to_offsets.get(match.getEndOffset()-1);
          }
          double score = match.getScore();
          int matchindex = match.getIndex();
          String classname = classes.get(matchindex);
          FeatureMap fm = Factory.newFeatureMap();
          fm.put("class", classname);
          fm.put("pattern",patterns.get(matchindex));
          fm.put("score",score);
          fm.put("score_string",""+score);
          fm.put("string",match.getString());
          try {
            // TODO? also add a feature that contains the actual part in the text
            // that was matched (needed if matching against a feature)
            outputAS.add(new Long(from),
                              new Long(to),
                              outputAnnotationType,
                              fm);
          } catch (gate.util.InvalidOffsetException e) {
            System.err.println("Invalid offset exception when annotating "+match+" in doc "+document.getName());
          }

        }
      }

      //System.out.println("Found "+totalmatches+" matches in document "+document.getName());

    }
    if (tmpAS != null) {
      document.removeAnnotationSet(TMP_AS_NAME);
    }


    fireProcessFinished();
    fireStatusChanged("Lookup complete!");
  } // execute

  protected Iterator<Annotation> getSortedAnnotationsIterator(AnnotationSet annset) {
    List<Annotation> tokens = new ArrayList(annset);

    if(tokens == null) {
      return null;
    }

    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(tokens, offsetComparator);
    Iterator<Annotation> tokenIter = tokens.listIterator();
    return tokenIter;
  }


} 
