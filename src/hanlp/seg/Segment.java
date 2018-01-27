/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/29 14:53</create-date>
 *
 * <copyright file="AbstractBaseSegment.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package hanlp.seg;

import hanlp.HanLP;
import hanlp.collection.trie.DoubleArrayTrie;
import hanlp.collection.trie.bintrie.BaseNode;
import hanlp.corpus.tag.Nature;
import hanlp.dictionary.CoreDictionary;
import hanlp.dictionary.CustomDictionary;
import hanlp.dictionary.other.CharTable;
import hanlp.dictionary.other.CharType;
import hanlp.seg.NShort.Path.AtomNode;
import hanlp.seg.common.Term;
import hanlp.seg.common.Vertex;
import hanlp.seg.common.WordNet;
import hanlp.summary.TextRankSentenceMultiThreading;
import hanlp.utility.Predefine;
import hanlp.utility.SentencesUtil;
import hanlp.utility.TextUtility;

import java.util.*;

import static hanlp.utility.Predefine.logger;

/**
 * 分词器
 * 是所有分词器的abstract
 */
public abstract class Segment
{
    /**
     * 分词器配置
     */
    protected Config config;

    /**
     * 构造一个分词器
     */
    public Segment()
    {
        config = new Config();
    }


    
    /**
     * 快速原子分词，希望用这个方法替换掉原来缓慢的方法
     *
     * @param charArray
     * @param start
     * @param end
     * @return
     */
    
    protected static List<AtomNode> quickAtomSegment(char[] charArray, int start, int end)
    {
        List<AtomNode> atomNodeList = new LinkedList<AtomNode>();
        int offsetAtom = start;
        int preType = CharType.get(charArray[offsetAtom]);
        int curType;
        while (++offsetAtom < end)
        {
            curType = CharType.get(charArray[offsetAtom]);
            if (curType != preType)
            {
                // 浮点数识别
                if (charArray[offsetAtom] == '.' && preType == CharType.CT_NUM)
                {
                    while (++offsetAtom < end)
                    {
                        curType = CharType.get(charArray[offsetAtom]);
                        if (curType != CharType.CT_NUM) break;
                    }
                }
                atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));
                start = offsetAtom;
            }
            preType = curType;
        }
        if (offsetAtom == end)
            atomNodeList.add(new AtomNode(new String(charArray, start, offsetAtom - start), preType));

        return atomNodeList;
    }



    /**
     * 合并数字
     * @param termList
     */
    protected void mergeNumberQuantifier(List<Vertex> termList, WordNet wordNetAll, Config config)
    {
        if (termList.size() < 4) return;
        StringBuilder sbQuantifier = new StringBuilder();
        ListIterator<Vertex> iterator = termList.listIterator();
        iterator.next();
        int line = 1;
        while (iterator.hasNext())
        {
            Vertex pre = iterator.next();
            if (pre.hasNature(Nature.m))
            {
                sbQuantifier.append(pre.realWord);
                Vertex cur = null;
                while (iterator.hasNext() && (cur = iterator.next()).hasNature(Nature.m))
                {
                    sbQuantifier.append(cur.realWord);
                    iterator.remove();
                    removeFromWordNet(cur, wordNetAll, line, sbQuantifier.length());
                }
                if (cur != null)
                {
                    if ((cur.hasNature(Nature.q) || cur.hasNature(Nature.qv) || cur.hasNature(Nature.qt)))
                    {
                        if (config.indexMode)
                        {
                            wordNetAll.add(line, new Vertex(sbQuantifier.toString(), new CoreDictionary.Attribute(Nature.m)));
                        }
                        sbQuantifier.append(cur.realWord);
                        iterator.remove();
                        removeFromWordNet(cur, wordNetAll, line, sbQuantifier.length());
                    }
                    else
                    {
                        line += cur.realWord.length();   // (cur = iterator.next()).hasNature(Nature.m) 最后一个next可能不含q词性
                    }
                }
                if (sbQuantifier.length() != pre.realWord.length())
                {
                    pre.realWord = sbQuantifier.toString();
                    pre.word = Predefine.TAG_NUMBER;
                    pre.attribute = new CoreDictionary.Attribute(Nature.mq);
                    pre.wordID = CoreDictionary.M_WORD_ID;
                    sbQuantifier.setLength(0);
                }
            }
            sbQuantifier.setLength(0);
            line += pre.realWord.length();
        }
//        System.out.println(wordNetAll);
    }

    /**
     * 将一个词语从词网中彻底抹除
     * @param cur 词语
     * @param wordNetAll 词网
     * @param line 当前扫描的行数
     * @param length 当前缓冲区的长度
     */
    private static void removeFromWordNet(Vertex cur, WordNet wordNetAll, int line, int length)
    {
        LinkedList<Vertex>[] vertexes = wordNetAll.getVertexes();
        // 将其从wordNet中删除
        for (Vertex vertex : vertexes[line + length])
        {
            if (vertex.from == cur)
                vertex.from = null;
        }
        ListIterator<Vertex> iterator = vertexes[line + length - cur.realWord.length()].listIterator();
        while (iterator.hasNext())
        {
            Vertex vertex = iterator.next();
            if (vertex == cur) iterator.remove();
        }
    }

    /**
     * 分词<br>
     * 此方法是线程安全的
     *
     * @param text 待分词文本
     * @return 单词列表
     */
    public List<Term> seg(String text)
    {   
    	
        char[] charArray = text.toCharArray();
        if (HanLP.Config.Normalization)
        {
            CharTable.normalization(charArray);
        }
        //使用threading
        //if (config.threadNumber > 1 && charArray.length > 10000)    //multithreading,text量要大才有意義,效率才會快
        if(config.threadNumber>1)
        {   
        	
        	           	
        	config.flagthread=true;
        	       	
            List<String> sentenceList = SentencesUtil.toSentenceList(charArray);//提供的text斷句
            String[] sentenceArray = new String[sentenceList.size()];//斷句完的句子數(sentenceArray.length)
            sentenceList.toArray(sentenceArray); //把StringList裡的內容放到 string[]陣列裡面存放
            //////////
            config.sentenceListNum=sentenceList.size();//把  斷句完的句子總數量 丟回給doc
        	/////////
            System.out.printf("\nMultiThreading有啟動,Threading Number=%d\n",config.threadNumber);
            
            //noinspection unchecked
            List<Term>[] termListArray = new List[sentenceArray.length];// 預設給每個原子節點 儲存的term
            final int per = sentenceArray.length / config.threadNumber;      
            
            ///////////////////////
            WorkThread[] threadArray = new WorkThread[config.threadNumber];
            for (int i = 0; i < config.threadNumber - 1; ++i)
            {
                int from = i * per; 
                ///
                
                System.out.println(i+" "+per+" "+sentenceArray.length+"句子數  "+config.threadNumber+"線呈數 ");
                ///
                threadArray[i] = new WorkThread(sentenceArray, termListArray, from, from + per);
                threadArray[i].start();
               
            }
            threadArray[config.threadNumber - 1] = new WorkThread(sentenceArray, termListArray, (config.threadNumber - 1) * per, sentenceArray.length);
            threadArray[config.threadNumber - 1].start();
            try
            {
                for (WorkThread thread : threadArray)
                {
                    thread.join();
                }
            }
            catch (InterruptedException e)
            {
                logger.severe("线程同步异常：" + TextUtility.exceptionToString(e));
                return Collections.emptyList();
            }
            List<Term> termList = new LinkedList<Term>();
            if (config.offset || config.indexMode)  // 由于分割了句子，所以需要重新校正offset
            {
                int sentenceOffset = 0;
                for (int i = 0; i < sentenceArray.length; ++i)//斷句完的句子個數
                {
                    for (Term term : termListArray[i])
                    {
                        term.offset += sentenceOffset;
                        termList.add(term);
                    }
                    sentenceOffset += sentenceArray[i].length();
                }
            }
            else
            {
                for (List<Term> list : termListArray)
                {
                    termList.addAll(list);
                }
            }

            return termList;
        }

        
        return segSentence(charArray);
    }

    /**
     * 分词
     *
     * @param text 待分词文本
     * @return 单词列表
     */
    public List<Term> seg(char[] text) 
    {
        assert text != null;
        if (HanLP.Config.Normalization)
        {
            CharTable.normalization(text);
        }
        return segSentence(text);
    }
   
    /**
     * 分词断句 输出句子形式
     *
     * @param text 待分词句子
     * @return 句子列表，每个句子由一个单词列表组成
     *//*
    public List<List<Term>> seg2sentence(String text)
    {
        List<List<Term>> resultList = new LinkedList<List<Term>>();
        {
            for (String sentence : SentencesUtil.toSentenceList(text))
            {
                resultList.add(segSentence(sentence.toCharArray()));
            }
        }

        return resultList;
    }*/

    /**
     * 给一个句子分词 (抽象方法，需要繼承此類別的class去實作內容物)
     *
     * @param sentence 待分词句子
     * @return 单词列表
     */
    protected abstract List<Term> segSentence(char[] sentence);

    /**
     * 设为索引模式
     *
     * @return
     */
    public Segment enableIndexMode(boolean enable)
    {
        config.indexMode = enable;
        return this;
    }

    /**
     * 开启词性标注
     *
     * @param enable
     * @return
     */
    public Segment enablePartOfSpeechTagging(boolean enable)
    {
        config.speechTagging = enable;
        return this;
    }

 
    /**
     * 是否启用用户词典
     *
     * @param enable
     */
    public Segment enableCustomDictionary(boolean enable)
    {
        config.useCustomDictionary = enable;
        return this;
    }

    /**
     * 是否启用偏移量计算（开启后Term.offset才会被计算）
     *
     * @param enable
     * @return
     */
    public Segment enableOffset(boolean enable)
    {
        config.offset = enable;
        return this;
    }

    /**
     * 是否启用数词和数量词识别<br>
     *     即[二, 十, 一] => [二十一]，[十, 九, 元] => [十九元]
     * @param enable
     * @return
     */
    public Segment enableNumberQuantifierRecognize(boolean enable)
    {
        config.numberQuantifierRecognize = enable;
        return this;
    }

    /**
     * 是否启用所有的命名实体识别
     *
     * @param enable
     * @return
     */
    public Segment enableAllNamedEntityRecognize(boolean enable)
    {
        config.nameRecognize = enable;
        config.japaneseNameRecognize = enable;
        config.translatedNameRecognize = enable;
        config.placeRecognize = enable;
        config.organizationRecognize = enable;
        config.updateNerConfig();
        return this;
    }

    class WorkThread extends Thread
    {
        String[] sentenceArray;
        List<Term>[] termListArray;
        int from;
        int to;

        public WorkThread(String[] sentenceArray, List<Term>[] termListArray, int from, int to)
        {
            this.sentenceArray = sentenceArray;
            this.termListArray = termListArray;
            this.from = from;
            this.to = to;
        }

        @Override
        public void run()
        {
            for (int i = from; i < to; ++i)
            {
                termListArray[i] = segSentence(sentenceArray[i].toCharArray());//各別ViterbiSegment裡面segSentence去做分詞
            }
        }
    }

    /**
     * 开启多线程
     * @param enable true表示开启4个线程，false表示单线程
     * @return
     */
    public Segment enableMultithreading(boolean enable)
    {
        if (enable) config.threadNumber = 4;
        else config.threadNumber = 1;
        return this;
    }

    /**
     * 开启多线程
     * @param threadNumber 线程数量
     * @return
     */
    public Segment enableMultithreading(int threadNumber)
    {
        config.threadNumber = threadNumber;
        return this;
    }
}
