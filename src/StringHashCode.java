
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 探究2、7、11、31、101作为因数，对于String hashCode数据冲突的影响
 * https://segmentfault.com/a/1190000010799123
 *
 * @Author: xyx
 * @Date: 2018-12-09 12:37
 * @Version 1.0
 */
public class StringHashCode {

    private static Integer multiplier = 31;
    public static Integer hashCode(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = hash * multiplier + str.charAt(i);
        }
        return hash;
    }

    /**
     * 计算hash code冲突率，分析hashcode最大最小值
     *
     * @param multiplier
     * @param hashs
     */
    public static void calculateConflictRate(Integer multiplier, List<Integer> hashs) {

        Integer max = hashs.stream().max(Integer::compare).get();
        Integer min = hashs.stream().min(Integer::compare).get();

        int uniqueHashNum = (int) hashs.stream().distinct().count();
        int conflictNum = hashs.size() - uniqueHashNum;
        double conflictRate = conflictNum * 1.0 / hashs.size();

        System.out.println(String.format("multiplier=%3d, minHash=%11d, maxHash=%10d, conflictNum=%3d, conflictRate=%.4f%%",
                multiplier, min, max, conflictNum, conflictRate * 100));
    }

    /**
     * 将整个hash空间等分为64份，统计每个空间内的哈希值数量
     * 需要用long该标志i，否则会无限循环在整数正负之间
     *
     * @param hashs
     */
    public static Map<Integer, Integer> partition(List<Integer> hashs) {
        final int step = (int) Math.pow(2, 26);
        List<Integer> nums = new ArrayList<>();
        Map<Integer, Integer> statistics = new HashMap<>();

        int start = 0;
        for (long i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i += step) {
            final long min = i;
            final long max = min + step;


            int num = (int) hashs
                    .parallelStream()
                    .filter(x -> x >= min && x < max)
                    .count();

            statistics.put(start++, num);
            System.out.println(String.format("%2d, %11d, %11d, %4d", start, min, max, num));
            nums.add(num);
        }

        //为了防止计算出错，再次验证
        int hashNum = nums.stream().reduce(Integer::sum).get();

        if (hashNum == hashs.size())
            System.out.println("true");
        else System.out.println("false");

        return statistics;
    }

    /**
     * 按照行读取
     *
     * @param fileName
     * @return
     */
    public static List<String> readFile(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String> words = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                index++;
                words.add(line);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return words;
    }

    public static void main(String[] args) {

        List<String> words = readFile("/usr/share/dict/words");
        List<Integer> wordsHashs = words.stream().map(StringHashCode::hashCode).collect(Collectors.toList());

        calculateConflictRate(multiplier, wordsHashs);

        partition(wordsHashs);
    }

}
