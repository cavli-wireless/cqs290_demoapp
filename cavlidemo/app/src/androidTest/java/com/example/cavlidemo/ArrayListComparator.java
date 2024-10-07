package com.example.cavlidemo;
import java.util.ArrayList;
public class ArrayListComparator {

    public static ComparisonResult compareByteLists(ArrayList<Byte> list1, ArrayList<Byte> list2) {
        ComparisonResult result = new ComparisonResult();

        int minLength = Math.min(list1.size(), list2.size());
        for (int i = 0; i < minLength; i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                result.differenceCount++;
                if (result.firstDifferenceIndex == -1) {
                    result.firstDifferenceIndex = i;
                }
            }
        }

        // Check for remaining elements in the longer list
        result.differenceCount += Math.abs(list1.size() - list2.size());

        return result;
    }

    public static class ComparisonResult {
        public int differenceCount = 0;
        public int firstDifferenceIndex = -1;
    }
}