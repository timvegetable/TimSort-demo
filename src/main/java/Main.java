import sort.ComplexTimSort;
import sort.OtherSorts;
import sort.SimpleTimSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.IntToLongFunction;
import java.util.stream.IntStream;

public class Main {

	private static final Random RANDOM = ThreadLocalRandom.current();
	private static final int INCREMENT = 65536;
	private static final List<Integer[]> testArrays = new ArrayList<>();

	static {
		for (int i = 1; i <= 10; i++) {
			final int max = i * INCREMENT;
			var array = RANDOM.ints(max, 0, max)
			                  .parallel()
			                  .boxed()
			                  .toArray(Integer[]::new);
			testArrays.add(array);
		}
	}

	public static void main(String[] args) {
		testSortWorks(SimpleTimSort::sort);
//		for (int i = 60; i < 70; i++) {
//			Integer[] arr = Arrays.copyOf(testArrays.get(0), i);
//			SimpleTimSort.sort(arr);
//		}
//		System.out.println(testSort(SimpleTimSort::parallelSort) + " - "
//		                   + "Simple Parallel");
//		System.out.println(testSort(Arrays::parallelSort) + " - Arrays "
//		                   + "Parallel");
//		System.out.println(testSort(OtherSorts::mergeSort) + " - Merge");
//		System.out.println(testSort(SimpleTimSort::sort) + " - Simple");
//		System.out.println(testSort(ComplexTimSort::sort) + " - Complex");
		//		System.out.println(testSort(Arrays::mergeSort) + " - Arrays");
	}

	public static void testSortWorks(Consumer<Integer[]> sortAlgorithm) {
		var testArray = testArrays.get(0);
		sortAlgorithm.accept(testArray);
		for (int i = 0; i < testArray.length - 1; i++) {
			assert testArray[i].compareTo(testArray[i + 1]) <= 0;
		}
	}

	public static String testSort(Consumer<Integer[]> sortAlgorithm) {
		long[] times = new long[10];
		for (int i = 0; i < 10; i++) {
			times[i] = sort(sortAlgorithm, i);
		}
		return Arrays.toString(times);
	}

	public static long sort(Consumer<Integer[]> sortAlgorithm, int index) {
		var testArray = testArrays.get(index);
		IntToLongFunction sortTime = i -> {
			var array = Arrays.copyOf(testArray, testArray.length);
			long start = System.currentTimeMillis();
			sortAlgorithm.accept(array);
			long end = System.currentTimeMillis();
			return end - start;
		};
		return (long) IntStream.range(0, 100)
		                       .mapToLong(sortTime)
		                       .average()
		                       .orElse(0);
	}
}
