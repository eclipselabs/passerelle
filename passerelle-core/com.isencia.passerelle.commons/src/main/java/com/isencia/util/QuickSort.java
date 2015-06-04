/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.util;

/**
 * QuickSort - adapted from Doug Lea's Public Domain collection library.
 * 
 * @author <a href="mailto:mbryson@mindspring.com">Dave Bryson</a>
 */
public class QuickSort {
  /**
   * Sort array of Objects using the QuickSort algorithm.
   * 
   * @param s A Comparable[].
   * @param lo The current lower bound.
   * @param hi The current upper bound.
   */
  public static void quickSort(Comparable s[], int lo, int hi) {
    if (lo >= hi) return;

    /*
     * Use median-of-three(lo, mid, hi) to pick a partition. Also swap them into
     * relative order while we are at it.
     */
    int mid = (lo + hi) / 2;

    if (s[lo].compareTo(s[mid]) > 0) {
      // Swap.
      Comparable tmp = s[lo];
      s[lo] = s[mid];
      s[mid] = tmp;
    }

    if (s[mid].compareTo(s[hi]) > 0) {
      // Swap .
      Comparable tmp = s[mid];
      s[mid] = s[hi];
      s[hi] = tmp;

      if (s[lo].compareTo(s[mid]) > 0) {
        // Swap.
        Comparable tmp2 = s[lo];
        s[lo] = s[mid];
        s[mid] = tmp2;
      }
    }

    // Start one past lo since already handled lo.
    int left = lo + 1;

    // Similarly, end one before hi since already handled hi.
    int right = hi - 1;

    // If there are three or fewer elements, we are done.
    if (left >= right) return;

    Comparable partition = s[mid];

    for (;;) {
      while (s[right].compareTo(partition) > 0)
        --right;

      while (left < right && s[left].compareTo(partition) <= 0)
        ++left;

      if (left < right) {
        // Swap.
        Comparable tmp = s[left];
        s[left] = s[right];
        s[right] = tmp;

        --right;
      } else
        break;
    }
    quickSort(s, lo, left);
    quickSort(s, left + 1, hi);
  }

  /**
   * Sorts and array of objects.
   * 
   * @param data An Object[].
   */
  public void sort(Comparable[] data) {
    QuickSort.quickSort(data, 0, data.length - 1);
  }
}
