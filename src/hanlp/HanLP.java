/*
* <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/17 19:02</create-date>
 *
 * <copyright file="HanLP.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package hanlp;


//import hanlp.phrase.IPhraseExtractor;
//import hanlp.phrase.MutualInformationEntropyPhraseExtractor;
import hanlp.seg.Segment;
import hanlp.seg.Viterbi.ViterbiSegment;
import hanlp.seg.common.Term;
import hanlp.summary.TextRankSentence;
import hanlp.tokenizer.StandardTokenizer;
import hanlp.utility.Predefine;
import hanlp.dictionary.ts.SimplifiedChineseDictionary;
import hanlp.dictionary.ts.TraditionalChineseDictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;


import static hanlp.utility.Predefine.logger;

/**
常用API函式
 */
public class HanLP
{
    /**
     * library全局配置，通过hanlp.properties配置（按照 变量名=值 的形式）
     */
    public static final class Config
    {   
    	
    	//
        /**
         * 開發模式
         */
        public static boolean DEBUG = false;
        /**
         * 核心词典路径
         */
        public static String CoreDictionaryPath = "data/dictionary/CoreNatureDictionary.txt";
        /**
         * 核心词典词性转移矩阵路径
         */
        public static String CoreDictionaryTransformMatrixDictionaryPath = "data/dictionary/CoreNatureDictionary.tr.txt";
        /**
         * 用户自定义词典路径
         */
        public static String CustomDictionaryPath[] = new String[]{"data/dictionary/custom/CustomDictionary.txt"};
        /**
         * 2元语法词典路径
         */
        public static String BiGramDictionaryPath = "data/dictionary/CoreNatureDictionary.ngram.txt";

        /**
         * 停用词词典路径
         */
        public static String CoreStopWordDictionaryPath = "data/dictionary/stopwords.txt";
        /**
         * 同义词词典路径
         */
        public static String CoreSynonymDictionaryDictionaryPath = "data/dictionary/synonym/CoreSynonym.txt";
        /**
         * 人名词典路径
         */
        //public static String PersonDictionaryPath = "data/dictionary/person/nr.txt";
        /**
         * 人名词典转移矩阵路径
         */
        //public static String PersonDictionaryTrPath = "data/dictionary/person/nr.tr.txt";
        /**
         * 地名词典路径
         */
        //public static String PlaceDictionaryPath = "data/dictionary/place/ns.txt";
        /**
         * 地名词典转移矩阵路径
         */
       // public static String PlaceDictionaryTrPath = "data/dictionary/place/ns.tr.txt";
        /**
         * 地名词典路径
         */
        //public static String OrganizationDictionaryPath = "data/dictionary/organization/nt.txt";
        /**
         * 地名词典转移矩阵路径
         */
       // public static String OrganizationDictionaryTrPath = "data/dictionary/organization/nt.tr.txt";
        /**
         * 繁简词典路径
         */
        public static String TraditionalChineseDictionaryPath = "data/dictionary/tc/TraditionalChinese.txt";
        /**
         * 声母韵母语调词典
         */
       // public static String SYTDictionaryPath = "data/dictionary/pinyin/SYTDictionary.txt";

        /**
         * 拼音词典路径
         */
        //public static String PinyinDictionaryPath = "data/dictionary/pinyin/pinyin.txt";

        /**
         * 音译人名词典
         */
        //public static String TranslatedPersonDictionaryPath = "data/dictionary/person/nrf.txt";

        /**
         * 日本人名词典路径
         */
        //public static String JapanesePersonDictionaryPath = "data/dictionary/person/nrj.txt";

        /**
         * 字符类型对应表
         */
        public static String CharTypePath = "data/dictionary/other/CharType.dat.yes";

        /**
         * 字符正规化表（全角转半角，繁体转简体）
         */
        public static String CharTablePath = "data/dictionary/other/CharTable.bin.yes";

        /**
         * 词-词性-依存关系模型
         */
        public static String WordNatureModelPath = "data/model/dependency/WordNature.txt";

       
        /**
         * 分词结果是否展示词性
         */
        public static boolean ShowTermNature = true;
        /**
         * 是否执行字符正规化（繁体->简体，全角->半角，大写->小写），切换配置后必须删CustomDictionary.txt.bin缓存
         */
        public static boolean Normalization = false;

        static
        {
            // 自动读取配置
            Properties p = new Properties();
            try
            {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                if (loader == null)
                {  // IKVM (v.0.44.0.5) doesn't set context classloader
                    loader = HanLP.Config.class.getClassLoader();
                }
                
                p.load(new InputStreamReader(Predefine.HANLP_PROPERTIES_PATH == null ?
                        loader.getResourceAsStream("HanLP.properties") :
                        new FileInputStream(Predefine.HANLP_PROPERTIES_PATH)
                        , "UTF-8"));
                
                //p.load(new FileInputSream("C:/Users/how/workspace/HanLP/bin/hanlp"));
                //p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("HanLP.properties"));
                //
                String root = p.getProperty("root", "").replaceAll("\\\\", "/");
                if (!root.endsWith("/")) root += "/";
                CoreDictionaryPath = root + p.getProperty("CoreDictionaryPath", CoreDictionaryPath);
                CoreDictionaryTransformMatrixDictionaryPath = root + p.getProperty("CoreDictionaryTransformMatrixDictionaryPath", CoreDictionaryTransformMatrixDictionaryPath);
                BiGramDictionaryPath = root + p.getProperty("BiGramDictionaryPath", BiGramDictionaryPath);
                CoreStopWordDictionaryPath = root + p.getProperty("CoreStopWordDictionaryPath", CoreStopWordDictionaryPath);
                CoreSynonymDictionaryDictionaryPath = root + p.getProperty("CoreSynonymDictionaryDictionaryPath", CoreSynonymDictionaryDictionaryPath);
                //PersonDictionaryPath = root + p.getProperty("PersonDictionaryPath", PersonDictionaryPath);
                //PersonDictionaryTrPath = root + p.getProperty("PersonDictionaryTrPath", PersonDictionaryTrPath);
                String[] pathArray = p.getProperty("CustomDictionaryPath", "dictionary/custom/CustomDictionary.txt").split(";");
                String prePath = root;
                for (int i = 0; i < pathArray.length; ++i)
                {
                    if (pathArray[i].startsWith(" "))
                    {
                        pathArray[i] = prePath + pathArray[i].trim();
                    }
                    else
                    {
                        pathArray[i] = root + pathArray[i];
                        int lastSplash = pathArray[i].lastIndexOf('/');
                        if (lastSplash != -1)
                        {
                            prePath = pathArray[i].substring(0, lastSplash + 1);
                        }
                    }
                }
                CustomDictionaryPath = pathArray;
                TraditionalChineseDictionaryPath = root + p.getProperty("TraditionalChineseDictionaryPath", TraditionalChineseDictionaryPath);
                //SYTDictionaryPath = root + p.getProperty("SYTDictionaryPath", SYTDictionaryPath);
                //PinyinDictionaryPath = root + p.getProperty("PinyinDictionaryPath", PinyinDictionaryPath);
                //TranslatedPersonDictionaryPath = root + p.getProperty("TranslatedPersonDictionaryPath", TranslatedPersonDictionaryPath);
                //JapanesePersonDictionaryPath = root + p.getProperty("JapanesePersonDictionaryPath", JapanesePersonDictionaryPath);
                //PlaceDictionaryPath = root + p.getProperty("PlaceDictionaryPath", PlaceDictionaryPath);
                //PlaceDictionaryTrPath = root + p.getProperty("PlaceDictionaryTrPath", PlaceDictionaryTrPath);
                //OrganizationDictionaryPath = root + p.getProperty("OrganizationDictionaryPath", OrganizationDictionaryPath);
                //OrganizationDictionaryTrPath = root + p.getProperty("OrganizationDictionaryTrPath", OrganizationDictionaryTrPath);
                CharTypePath = root + p.getProperty("CharTypePath", CharTypePath);
                CharTablePath = root + p.getProperty("CharTablePath", CharTablePath);
                WordNatureModelPath = root + p.getProperty("WordNatureModelPath", WordNatureModelPath);
                
                ShowTermNature = "true".equals(p.getProperty("ShowTermNature", "true"));
                Normalization = "true".equals(p.getProperty("Normalization", "false"));
            }
            catch (Exception e)
            {
                StringBuilder sbInfo = new StringBuilder("========Tips========\n请将HanLP.properties放在下列目录：\n"); // 打印一些友好的tips
                String classPath = (String) System.getProperties().get("java.class.path");
                if (classPath != null)
                {
                    for (String path : classPath.split(";"))
                    {
                        if (new File(path).isDirectory())
                        {
                            sbInfo.append(path).append('\n');
                        }
                    }
                }
                sbInfo.append("Web项目则请放到下列目录：\n" +
                                      "Webapp/WEB-INF/lib\n" +
                                      "Webapp/WEB-INF/classes\n" +
                                      "Appserver/lib\n" +
                                      "JRE/lib\n");
                sbInfo.append("并且编辑root=PARENT/path/to/your/data\n");
                sbInfo.append("现在HanLP将尝试从").append(System.getProperties().get("user.dir")).append("读取data……");
                logger.severe("没有找到HanLP.properties，可能会导致找不到data\n" + sbInfo);
            }
        }

        /**
         * 开启调试模式(会降低性能)
         */
        public static void enableDebug()
        {
            enableDebug(true);
        }

        /**
         * 开启调试模式(会降低性能)
         * @param enable
         */
        public static void enableDebug(boolean enable)
        {
            DEBUG = enable;
            if (DEBUG)
            {
                logger.setLevel(Level.ALL);
            }
            else
            {
                logger.setLevel(Level.OFF);
            }
        }
    }

    /**
     * 工具类，不需要生成实例
     */
    private HanLP() {}
    
    /**
     *繁转简 
     *
     * @param traditionalChineseString 繁体中文
     * @return 简体中文
     */
    public static String convertToSimplifiedChinese(String traditionalChineseString)
    {
        return TraditionalChineseDictionary.convertToSimplifiedChinese(traditionalChineseString.toCharArray());
    }

    /**
     *简转繁
     *
     * @param simplifiedChineseString 简体中文
     * @return 繁体中文
     */
    public static String convertToTraditionalChinese(String simplifiedChineseString)
    {
        return SimplifiedChineseDictionary.convertToTraditionalChinese(simplifiedChineseString.toCharArray());
    }


    /**
     * 分词
     *
     * @param text 文本
     * @return 切分后的单词
     */
    public static List<Term> segment(String text)
    {
        return StandardTokenizer.segment(text.toCharArray());
    }

    /**
         建立一個分詞
     */
    public static Segment newSegment()
    {
        return new ViterbiSegment();   // Viterbi分词

    }





    /**
     * 自动摘要
     * @param document 目标文档
     * @param size 需要的关键句的个数
     * @return 关键句列表
     */
    public static List<String> extractSummary(String document, int size)
    {   
        
        
        
    	//return TextRankSentenceMultiThreading.getTopSentenceList(document, size);
        
        
        return TextRankSentence.getTopSentenceList(document, size);
        
    }
    
    
  
}
