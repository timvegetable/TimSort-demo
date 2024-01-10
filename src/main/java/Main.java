import sort.ComplexTimSort;
import sort.SimpleTimSort;
import sort.OtherSorts;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.IntToLongFunction;
import java.util.stream.IntStream;

public class Main {

	private static final Random RANDOM = ThreadLocalRandom.current();
	private static final int INCREMENT = 100000;
	private static final List<Integer[]> testArrays = new ArrayList<>();
	private static final String FAILURE = "Sort failed!";

	static {
		for (int i = 1; i <= 10; i++) {
			testArrays.add(generateRandomArray(i * INCREMENT));
		}
	}

	public static void main(String[] args) {
		testAndPrint(ComplexTimSort::sort);
	}

	public static void testAndPrint(Consumer<Integer[]> sortAlgorithm) {
		var array = generateRandomArray(100);
		System.out.println("Before: ");
		System.out.println(Arrays.toString(array) + "\n");
		sortAlgorithm.accept(array);
		for (int i = 0; i < array.length - 1; i++) {
			assert array[i].compareTo(array[i + 1]) <= 0: FAILURE;
		}
		System.out.println("After: ");
		System.out.println(Arrays.toString(array));
	}

	public static void testSortWorks(Consumer<Integer[]> sortAlgorithm) {
		var testArray = testArrays.get(0);
		sortAlgorithm.accept(testArray);
		for (int i = 0; i < testArray.length - 1; i++) {
			assert testArray[i].compareTo(testArray[i + 1]) <= 0: FAILURE;
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

	public static Integer[] generateRandomArray(int size) {
		return RANDOM.ints(size, 0, size)
		             .parallel()
		             .boxed()
		             .toArray(Integer[]::new);
	}
}