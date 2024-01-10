package sort;

import java.util.Arrays;

public interface OtherSorts {

	static <T extends Comparable<T>> void mergeSort(T[] arr) {
		mergeSortHelper(arr, arr.length);
	}

	private static <T extends Comparable<T>> void mergeSortHelper(T[] arr,
	                                                              int n)
	{
		if (n < 2) {
			return;
		}
		final int mid = n >>> 1;
		T[] left = Arrays.copyOfRange(arr, 0, mid);
		T[] right = Arrays.copyOfRange(arr, mid, n);
		mergeSortHelper(left, mid);
		mergeSortHelper(right, n - mid);
		merge(arr, left, right, mid, n - mid);
	}

	private static <T extends Comparable<T>> void merge(T[] arr, T[] left,
	                                                    T[] right, int l,
	                                                    int r)
	{
		int i = 0, j = 0, k = 0;
		while (i < l && j < r) {
			if (left[i].compareTo(right[j]) <= 0) {
				arr[k++] = left[i++];
			} else {
				arr[k++] = right[j++];
			}
		}
		while (i < l) {
			arr[k++] = left[i++];
		}
		while (j < r) {
			arr[k++] = right[j++];
		}
	}

	static <T extends Comparable<T>> void quickSort(T[] arr) {
		quickSortHelper(arr, 0, arr.length - 1);
	}

	private static <T extends Comparable<T>> void quickSortHelper(T[] arr,
	                                                              int start,
	                                                              int end)
	{
		if (start < end) {
			int index = partition(arr, start, end);

			quickSortHelper(arr, start, index - 1);
			quickSortHelper(arr, index + 1, end);
		}
	}

	private static <T extends Comparable<T>> int partition(T[] arr, int start,
	                                                       int end)
	{
		T pivot = arr[end];
		int i = start - 1;

		for (int j = start; j < end; j++) {
			if (arr[j].compareTo(pivot) <= 0) {
				T temp = arr[++i];
				arr[i] = arr[j];
				arr[j] = temp;
			}
		}

		T temp = arr[++i];
		arr[i] = arr[end];
		arr[end] = temp;
		return i;
	}
}
