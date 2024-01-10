package sort;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static java.lang.Math.min;
public interface SimpleTimSort {
	int THRESHOLD = 32;

	private static <T> int runLength(T[] array) {
		int runLength = array.length;
		int remainder = 0;
		while (runLength >= THRESHOLD) {
			remainder |= runLength & 1; // runLength is even ? 0 : 1
			runLength >>>= 1; // divides runLength by 2
		}
		return runLength + remainder;
	}

	private static <T extends Comparable<T>> void binarySort(T[] array,
	                                                         int start,
	                                                         int end)
	{
		for (int i = start + 1; i <= end; i++) {
			final T target = array[i];
			final int index = binarySearch(array, start, i, target);
			for (int j = i; j > index; j--) {
				array[j] = array[j - 1];
			}
			array[index] = target;
		}
	}

	private static <T extends Comparable<T>> int binarySearch(T[] array,
	                                                          int start,
	                                                          int end,
	                                                          T target)
	{
		int left = start;
		int right = end;
		while (left < right) {
			final int mid = (left + right) >>> 1;
			if (array[mid].compareTo(target) <= 0) {
				left = mid + 1;
			} else {
				right = mid;
			}
		}
		return left;
	}

	private static <T extends Comparable<T>> void merge(T[] array, int start,
	                                                    int mid, int end)
	{
		T[] left = Arrays.copyOfRange(array, start, mid + 1);
		T[] right = Arrays.copyOfRange(array, mid + 1, end + 1);
		int leftIndex = 0;
		int rightIndex = 0;
		while (leftIndex < left.length && rightIndex < right.length) {
			final T leftValue = left[leftIndex];
			final T rightValue = right[rightIndex];
			if (leftValue.compareTo(rightValue) < 0) {
				array[start + leftIndex + rightIndex] = leftValue;
				leftIndex++;
			} else {
				array[start + leftIndex + rightIndex] = rightValue;
				rightIndex++;
			}
		}
		for (int i = leftIndex; i < left.length; i++) {
			array[start + i + rightIndex] = left[i];
		}
		for (int i = rightIndex; i < right.length; i++) {
			array[start + leftIndex + i] = right[i];
		}
	}

	static <T extends Comparable<T>> void sort(T[] array) {
		final int runLength = runLength(array);
		for (int start = 0; start <= array.length; start += runLength) {
			final int end = min(array.length - 1, start + runLength - 1);
			binarySort(array, start, end);
		}
		for (int size = runLength; size < array.length; size <<= 1) {
			for (int left = 0; left <= array.length; left += size << 1) {
				final int mid = left + size - 1;
				final int right = min(array.length - 1, left + 2 * size - 1);
				if (mid < right) {
					merge(array, left, mid, right);
				}
			}
		}
	}

	static <T extends Comparable<T>> void parallelSort(T[] array) {
		final int length = array.length;
		final int runLength = runLength(array);
		IntStream.rangeClosed(0, length / runLength)
		         .parallel()
		         .map(i -> i * runLength)
		         .forEach(i -> binarySort(array, i, min(length - 1,
		                                  i + runLength - 1)));
		for (int size = runLength; size < length; size <<= 1) {
			final int mergeSize = size;
			IntConsumer merge = left -> {
				final int mid = left + mergeSize - 1;
				final int right = min(length - 1, left + 2 * mergeSize - 1);
				if (mid < right) {
					merge(array, left, mid, right);
				}
			};
			IntStream.rangeClosed(0, length / (mergeSize << 1))
			         .parallel()
			         .map(i -> i * mergeSize << 1)
			         .forEach(merge);
		}
	}

	static <T extends Comparable<T>> T[] sorted(T[] array) {
		T[] output = Arrays.copyOf(array, array.length);
		sort(output);
		return output;
	}

	static <T extends Comparable<T>> T[] parallelSorted(T[] array) {
		T[] output = Arrays.copyOf(array, array.length);
		parallelSort(output);
		return output;
	}
}