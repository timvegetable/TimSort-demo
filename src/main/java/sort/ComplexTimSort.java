package sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Math.abs;
import static java.lang.Math.min;
public interface ComplexTimSort {
	int THRESHOLD = 32;
	int MIN_GALLOP = 7;

	/**
	 * Sorts an array of comparable objects.
	 * @param <T> the type of the array
	 * @param arr the array to be sorted
	 */
	static <T extends Comparable<T>> void sort(T[] arr) {
		sort(arr, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	/**
	 * Sorts an array using a given comparator.
	 * @param <T> the type of the array
	 * @param arr the array to be sorted
	 * @param comp the comparator used to sort the array
	 */
	static <T> void sort(T[] arr, Comparator<T> comp) {
		int minGallop = MIN_GALLOP;
		int lo = 0;
		final int hi = arr.length;
		int remaining = hi;
		final int minRun = runLength(arr);
		final ArrayList<int[]> stack = new ArrayList<>(hi / minRun + 1);

		if (remaining < THRESHOLD) {
			final int startingRunLength = ascendThenCount(arr, comp, lo, hi);
			binarySort(arr, comp, lo, hi, lo + startingRunLength);
			return;
		}

		while (remaining > 0) {
			int runLength = ascendThenCount(arr, comp, lo, hi);
			if (runLength < minRun) {
				final int force = min(remaining, minRun);
				binarySort(arr, comp, lo, lo + force, lo + runLength);
				runLength = force;
			}
			stack.add(new int[]{lo, runLength});
			minGallop = mergeCollapse(arr, stack, comp, minGallop);
			lo += runLength;
			remaining -= runLength;
		}

		mergeForceCollapse(arr, stack, comp, minGallop);
	}

	/**
	 * Calculates a run length for the array that is effectively equivalent to
	 * ceil(length / (2 ^ (ceil(log2(length + 0.001)) - 2)))
	 * @param <T> the type of the array
	 * @param arr the array whose run length is to be calculated
	 * @return the calculated run length
	 */
	private static <T> int runLength(T[] arr) {
		int runLength = arr.length;
		int remainder = 0;
		while (runLength >= THRESHOLD) {
			remainder |= runLength & 1;
			runLength >>>= 1;
		}
		return runLength + remainder;
	}

	/**
	 * Counts the length of the ascending or descending run starting at the
	 * left index and returns the count. If the elements are in descending
	 * order, this reverses the run to make it ascending after counting.
	 * @param <T> the type of the array
	 * @param arr the array being worked with
	 * @param comp the comparator used to sort the array
	 * @param left the starting index of the run, inclusive
	 * @param right the maximum index of the run, exclusive
	 * @return the length of the run
	 */
	private static <T> int ascendThenCount(T[] arr, Comparator<T> comp,
	                                       int left, int right)
	{
		int runRight = left + 1;
		if (runRight == right) {
			return 1;
		}
		if (comp.compare(arr[left],arr[runRight]) > 0) {
			while (runRight < right
			       && comp.compare(arr[runRight], arr[runRight - 1]) < 0)
			{
				runRight++;
			}
			reverse(arr, left, runRight);
		} else {
			while (runRight < right
			       && comp.compare(arr[runRight], arr[runRight - 1]) >= 0)
			{
				runRight++;
			}
		}
		return runRight - left;
	}

	/**
	 * Reverses the order of a descending run
	 * @param <T> the type of the array
	 * @param arr the array being worked with
	 * @param left the starting index of the descending run, inclusive
	 * @param right the ending index of the descending run, exclusive
	 */
	private static <T> void reverse(T[] arr, int left, int right) {
		right--;
		while (left < right) {
			T temp = arr[left];
			arr[left] = arr[right];
			arr[right] = temp;
			left++;
			right--;
		}
	}

	private static <T> void binarySort(T[] arr, Comparator<T> comp, int lo,
	                                   int hi, int start)
	{
		if (start == lo) {
			start++;
		}
		while (start < hi) {
			final T pivot = arr[start];
			final int index =
			abs(binarySearch(arr, comp, lo, start, pivot) + 1);
			System.arraycopy(arr, index, arr, index + 1, start - index);
			arr[index] = pivot;
			start++;
		}
	}

	private static <T> int binarySearch(T[] arr, Comparator<T> comp, int base,
	                                    int end, T target)
	{
		int left = base;
		int right = end - 1;
		while (left <= right) {
			final int mid = (left + right) >>> 1;
			final T middle = arr[mid];
			final int compare = comp.compare(middle, target);
			if (compare < 0) {
				left = mid + 1;
			} else if (compare > 0) {
				right = mid - 1;
			} else {
				return mid;
			}
		}
		return -(left + 1);
	}

	private static <T> int searchLeft(T[] arr, Comparator<T> comp, int base,
	                                  int length, T target)
	{
		int left = base;
		int right = base + length;
		while (left < right) {
			final int mid = (left + right) >>> 1;
			if (comp.compare(target, arr[mid]) > 0) {
				left = mid + 1;
			} else {
				right = mid;
			}
		}
		return left - base;
	}

	private static <T> int searchRight(T[] arr, Comparator<T> comp, int base,
	                                   int length, T target)
	{
		int left = base;
		int right = length;
		while (left < right) {
			final int mid = (left + right) >>> 1;
			if (comp.compare(target, arr[mid]) < 0) {
				right = mid;
			} else {
				left = mid + 1;
			}
		}
		return left - base;
	}

	private static <T> int gallopLeft(T[] arr, Comparator<T> comp, int base,
	                                  int length, int hint, T target)
	{
		int lastOffset = 0;
		int offset = 1;
		if (comp.compare(target, arr[base + hint]) > 0) {
			final int maxOffset = length - hint;
			while (offset < maxOffset
			       && comp.compare(target, arr[base + hint + offset]) > 0)
			{
				lastOffset = offset;
				offset = (offset << 1) + 1; // offset * 2 + 1
			}

			if (offset > maxOffset) {
				offset = maxOffset;
			}

			lastOffset += hint;
			offset += hint;
		} else {
			final int maxOffset = hint + 1;
			while (offset < maxOffset
			       && comp.compare(target, arr[base + hint - offset]) <= 0)
			{
				lastOffset = offset;
				offset = (offset << 1) + 1; // offset * 2 + 1
			}

			if (offset > maxOffset) {
				offset = maxOffset;
			}

			final int temp = lastOffset;
			lastOffset = hint - offset;
			offset = hint - temp;
		}

		lastOffset++;
		while (lastOffset < offset) {
			final int mid = (lastOffset + offset) >>> 1;
			if (comp.compare(target, arr[base + mid]) > 0) {
				lastOffset = mid + 1;
			} else {
				offset = mid;
			}
		}
		return offset;
	}

	private static <T> int gallopRight(T[] arr, Comparator<T> comp, int base,
	                                   int length, int hint, T target)
	{
		int lastOffset = 0;
		int offset = 1;
		if (comp.compare(target, arr[base + hint]) < 0) {
			final int maxOffset = hint + 1;
			while (offset < maxOffset
			       && comp.compare(target, arr[base + hint - offset]) < 0)
			{
				lastOffset = offset;
				offset = (offset << 1) + 1; // offset * 2 + 1
			}

			if (offset > maxOffset) {
				offset = maxOffset;
			}

			final int temp = lastOffset;
			lastOffset = hint - offset;
			offset = hint - temp;
		} else {
			final int maxOffset = length - hint;
			while (offset < maxOffset
			       && comp.compare(target, arr[base + hint + offset]) >= 0)
			{
				lastOffset = offset;
				offset = (offset << 1) + 1; // offset * 2 + 1
			}

			if (offset > maxOffset) {
				offset = maxOffset;
			}

			lastOffset += hint;
			offset += hint;
		}
		lastOffset++;
		while (lastOffset < offset) {
			final int mid = (lastOffset + offset) >>> 1;
			if (comp.compare(target, arr[base + mid]) < 0) {
				offset = mid;
			} else {
				lastOffset = mid + 1;
			}
		}
		return offset;
	}

	private static <T> int mergeLo(T[] arr, Comparator<T> comp, int l, int m,
	                                int r, int minGallop)
	{
		final T[] temp = Arrays.copyOfRange(arr, l, m);
		int i = l, j = m, k = 0;
		boolean done = false;

		while (!done) {
			int count1 = 0;
			int count2 = 0;
			while ((count1 | count2) < minGallop) {
				if (comp.compare(temp[k], arr[j]) < 0) {
					arr[i] = temp[k];
					count1++;
					count2 = 0;
					k++;
				} else {
					arr[i] = arr[j];
					count1 = 0;
					count2++;
					j++;
				}
				i++;

				if (k == m - l || j == r) {
					done = true;
					break;
				}
			}

			if (done) {
				break;
			}

			while (count1 >= MIN_GALLOP || count2 >= MIN_GALLOP) {
				count1 = gallopRight(temp, comp, k, m - l - k, 0, arr[j]);
				if (count1 != 0) {
					System.arraycopy(temp, k, arr, i, count1);
					i += count1;
					k += count1;
					if (k == m - l) {
						done = true;
						break;
					}
				}

				arr[i] = arr[j];
				i++;
				j++;
				if (j == r) {
					done = true;
					break;
				}

				count2 = gallopLeft(arr, comp, j, r - j, 0, temp[k]);
				if (count2 != 0) {
					System.arraycopy(arr, j, arr, i, count2);
					i += count2;
					j += count2;
					if (j == r) {
						done = true;
						break;
					}
				}

				arr[i] = temp[k];
				i++;
				k++;
				if (k == m - l) {
					done = true;
					break;
				}

				minGallop--;
			}
			if (minGallop < 0) {
				minGallop = 0;
			}
			minGallop += 2;
		}

		if (k < m - l) {
			System.arraycopy(temp, k, arr, i, m - l - k);
		}
		return minGallop;
	}

	private static <T> int mergeHi(T[] arr, Comparator<T> comp, int l, int m,
	                                int r, int minGallop)
	{
		final T[] temp = Arrays.copyOfRange(arr, m, r);
		int i = r - 1, j = m - 1, k = r - m - 1;
		boolean done = false;

		while (!done) {
			int count1 = 0;
			int count2 = 0;
			while ((count1 | count2) < minGallop) {
				if (comp.compare(temp[k], arr[j]) > 0) {
					arr[i] = temp[k];
					count1++;
					count2 = 0;
					k--;
				} else {
					arr[i] = arr[j];
					count1 = 0;
					count2++;
					j--;
				}
				i--;

				if (k == -1 || j == l - 1) {
					done = true;
					break;
				}
			}

			if (done) {
				break;
			}

			while (count1 >= MIN_GALLOP || count2 >= MIN_GALLOP) {
				count1 = j - l + 1 - gallopRight(arr, comp,  l, j - l + 1,
				                                 j - l, temp[k]);
				if (count1 != 0) {
					final int gap = count1 - 1;
					System.arraycopy(arr, j - gap, arr, i - gap, 1 + gap);
					i -= count1;
					j -= count1;
					if (j == l - 1) {
						done = true;
						break;
					}
				}

				arr[i] = temp[k];
				i--;
				k--;

				if (k == -1) {
					done = true;
					break;
				}

				count2 = k + 1 - gallopLeft(temp, comp, 0, k + 1, k, arr[j]);
				if (count2 != 0) {
					final int gap = count2 - 1;
					System.arraycopy(temp, k - gap, arr, i - gap, 1 + gap);
					i -= count2;
					k -= count2;
					if (k == -1) {
						done = true;
						break;
					}
				}

				arr[i] = arr[j];
				i--;
				j--;
				if (j == l - 1) {
					done = true;
					break;
				}

				minGallop--;
			}
			if (minGallop < 0) {
				minGallop = 0;
			}
			minGallop += 2;
		}

		if (k >= 0) {
			System.arraycopy(temp, 0, arr, l, k + 1);
		}
		return minGallop;
	}

	private static <T> int mergeCollapse(T[] arr, ArrayList<int[]> stack,
	                                      Comparator<T> comp, int minGallop)
	{
		while (stack.size() > 1) {
			int n = stack.size() - 2;
			if ((n > 0 && stack.get(n - 1)[1]
			    <= stack.get(n)[1] + stack.get(n + 1)[1])
			    ||
			    (n > 1 && stack.get(n - 2)[1]
			     <= stack.get(n - 1)[1] + stack.get(n)[1]))
			{
				if (stack.get(n - 1)[1] < stack.get(n + 1)[1]) {
					n--;
				}
			} else if (stack.get(n)[1] > stack.get(n + 1)[1]) {
				break;
			}
			minGallop = mergeAt(arr, stack, comp, n, minGallop);
		}
		return minGallop;
	}

	private static <T> int mergeAt(T[] arr, ArrayList<int[]> stack,
	                                Comparator<T> comp, int index,
	                                int minGallop)
	{
		final int[] one = stack.get(index);
		final int[] two = stack.get(index + 1);
		int base1 = one[0], len1 = one[1], base2 = two[0], len2 = two[1];
		final int length = stack.size();

		stack.set(index, new int[]{base1, len1 + len2});
		if (index == length - 3) {
			stack.set(index + 1, stack.get(index + 2));
		}
		stack.remove(length - 1);

		final int offset = searchRight(arr, comp, base1, len1, arr[base2]);
		base1 += offset;
		len1 -= offset;
		if (len1 == 0) {
			return minGallop;
		}

		len2 = searchLeft(arr, comp, base2, len2, arr[base1 + len1 - 1]);
		if (len2 == 0) {
			return minGallop;
		}
		return len1 > len2
		    ? mergeLo(arr, comp, base1, base2, base2 + len2, minGallop)
		    : mergeHi(arr, comp, base1, base2, base2 + len2, minGallop);
	}

	private static <T> void mergeForceCollapse(T[] arr, ArrayList<int[]> stack,
	                                           Comparator<T> comp,
	                                           int minGallop)
	{
		while (stack.size() > 1) {
			int n = stack.size() - 2;
			if (n > 0 && stack.get(n - 1)[1] < stack.get(n + 1)[1]) {
				n--;
			}

			minGallop = mergeAt(arr, stack, comp, n, minGallop);
		}
	}

	static <T extends Comparable<T>> T[] sorted(T[] arr) {
		T[] output = Arrays.copyOf(arr, arr.length);
		sort(output);
		return output;
	}

	static <T> T[] sorted(T[] arr, Comparator<T> comp) {
		T[] output = Arrays.copyOf(arr, arr.length);
		sort(output, comp);
		return output;
	}
}