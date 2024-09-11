import java.util.Arrays;

public class BinaryPermutationDebugging {

    public static int factorial(int num) {
        return (num>1)?factorial(num-1)*num:num;
    }

    public static int unitTestCalculations(int numberOfVars) {
        return factorial(numberOfVars);
    }

    public static void main(String[] args) {
        permutations();
    }

    private static void swap(String[] elements, int a, int b) {
        String tmp = elements[a];
        elements[a] = elements[b];
        elements[b] = tmp;
    }

    private static void printArray(String[] elements, char delimiter) {
        String delimiterSpace = delimiter + " ";
        for(int i = 0; i < elements.length; i++) {
            System.out.print(elements[i] + delimiterSpace);
        }
        System.out.print('\n');
    }

    public static void permutations() {
        int n = 4;

        int[] indexes = new int[n];
        for (int i = 0; i < n; i++) {
            indexes[i] = 0;
        }
        String[] elements = {"0","0","1","1"};
        int i = 0;
        while (i < n) {
            if (indexes[i] < i) {
                swap(elements, i % 2 == 0 ?  0: indexes[i], i);
                System.out.println(Arrays.toString(elements));
                indexes[i]++;
                i = 0;
            }
            else {
                indexes[i] = 0;
                i++;
            }
        }
    }
}
