package SketchAnalysisOnHealthcare.SketchAnalysisOnHealthcare;

/* Class that reads a FrequentItemSet file and tries to split as evenly as possible to form
   and n-level(user specified tree depth). It forms a JSON object as output which will have the
   required tree which could be used for UI based visualization to show how the data is split.
   Focus is to keep the tree paths as mutually exclusive as possible.

   This code reads the FIS data. The code also takes as an optional input a file containing
   dimension and value pairs of interest on which the segmentation needs to be done.
   The user can also specify the starting dimension=value pair for the first split. Here the
   user needs to be careful to specify "mutually exclusive" options for the split.

   Lastly, the user can also specify the filter as a pre-condition based on which the
   data can be split.

   Max 4 level trees are built
   Split options can be equal split as possible or max split towards the left node
   Inputs
           1: inclusionFileName :-  File with inclusion list of dimension = value
              or only dimension(like in the case of ageGroup and Gender). If empty,
              it will include all the dimension value pairs.
           2: exclusionFileName :- File with exclusion list of dimension = value or
              only dimension(like in the case of ageGroup and Gender). If empty,
              none of the dimension value pairs will be excluded from the inclusion
              list.
           3. levels :- Number of levels default 3.
           4. precondition :- filter that will be applied first. This will follow the
              syntax of !, and, | as well as parentheses. The terminals of this will be
              dimension value pair example :
              "Gender = M" and ("AgeGroup = 70-75" or "AgeGroups = 65-70") and !"ICD=I50"
           5. dimensionValueSketches File :- the file with dimension value followed by
              Base64 representing the sketches
           6. FrequentItemSet File :- This will be used as is if there is no filter or
              the Filter is simple and does not have "or" and "and". Top level segmentations
              will run fast using the Frequent itemset list
           7. Split type order :- Middle split(default) or a greedy split, or max confidence
              or max confidence
           8. Lift - minimum value of lift
 */
public class DataSegmentation {

}
