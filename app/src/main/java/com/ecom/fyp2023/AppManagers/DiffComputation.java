package com.ecom.fyp2023.AppManagers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ecom.fyp2023.ModelClasses.Diff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;

/*
 * Functions for diff, match and patch.
 * Computes the difference between two texts to create a patch.
 * Applies the patch onto another text, allowing for errors.
 */

/**
 * Class containing the diff, methods.
 * Also contains the behaviour settings.
 */

public class DiffComputation {
  public float Diff_Timeout = 1.0f;

  protected static class LinesToCharsResult {
    protected String chars1;
    protected String chars2;
    protected List<String> lineArray;

    protected LinesToCharsResult(String chars1, String chars2,
        List<String> lineArray) {
      this.chars1 = chars1;
      this.chars2 = chars2;
      this.lineArray = lineArray;
    }
  }

  //  DIFF FUNCTIONS
  /**
   * The data structure representing a diff is a Linked list of Diff objects:
   * {Diff(Operation.DELETE, "Hello"), Diff(Operation.INSERT, "Goodbye"),
   *  Diff(Operation.EQUAL, " world.")}
   * which means: delete "Hello", add "Goodbye" and keep " world."
   */
  public enum Operation {
    DELETE, INSERT, EQUAL
  }

  //Find the differences between two texts.
  public LinkedList<Diff> diff_main(String text1, String text2) {
    return diff_main(text1, text2, true);
  }

  public LinkedList<Diff> diff_main(String text1, String text2,
                                    boolean checklines) {
    // Set a deadline by which time the diff must be complete.
    long deadline;
    if (Diff_Timeout <= 0) {
      deadline = Long.MAX_VALUE;
    } else {
      deadline = System.currentTimeMillis() + (long) (Diff_Timeout * 1000);
    }
    return diff_main(text1, text2, checklines, deadline);
  }

  //Find the differences between two texts.  Simplifies the problem by stripping any common prefix or suffix off the texts before diffing.
  private LinkedList<Diff> diff_main(String text1, String text2, boolean checklines, long deadline) {
    // Check for null inputs.
    if (text1 == null || text2 == null) {
      throw new IllegalArgumentException("Null inputs. (diff_main)");
    }

    // Check for equality (speedup).
    LinkedList<Diff> diffs;
    if (text1.equals(text2)) {
      diffs = new LinkedList<Diff>();
      if (text1.length() != 0) {
        diffs.add(new Diff(Operation.EQUAL, text1));
      }
      return diffs;
    }

    // Trim off common prefix (speedup).
    int commonlength = diff_commonPrefix(text1, text2);
    String commonprefix = text1.substring(0, commonlength);
    text1 = text1.substring(commonlength);
    text2 = text2.substring(commonlength);

    // Trim off common suffix (speedup).
    commonlength = diff_commonSuffix(text1, text2);
    String commonsuffix = text1.substring(text1.length() - commonlength);
    text1 = text1.substring(0, text1.length() - commonlength);
    text2 = text2.substring(0, text2.length() - commonlength);

    // Compute the diff on the middle block.
    diffs = diff_compute(text1, text2, checklines, deadline);

    // Restore the prefix and suffix.
    if (commonprefix.length() != 0) {
      diffs.addFirst(new Diff(Operation.EQUAL, commonprefix));
    }
    if (commonsuffix.length() != 0) {
      diffs.addLast(new Diff(Operation.EQUAL, commonsuffix));
    }

    diff_cleanupMerge(diffs);
    return diffs;
  }

  //Find the differences between two texts.  Assumes that the texts do not have any common prefix or suffix.
  private LinkedList<Diff> diff_compute(@NonNull String text1, String text2, boolean checklines, long deadline) {
    LinkedList<Diff> diffs = new LinkedList<Diff>();

    if (text1.length() == 0) {
      // Just add some text (speedup).
      diffs.add(new Diff(Operation.INSERT, text2));
      return diffs;
    }

    if (text2.length() == 0) {
      // Just delete some text (speedup).
      diffs.add(new Diff(Operation.DELETE, text1));
      return diffs;
    }

    String longtext = text1.length() > text2.length() ? text1 : text2;
    String shorttext = text1.length() > text2.length() ? text2 : text1;
    int i = longtext.indexOf(shorttext);
    if (i != -1) {
      // Shorter text is inside the longer text (speedup).
      Operation op = (text1.length() > text2.length()) ? Operation.DELETE : Operation.INSERT;
      diffs.add(new Diff(op, longtext.substring(0, i)));
      diffs.add(new Diff(Operation.EQUAL, shorttext));
      diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
      return diffs;
    }

    if (shorttext.length() == 1) {
      // Single character string.
      // After the previous speedup, the character can't be an equality.
      diffs.add(new Diff(Operation.DELETE, text1));
      diffs.add(new Diff(Operation.INSERT, text2));
      return diffs;
    }

    // Check to see if the problem can be split in two.
    String[] hm = diff_halfMatch(text1, text2);
    if (hm != null) {
      // A half-match was found, sort out the return data.
      String text1_a = hm[0];
      String text1_b = hm[1];
      String text2_a = hm[2];
      String text2_b = hm[3];
      String mid_common = hm[4];
      // Send both pairs off for separate processing.
      LinkedList<Diff> diffs_a = diff_main(text1_a, text2_a, checklines, deadline);
      LinkedList<Diff> diffs_b = diff_main(text1_b, text2_b, checklines, deadline);
      // Merge the results.
      diffs = diffs_a;
      diffs.add(new Diff(Operation.EQUAL, mid_common));
      diffs.addAll(diffs_b);
      return diffs;
    }

    if (checklines && text1.length() > 100 && text2.length() > 100) {
      return diff_lineMode(text1, text2, deadline);
    }

    return diff_bisect(text1, text2, deadline);
  }

  //Do a quick line-level diff on both strings, then rediff the parts for greater accuracy. This speedup can produce non-minimal diffs.
  @NonNull
  private LinkedList<Diff> diff_lineMode(String text1, String text2, long deadline) {
    // Scan the text on a line-by-line basis first.
    LinesToCharsResult a = diff_linesToChars(text1, text2);
    text1 = a.chars1;
    text2 = a.chars2;
    List<String> linearray = a.lineArray;

    LinkedList<Diff> diffs = diff_main(text1, text2, false, deadline);

    // Convert the diff back to original text.
    diff_charsToLines(diffs, linearray);
    // Eliminate freak matches (e.g. blank lines)
    diff_cleanupSemantic(diffs);

    // Rediff any replacement blocks, this time character-by-character.
    // Add a dummy entry at the end.
    diffs.add(new Diff(Operation.EQUAL, ""));
    int count_delete = 0;
    int count_insert = 0;
    String text_delete = "";
    String text_insert = "";
    ListIterator<Diff> pointer = diffs.listIterator();
    Diff thisDiff = pointer.next();
    while (thisDiff != null) {
      switch (thisDiff.operation) {
      case INSERT:
        count_insert++;
        text_insert += thisDiff.text;
        break;
      case DELETE:
        count_delete++;
        text_delete += thisDiff.text;
        break;
      case EQUAL:
        // Upon reaching an equality, check for prior redundancies.
        if (count_delete >= 1 && count_insert >= 1) {
          // Delete the offending records and add the merged ones.
          pointer.previous();
          for (int j = 0; j < count_delete + count_insert; j++) {
            pointer.previous();
            pointer.remove();
          }
          for (Diff subDiff : diff_main(text_delete, text_insert, false, deadline)) {
            pointer.add(subDiff);
          }
        }
        count_insert = 0;
        count_delete = 0;
        text_delete = "";
        text_insert = "";
        break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    diffs.removeLast();

    return diffs;
  }

  //Find the 'middle snake' of a diff, split the problem in two  and return the recursively constructed diff.
  protected LinkedList<Diff> diff_bisect(@NonNull String text1, @NonNull String text2, long deadline) {
    // Cache the text lengths to prevent multiple calls.
    int text1_length = text1.length();
    int text2_length = text2.length();
    int max_d = (text1_length + text2_length + 1) / 2;
    int v_offset = max_d;
    int v_length = 2 * max_d;
    int[] v1 = new int[v_length];
    int[] v2 = new int[v_length];
    for (int x = 0; x < v_length; x++) {
      v1[x] = -1;
      v2[x] = -1;
    }
    v1[v_offset + 1] = 0;
    v2[v_offset + 1] = 0;
    int delta = text1_length - text2_length;
    // If the total number of characters is odd, then the front path will
    // collide with the reverse path.
    boolean front = (delta % 2 != 0);
    // Offsets for start and end of k loop.
    // Prevents mapping of space beyond the grid.
    int k1start = 0;
    int k1end = 0;
    int k2start = 0;
    int k2end = 0;
    for (int d = 0; d < max_d; d++) {
      // Bail out if deadline is reached.
      if (System.currentTimeMillis() > deadline) {
        break;
      }

      // Walk the front path one step.
      for (int k1 = -d + k1start; k1 <= d - k1end; k1 += 2) {
        int k1_offset = v_offset + k1;
        int x1;
        if (k1 == -d || (k1 != d && v1[k1_offset - 1] < v1[k1_offset + 1])) {
          x1 = v1[k1_offset + 1];
        } else {
          x1 = v1[k1_offset - 1] + 1;
        }
        int y1 = x1 - k1;
        while (x1 < text1_length && y1 < text2_length && text1.charAt(x1) == text2.charAt(y1)) {
          x1++;
          y1++;
        }
        v1[k1_offset] = x1;
        if (x1 > text1_length) {
          // Ran off the right of the graph.
          k1end += 2;
        } else if (y1 > text2_length) {
          // Ran off the bottom of the graph.
          k1start += 2;
        } else if (front) {
          int k2_offset = v_offset + delta - k1;
          if (k2_offset >= 0 && k2_offset < v_length && v2[k2_offset] != -1) {
            // Mirror x2 onto top-left coordinate system.
            int x2 = text1_length - v2[k2_offset];
            if (x1 >= x2) {
              // Overlap detected.
              return diff_bisectSplit(text1, text2, x1, y1, deadline);
            }
          }
        }
      }

      // Walk the reverse path one step.
      for (int k2 = -d + k2start; k2 <= d - k2end; k2 += 2) {
        int k2_offset = v_offset + k2;
        int x2;
        if (k2 == -d || (k2 != d && v2[k2_offset - 1] < v2[k2_offset + 1])) {
          x2 = v2[k2_offset + 1];
        } else {
          x2 = v2[k2_offset - 1] + 1;
        }
        int y2 = x2 - k2;
        while (x2 < text1_length && y2 < text2_length && text1.charAt(text1_length - x2 - 1) == text2.charAt(text2_length - y2 - 1)) {
          x2++;
          y2++;
        }
        v2[k2_offset] = x2;
        if (x2 > text1_length) {
          // Ran off the left of the graph.
          k2end += 2;
        } else if (y2 > text2_length) {
          // Ran off the top of the graph.
          k2start += 2;
        } else if (!front) {
          int k1_offset = v_offset + delta - k2;
          if (k1_offset >= 0 && k1_offset < v_length && v1[k1_offset] != -1) {
            int x1 = v1[k1_offset];
            int y1 = v_offset + x1 - k1_offset;
            // Mirror x2 onto top-left coordinate system.
            x2 = text1_length - x2;
            if (x1 >= x2) {
              // Overlap detected.
              return diff_bisectSplit(text1, text2, x1, y1, deadline);
            }
          }
        }
      }
    }
    // Diff took too long and hit the deadline or number of diffs equals number of characters.
    LinkedList<Diff> diffs = new LinkedList<Diff>();
    diffs.add(new Diff(Operation.DELETE, text1));
    diffs.add(new Diff(Operation.INSERT, text2));
    return diffs;
  }

  //Given the location of the 'middle snake', split the diff in two parts and recurse.
  @NonNull
  private LinkedList<Diff> diff_bisectSplit(@NonNull String text1, @NonNull String text2,
                                            int x, int y, long deadline) {
    String text1a = text1.substring(0, x);
    String text2a = text2.substring(0, y);
    String text1b = text1.substring(x);
    String text2b = text2.substring(y);

    // Compute both diffs serially.
    LinkedList<Diff> diffs = diff_main(text1a, text2a, false, deadline);
    LinkedList<Diff> diffsb = diff_main(text1b, text2b, false, deadline);

    diffs.addAll(diffsb);
    return diffs;
  }

  //Split two texts into a list of strings.  Reduce the texts to a string of hashes where each Unicode character represents one line.
  protected LinesToCharsResult diff_linesToChars(String text1, String text2) {
    List<String> lineArray = new ArrayList<String>();
    Map<String, Integer> lineHash = new HashMap<String, Integer>();
    // e.g. linearray[4] == "Hello\n"
    // e.g. linehash.get("Hello\n") == 4

   //inserting a junk entry to avoid generating a null character.
    lineArray.add("");

    // Allocate 2/3rds of the space for text1, the rest for text2.
    String chars1 = diff_linesToCharsMunge(text1, lineArray, lineHash, 40000);
    String chars2 = diff_linesToCharsMunge(text2, lineArray, lineHash, 65535);
    return new LinesToCharsResult(chars1, chars2, lineArray);
  }


  //Split a text into a list of strings.  Reduce the texts to a string of hashes where each Unicode character represents one line.
  @NonNull
  private String diff_linesToCharsMunge(@NonNull String text, List<String> lineArray,
                                        Map<String, Integer> lineHash, int maxLines) {
    int lineStart = 0;
    int lineEnd = -1;
    String line;
    StringBuilder chars = new StringBuilder();
    while (lineEnd < text.length() - 1) {
      lineEnd = text.indexOf('\n', lineStart);
      if (lineEnd == -1) {
        lineEnd = text.length() - 1;
      }
      line = text.substring(lineStart, lineEnd + 1);

      if (lineHash.containsKey(line)) {
        chars.append(String.valueOf((char) (int) lineHash.get(line)));
      } else {
        if (lineArray.size() == maxLines) {
          line = text.substring(lineStart);
          lineEnd = text.length();
        }
        lineArray.add(line);
        lineHash.put(line, lineArray.size() - 1);
        chars.append(String.valueOf((char) (lineArray.size() - 1)));
      }
      lineStart = lineEnd + 1;
    }
    return chars.toString();
  }

  //Rehydrate the text in a diff from a string of line hashes to real lines of text.
  protected void diff_charsToLines(@NonNull List<Diff> diffs,
                                   List<String> lineArray) {
    StringBuilder text;
    for (Diff diff : diffs) {
      text = new StringBuilder();
      for (int j = 0; j < diff.text.length(); j++) {
        text.append(lineArray.get(diff.text.charAt(j)));
      }
      diff.text = text.toString();
    }
  }

  //Determine the common prefix of two strings
  public int diff_commonPrefix(@NonNull String text1, @NonNull String text2) {
    int n = Math.min(text1.length(), text2.length());
    for (int i = 0; i < n; i++) {
      if (text1.charAt(i) != text2.charAt(i)) {
        return i;
      }
    }
    return n;
  }

  //Determine the common suffix of two strings
  public int diff_commonSuffix(@NonNull String text1, @NonNull String text2) {
    int text1_length = text1.length();
    int text2_length = text2.length();
    int n = Math.min(text1_length, text2_length);
    for (int i = 1; i <= n; i++) {
      if (text1.charAt(text1_length - i) != text2.charAt(text2_length - i)) {
        return i - 1;
      }
    }
    return n;
  }

  //Determine if the suffix of one string is the prefix of another.
  protected int diff_commonOverlap(@NonNull String text1, @NonNull String text2) {
    // Cache the text lengths to prevent multiple calls.
    int text1_length = text1.length();
    int text2_length = text2.length();
    // Eliminate the null case.
    if (text1_length == 0 || text2_length == 0) {
      return 0;
    }
    // Truncate the longer string.
    if (text1_length > text2_length) {
      text1 = text1.substring(text1_length - text2_length);
    } else if (text1_length < text2_length) {
      text2 = text2.substring(0, text1_length);
    }
    int text_length = Math.min(text1_length, text2_length);
    if (text1.equals(text2)) {
      return text_length;
    }

    // Start by looking for a single character match and increase length until no match is found.
    int best = 0;
    int length = 1;
    while (true) {
      String pattern = text1.substring(text_length - length);
      int found = text2.indexOf(pattern);
      if (found == -1) {
        return best;
      }
      length += found;
      if (found == 0 || text1.substring(text_length - length).equals(
          text2.substring(0, length))) {
        best = length;
        length++;
      }
    }
  }

  //Do the two texts share a substring which is at least half the length of the longer text? This speedup can produce non-minimal diffs.
  protected String[] diff_halfMatch(String text1, String text2) {
    if (Diff_Timeout <= 0) {
      return null;
    }
    String longtext = text1.length() > text2.length() ? text1 : text2;
    String shorttext = text1.length() > text2.length() ? text2 : text1;
    if (longtext.length() < 4 || shorttext.length() * 2 < longtext.length()) {
      return null;
    }

    // First check if the second quarter is the seed for a half-match.
    String[] hm1 = diff_halfMatchI(longtext, shorttext,
                                   (longtext.length() + 3) / 4);
    // Check again based on the third quarter.
    String[] hm2 = diff_halfMatchI(longtext, shorttext,
                                   (longtext.length() + 1) / 2);
    String[] hm;
    if (hm1 == null && hm2 == null) {
      return null;
    } else if (hm2 == null) {
      hm = hm1;
    } else if (hm1 == null) {
      hm = hm2;
    } else {
      // Both matched.  Select the longest.
      hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
    }

    // A half-match was found, sort out the return data.
    if (text1.length() > text2.length()) {
      return hm;
      //return new String[]{hm[0], hm[1], hm[2], hm[3], hm[4]};
    } else {
      return new String[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
    }
  }

  //Does a substring of shorttext exist within longtext such that the substring is at least half the length of longtext?
  @Nullable
  private String[] diff_halfMatchI(@NonNull String longtext, @NonNull String shorttext, int i) {
    // Start with a 1/4 length substring at position i as a seed.
    String seed = longtext.substring(i, i + longtext.length() / 4);
    int j = -1;
    String best_common = "";
    String best_longtext_a = "", best_longtext_b = "";
    String best_shorttext_a = "", best_shorttext_b = "";
    while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
      int prefixLength = diff_commonPrefix(longtext.substring(i), shorttext.substring(j));
      int suffixLength = diff_commonSuffix(longtext.substring(0, i), shorttext.substring(0, j));
      if (best_common.length() < suffixLength + prefixLength) {
        best_common = shorttext.substring(j - suffixLength, j) + shorttext.substring(j, j + prefixLength);
        best_longtext_a = longtext.substring(0, i - suffixLength);
        best_longtext_b = longtext.substring(i + prefixLength);
        best_shorttext_a = shorttext.substring(0, j - suffixLength);
        best_shorttext_b = shorttext.substring(j + prefixLength);
      }
    }
    if (best_common.length() * 2 >= longtext.length()) {
      return new String[]{best_longtext_a, best_longtext_b, best_shorttext_a, best_shorttext_b, best_common};
    } else {
      return null;
    }
  }

  //Reduce the number of edits by eliminating semantically trivial equalities.
  public void diff_cleanupSemantic(@NonNull LinkedList<Diff> diffs) {
    if (diffs.isEmpty()) {
      return;
    }
    boolean changes = false;
    Deque<Diff> equalities = new ArrayDeque<Diff>();
    String lastEquality = null;
    ListIterator<Diff> pointer = diffs.listIterator();
    // Number of characters that changed prior to the equality.
    int length_insertions1 = 0;
    int length_deletions1 = 0;
    // Number of characters that changed after the equality.
    int length_insertions2 = 0;
    int length_deletions2 = 0;
    Diff thisDiff = pointer.next();
    while (thisDiff != null) {
      if (thisDiff.operation == Operation.EQUAL) {
        // Equality found.
        equalities.push(thisDiff);
        length_insertions1 = length_insertions2;
        length_deletions1 = length_deletions2;
        length_insertions2 = 0;
        length_deletions2 = 0;
        lastEquality = thisDiff.text;
      } else {
        // An insertion or deletion.
        if (thisDiff.operation == Operation.INSERT) {
          length_insertions2 += thisDiff.text.length();
        } else {
          length_deletions2 += thisDiff.text.length();
        }
        // Eliminate an equality that is smaller or equal to the edits on both
        if (lastEquality != null && (lastEquality.length() <= Math.max(length_insertions1, length_deletions1))
                && (lastEquality.length() <= Math.max(length_insertions2, length_deletions2))) {
          while (thisDiff != equalities.peek()) {
            thisDiff = pointer.previous();
          }
          pointer.next();

          // Replace equality with a delete.
          pointer.set(new Diff(Operation.DELETE, lastEquality));
          // Insert a corresponding an insert.
          pointer.add(new Diff(Operation.INSERT, lastEquality));

          equalities.pop();
          if (!equalities.isEmpty()) {
            equalities.pop();
          }
          if (equalities.isEmpty()) {
            // There are no previous equalities, walk back to the start.
            while (pointer.hasPrevious()) {
              pointer.previous();
            }
          } else {
            thisDiff = equalities.peek();
            while (thisDiff != pointer.previous()) {

            }
          }

          length_insertions1 = 0;  // Reset the counters.
          length_insertions2 = 0;
          length_deletions1 = 0;
          length_deletions2 = 0;
          lastEquality = null;
          changes = true;
        }
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }

    // Normalize the diff.
    if (changes) {
      diff_cleanupMerge(diffs);
    }
    diff_cleanupSemanticLossless(diffs);

    // Find any overlaps between deletions and insertions.
    // e.g: <del>abcxxx</del><ins>xxxdef</ins>
    //   -> <del>abc</del>xxx<ins>def</ins>
    // e.g: <del>xxxabc</del><ins>defxxx</ins>
    //   -> <ins>def</ins>xxx<del>abc</del>
    // Only extract an overlap if it is as big as the edit ahead or behind it.
    pointer = diffs.listIterator();
    Diff prevDiff = null;
    thisDiff = null;
    if (pointer.hasNext()) {
      prevDiff = pointer.next();
      if (pointer.hasNext()) {
        thisDiff = pointer.next();
      }
    }
    while (thisDiff != null) {
      if (prevDiff.operation == Operation.DELETE &&
          thisDiff.operation == Operation.INSERT) {
        String deletion = prevDiff.text;
        String insertion = thisDiff.text;
        int overlap_length1 = this.diff_commonOverlap(deletion, insertion);
        int overlap_length2 = this.diff_commonOverlap(insertion, deletion);
        if (overlap_length1 >= overlap_length2) {
          if (overlap_length1 >= deletion.length() / 2.0 || overlap_length1 >= insertion.length() / 2.0) {
            // Overlap found. Insert an equality and trim the surrounding edits.
            pointer.previous();
            pointer.add(new Diff(Operation.EQUAL, insertion.substring(0, overlap_length1)));
            prevDiff.text = deletion.substring(0, deletion.length() - overlap_length1);
            thisDiff.text = insertion.substring(overlap_length1);
          }
        } else {
          if (overlap_length2 >= deletion.length() / 2.0 ||
              overlap_length2 >= insertion.length() / 2.0) {
            // Reverse overlap found.
            // Insert an equality and swap and trim the surrounding edits.
            pointer.previous();
            pointer.add(new Diff(Operation.EQUAL, deletion.substring(0, overlap_length2)));
            prevDiff.operation = Operation.INSERT;
            prevDiff.text = insertion.substring(0, insertion.length() - overlap_length2);
            thisDiff.operation = Operation.DELETE;
            thisDiff.text = deletion.substring(overlap_length2);
          }
        }
        thisDiff = pointer.hasNext() ? pointer.next() : null;
      }
      prevDiff = thisDiff;
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
  }

  //Look for single edits surrounded on both sides by equalities which can be shifted sideways to align the edit to a word boundary.
  //e.g: The c<ins>at c</ins>ame. -> The <ins>cat </ins>came
  public void diff_cleanupSemanticLossless(@NonNull LinkedList<Diff> diffs) {
    String equality1, edit, equality2;
    String commonString;
    int commonOffset;
    int score, bestScore;
    String bestEquality1, bestEdit, bestEquality2;
    // Create a new iterator at the start.
    ListIterator<Diff> pointer = diffs.listIterator();
    Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
    Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
    Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
    while (nextDiff != null) {
      if (prevDiff.operation == Operation.EQUAL && nextDiff.operation == Operation.EQUAL) {
        equality1 = prevDiff.text;
        edit = thisDiff.text;
        equality2 = nextDiff.text;

        commonOffset = diff_commonSuffix(equality1, edit);
        if (commonOffset != 0) {
          commonString = edit.substring(edit.length() - commonOffset);
          equality1 = equality1.substring(0, equality1.length() - commonOffset);
          edit = commonString + edit.substring(0, edit.length() - commonOffset);
          equality2 = commonString + equality2;
        }

        // Second, step character by character right, looking for the best fit.
        bestEquality1 = equality1;
        bestEdit = edit;
        bestEquality2 = equality2;
        bestScore = diff_cleanupSemanticScore(equality1, edit) + diff_cleanupSemanticScore(edit, equality2);
        while (edit.length() != 0 && equality2.length() != 0 && edit.charAt(0) == equality2.charAt(0)) {
          equality1 += edit.charAt(0);
          edit = edit.substring(1) + equality2.charAt(0);
          equality2 = equality2.substring(1);
          score = diff_cleanupSemanticScore(equality1, edit) + diff_cleanupSemanticScore(edit, equality2);
          // The >= encourages trailing rather than leading whitespace on edits.
          if (score >= bestScore) {
            bestScore = score;
            bestEquality1 = equality1;
            bestEdit = edit;
            bestEquality2 = equality2;
          }
        }

        if (!prevDiff.text.equals(bestEquality1)) {
          if (bestEquality1.length() != 0) {
            prevDiff.text = bestEquality1;
          } else {
            pointer.previous(); // Walk past nextDiff.
            pointer.previous(); // Walk past thisDiff.
            pointer.previous(); // Walk past prevDiff.
            pointer.remove(); // Delete prevDiff.
            pointer.next(); // Walk past thisDiff.
            pointer.next(); // Walk past nextDiff.
          }
          thisDiff.text = bestEdit;
          if (bestEquality2.length() != 0) {
            nextDiff.text = bestEquality2;
          } else {
            pointer.remove(); // Delete nextDiff.
            nextDiff = thisDiff;
            thisDiff = prevDiff;
          }
        }
      }
      prevDiff = thisDiff;
      thisDiff = nextDiff;
      nextDiff = pointer.hasNext() ? pointer.next() : null;
    }
  }

  //Given two strings, compute a score representing whether the internal boundary falls on logical boundaries.
  //Scores range from 6 (best) to 0 (worst).
  private int diff_cleanupSemanticScore(@NonNull String one, String two) {
    if (one.length() == 0 || two.length() == 0) {
      return 6;
    }

    // Each port of this function behaves slightly differently due to subtle differences in each language's definition of things like 'whitespace'
    char char1 = one.charAt(one.length() - 1);
    char char2 = two.charAt(0);
    boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
    boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
    boolean whitespace1 = nonAlphaNumeric1 && Character.isWhitespace(char1);
    boolean whitespace2 = nonAlphaNumeric2 && Character.isWhitespace(char2);
    boolean lineBreak1 = whitespace1 && Character.getType(char1) == Character.CONTROL;
    boolean lineBreak2 = whitespace2 && Character.getType(char2) == Character.CONTROL;
    boolean blankLine1 = lineBreak1 && BLANKLINEEND.matcher(one).find();
    boolean blankLine2 = lineBreak2 && BLANKLINESTART.matcher(two).find();

    if (blankLine1 || blankLine2) {
      return 5;
    } else if (lineBreak1 || lineBreak2) {
      return 4;
    } else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
      return 3;
    } else if (whitespace1 || whitespace2) {
      return 2;
    } else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
      return 1;
    }
    return 0;
  }

  // Define some regex patterns for matching boundaries.
  private Pattern BLANKLINEEND = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
  private Pattern BLANKLINESTART = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);


  //Reorder and merge like edit sections.  Merge equalities. Any edit section can move as long as it doesn't cross an equality.
  public void diff_cleanupMerge(@NonNull LinkedList<Diff> diffs) {
    diffs.add(new Diff(Operation.EQUAL, ""));
    ListIterator<Diff> pointer = diffs.listIterator();
    int count_delete = 0;
    int count_insert = 0;
    String text_delete = "";
    String text_insert = "";
    Diff thisDiff = pointer.next();
    Diff prevEqual = null;
    int commonlength;
    while (thisDiff != null) {
      switch (thisDiff.operation) {
      case INSERT:
        count_insert++;
        text_insert += thisDiff.text;
        prevEqual = null;
        break;
      case DELETE:
        count_delete++;
        text_delete += thisDiff.text;
        prevEqual = null;
        break;
      case EQUAL:
        if (count_delete + count_insert > 1) {
          boolean both_types = count_delete != 0 && count_insert != 0;
          // Delete the offending records.
          pointer.previous();  // Reverse direction.
          while (count_delete-- > 0) {
            pointer.previous();
            pointer.remove();
          }
          while (count_insert-- > 0) {
            pointer.previous();
            pointer.remove();
          }
          if (both_types) {
            // Factor out any common prefixes.
            commonlength = diff_commonPrefix(text_insert, text_delete);
            if (commonlength != 0) {
              if (pointer.hasPrevious()) {
                thisDiff = pointer.previous();
                assert thisDiff.operation == Operation.EQUAL : "Previous diff should have been an equality.";
                thisDiff.text += text_insert.substring(0, commonlength);
                pointer.next();
              } else {
                pointer.add(new Diff(Operation.EQUAL, text_insert.substring(0, commonlength)));
              }
              text_insert = text_insert.substring(commonlength);
              text_delete = text_delete.substring(commonlength);
            }
            // Factor out any common suffixes.
            commonlength = diff_commonSuffix(text_insert, text_delete);
            if (commonlength != 0) {
              thisDiff = pointer.next();
              thisDiff.text = text_insert.substring(text_insert.length() - commonlength) + thisDiff.text;
              text_insert = text_insert.substring(0, text_insert.length() - commonlength);
              text_delete = text_delete.substring(0, text_delete.length() - commonlength);
              pointer.previous();
            }
          }
          // Insert the merged records.
          if (text_delete.length() != 0) {
            pointer.add(new Diff(Operation.DELETE, text_delete));
          }
          if (text_insert.length() != 0) {
            pointer.add(new Diff(Operation.INSERT, text_insert));
          }
          // Step forward to the equality.
          thisDiff = pointer.hasNext() ? pointer.next() : null;
        } else if (prevEqual != null) {
          // Merge this equality with the previous one.
          prevEqual.text += thisDiff.text;
          pointer.remove();
          thisDiff = pointer.previous();
          pointer.next();
        }
        count_insert = 0;
        count_delete = 0;
        text_delete = "";
        text_insert = "";
        prevEqual = thisDiff;
        break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    if (diffs.getLast().text.length() == 0) {
      diffs.removeLast();
    }

    boolean changes = false;
    //new iterator at the start.
    pointer = diffs.listIterator();
    Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
    thisDiff = pointer.hasNext() ? pointer.next() : null;
    Diff nextDiff = pointer.hasNext() ? pointer.next() : null;
    while (nextDiff != null) {
      assert prevDiff != null;
      if (prevDiff.operation == Operation.EQUAL &&
          nextDiff.operation == Operation.EQUAL) {
        assert thisDiff != null;
        if (thisDiff.text.endsWith(prevDiff.text)) {
          thisDiff.text = prevDiff.text + thisDiff.text.substring(0, thisDiff.text.length() - prevDiff.text.length());
          nextDiff.text = prevDiff.text + nextDiff.text;
          pointer.previous(); // Walk past nextDiff.
          pointer.previous(); // Walk past thisDiff.
          pointer.previous(); // Walk past prevDiff.
          pointer.remove(); // Delete prevDiff.
          pointer.next(); // Walk past thisDiff.
          thisDiff = pointer.next(); // Walk past nextDiff.
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        } else if (thisDiff.text.startsWith(nextDiff.text)) {
          prevDiff.text += nextDiff.text;
          thisDiff.text = thisDiff.text.substring(nextDiff.text.length()) + nextDiff.text;
          pointer.remove();
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        }
      }
      prevDiff = thisDiff;
      thisDiff = nextDiff;
      nextDiff = pointer.hasNext() ? pointer.next() : null;
    }
    // If shifts were made, the diff needs reordering and another shift sweep.
    if (changes) {
      diff_cleanupMerge(diffs);
    }
  }

}
