
package hanlp.summary;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 搜索相关性评分算法
 * @author hankcs
 */
public class BM25
{
    /**
     * 文档句子的个数
     */
    int D;

    /**
     * 文档句子的平均长度
     */
    double avgdl;

    /**
     * 拆分为[句子[单词]]形式的文档
     */
    List<List<String>> docs;

    /**
     * 文档中每个句子中的每个词与词频
     */
    Map<String, Integer>[] f; //f = new Map[D];   就儲放每個句子中的 詞語詞的頻率(所以需要宣告動態陣列)

    /**
     * 文档中全部词语与出现在几个句子中
     */
    Map<String, Integer> df;

    /**
     * IDF
     */
    Map<String, Double> idf;

    /**
     * 调节因子
     */
    final static float k1 = 1.5f;

    /**
     * 调节因子
     */
    final static float b = 0.75f;

    public BM25(List<List<String>> docs)//分詞處理完 做統計詞語的頻率
    {
        this.docs = docs;
        D = docs.size();                    //////文档句子的个数
        for (List<String> sentence : docs) 
        {
            avgdl += sentence.size();
        }
        avgdl /= D;              //文档句子的平均长度
        f = new Map[D];          //文档中每个句子中的每个词与词频
        df = new TreeMap<String, Integer>(); //文档中全部词语与出现在几个句子中
        idf = new TreeMap<String, Double>();
        init();
    }

    /**
     * 在构造时初始化自己的所有参数
     */
    private void init()
    {
        int index = 0;
        for (List<String> sentence : docs)
        {
            Map<String, Integer> tf = new TreeMap<String, Integer>();
            for (String word : sentence)//計算每一列 每個詞語 的頻率
            {
                Integer freq = tf.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                tf.put(word, freq);
            }
            f[index] = tf;
            for (Map.Entry<String, Integer> entry : tf.entrySet())
            {
                String word = entry.getKey();
                Integer freq = df.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                df.put(word, freq);
            }
            ++index;
        }
        for (Map.Entry<String, Integer> entry : df.entrySet())
        {
            String word = entry.getKey();
            Integer freq = entry.getValue();
            idf.put(word, Math.log(D - freq + 0.5) - Math.log(freq + 0.5));
        }
    }
    /*
IDF（Inverse Document Frequency）：
換個角度來看，假設 D 是「所有的文件總數」，i 是網頁中所使用的單詞，
t(i) 是該單詞在所有文件總數中出現的「文件數」，那麼 idf(i) 的算法就是 log ( D/t(i) ) = log D – log t(i)
    * */    
    public double sim(List<String> sentence, int index)
    {
        double score = 0;
        for (String word : sentence)
        {
            if (!f[index].containsKey(word)) continue;
            int d = docs.get(index).size();
            Integer wf = f[index].get(word);
            score += (idf.get(word) * wf * (k1 + 1)
                    / (wf + k1 * (1 - b + b * d
                                                / avgdl)));
        }

        return score;
    }
/*
 最後，將 tf(i,j) * idf(i)（例如：i =「健康」一詞）來進行計算，以某一特定文件內的高單詞頻率，乘上該單詞在文件總數中的低文件頻率，
 便可以產生 TF-IDF 權重值，且 TF-IDF 傾向於過濾掉常見的單詞，保留重要的單詞
 */
    public double[] simAll(List<String> sentence)//對各列sentence做分析
    {
        double[] scores = new double[D];//依序把每一列sentence個別計算
        for (int i = 0; i < D; ++i)
        {
            scores[i] = sim(sentence, i);
        }
        return scores;
    }
}
