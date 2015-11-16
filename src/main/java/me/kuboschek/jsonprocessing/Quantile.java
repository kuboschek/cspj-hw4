package me.kuboschek.jsonprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author rubin
 */
public class Quantile {

    private long sum;
    private long min, max;
    private ArrayList<Long> ls;
    private boolean isSorted;

    public Quantile() {
        sum = 0;
        // Initialise the min and max with values that will always change
        // after we insert the first element
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        ls = new ArrayList<>();
    }

    // Adds a number to this quantile
    public void addNumber(long newNumber) {
        ls.add(newNumber);
        sum += newNumber;
        if (newNumber < min) {
            min = newNumber;
        }
        if (newNumber > max) {
            max = newNumber;
        }
        isSorted = false;
    }

    // Relevant Getters
    public long getSize() {
        return ls.size();
    }

    public long getSum() {
        return sum;
    }

    public long getMin() {
        if (ls.isEmpty()) {
            return 0;
        }
        return min;
    }

    public long getMax() {
        if (ls.isEmpty()) {
            return 0;
        }
        return max;
    }

    public long getMedian() {
        if (!isSorted) {
            Collections.sort(ls);
            isSorted = true;
        }
        int size = ls.size();
        if (size == 0) {
            return 0;
        } else if (size == 1) {
            return ls.get(0);
        }

        if (size % 2 == 0) {
            int firstMiddle = size / 2 - 1;
            int secondMiddle = size / 2;
            // CHECK: Rounding down here
            return ((ls.get(firstMiddle) + ls.get(secondMiddle)) / 2);
        } else {
            return ls.get((size - 1) / 2);
        }
    }

    // For the quantiles we use the definition of Tuckey's hinges.
    public long getFirstQuantile() {
        if (!isSorted) {
            Collections.sort(ls);
            isSorted = true;
        }
        int size = ls.size();
        if (size == 0 || size == 1) {
            return 0;
        }

        int halfSize;
        if (size % 2 == 0) {
            halfSize = size / 2;
        } else {
            halfSize = (size - 1) / 2 + 1;
        }

        if (halfSize % 2 == 0) {
            int firstMiddle = halfSize / 2 - 1;
            int secondMiddle = halfSize / 2;
            return ((ls.get(firstMiddle) + ls.get(secondMiddle)) / 2);
        } else {
            return ls.get((halfSize - 1) / 2);
        }
    }

    public long getThirdQuantile() {
        // Reverse the list and than find first quantile of the reversed list
        // which is the third quantile of the normal list
        Collections.sort(ls, new Comparator<Long>() {
            @Override
            public int compare(Long l1, Long l2) {
                return l2.compareTo(l1);
            }
        });
        isSorted = false;

        int size = ls.size();
        if (size == 0 || size == 1) {
            return 0;
        }

        int halfSize;
        if (size % 2 == 0) {
            halfSize = size / 2;
        } else {
            halfSize = (size - 1) / 2 + 1;
        }

        if (halfSize % 2 == 0) {
            int firstMiddle = halfSize / 2 - 1;
            int secondMiddle = halfSize / 2;
            // CHECK: Rounding down here
            return ((ls.get(firstMiddle) + ls.get(secondMiddle)) / 2);
        } else {
            return ls.get((halfSize - 1) / 2);
        }
    }
}
