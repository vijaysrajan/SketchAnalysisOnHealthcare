package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

/*
  This code is used as a predictor that given a precondition example
  purchase or diseases etc, what are the top N expectation.
  This can be useful for as a precautionary measure.

  Input:
  1. inclusionFileName :-  File with inclusion list of dimension = value
              or only dimension(like in the case of ageGroup and Gender). If empty,
              it will include all the dimension value pairs.
  2: exclusionFileName :- File with exclusion list of dimension = value or
              only dimension(like in the case of ageGroup and Gender). If empty,
              none of the dimension value pairs will be excluded from the inclusion
              list.
   3: precondition :- filter that will be applied first. This will follow the
              syntax of !, and, | as well as parentheses. The terminals of this will be
              dimension value pair example :
              "Gender = M" and ("AgeGroup = 70-75" or "AgeGroups = 65-70") and !"ICD=I50"
   4: FIS file :- This will be used as is if there is no filter or
              the Filter is simple and does not have "or" and "and". Top level segmentations
              will run fast using the Frequent itemset list
   5: Sketches File :- file containing sketches
 */
public class GivenAPreConditionWhatAreTheTopNExpectations {

}
