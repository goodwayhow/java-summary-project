/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/30 10:02</create-date>
 *
 * <copyright file="HiddenMarkovModelSegment.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package hanlp.seg;

import hanlp.algoritm.Viterbi;
import hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import hanlp.collection.trie.DoubleArrayTrie;
import hanlp.corpus.tag.Nature;
import hanlp.dictionary.*;
import hanlp.dictionary.other.CharType;
import hanlp.seg.NShort.Path.*;
import hanlp.seg.common.Graph;
import hanlp.seg.common.Term;
import hanlp.seg.common.Vertex;
import hanlp.seg.common.WordNet;
import hanlp.utility.TextUtility;
import hanlp.utility.Predefine;

import java.util.*;

/**
 * 基于词语NGram模型的分词器基类
 *
 * @author hankcs
 */
public abstract class WordBasedGenerativeModelSegment extends Segment
{

    public WordBasedGenerativeModelSegment()
    {
        super();
    }


    /**
     * 将一条路径转为最终结果
     *
     * @param vertexList
     * @param offsetEnabled 是否计算offset
     * @return
     */
    protected static List<Term> convert(List<Vertex> vertexList, boolean offsetEnabled)
    {
        assert vertexList != null;
        assert vertexList.size() >= 2 : "这条路径不应当短于2" + vertexList.toString();
        int length = vertexList.size() - 2;
        List<Term> resultList = new ArrayList<Term>(length);
        Iterator<Vertex> iterator = vertexList.iterator();
        iterator.next();
        if (offsetEnabled)
        {
            int offset = 0;
            for (int i = 0; i < length; ++i)
            {
                Vertex vertex = iterator.next();
                Term term = convert(vertex);
                term.offset = offset;
                offset += term.length();
                resultList.add(term);
            }
        }
        else
        {
            for (int i = 0; i < length; ++i)
            {
                Vertex vertex = iterator.next();
                Term term = convert(vertex);
                resultList.add(term);
            }
        }
        return resultList;
    }

    /**
     * 将一条路径转为最终结果
     *
     * @param vertexList
     * @return
     */
    protected static List<Term> convert(List<Vertex> vertexList)
    {
        return convert(vertexList, false);
    }

    /**
     * 生成二元词图
     *
     * @param wordNet
     * @return
     */
    protected static Graph GenerateBiGraph(WordNet wordNet)
    {
        return wordNet.toGraph();
    }



    /**
     * 生成一元詞網
     *
     * @param wordNetStorage
     */
    protected void GenerateWordNet(final WordNet wordNetStorage)
    {
        final char[] charArray = wordNetStorage.charArray;
        
        // 核心词典查询
        ////////////////////////////這裡超重要去找出詞的處理
        DoubleArrayTrie<CoreDictionary.Attribute>.Searcher searcher = CoreDictionary.trie.getSearcher(charArray, 0);
        while (searcher.next())
        {
           wordNetStorage.add(searcher.begin+1 , new Vertex(new String(charArray, searcher.begin, searcher.length), searcher.value, searcher.index));
           
        }
        

        // 原子分词，保证图连通
        
        LinkedList<Vertex>[] vertexes = wordNetStorage.getVertexes(); 
        for (int i = 1; i < vertexes.length; )
        {
            if (vertexes[i].isEmpty())
            {
                int j = i + 1;
                for (; j < vertexes.length - 1; ++j)
                {
                    if (!vertexes[j].isEmpty()) break;
                }
                wordNetStorage.add(i, quickAtomSegment(charArray, i - 1, j - 1));
                i = j;
            }
            else i += vertexes[i].getLast().realWord.length();//看粗分詞網裡面 每一列的最後一個詞的長度
        }
    }

    /**
     * 将节点转为term
     *
     * @param vertex
     * @return
     */
    private static Term convert(Vertex vertex)
    {
        return new Term(vertex.realWord, vertex.guessNature());
    }

    /**
     * 词性标注
     *
     * @param vertexList
     */
    protected static void speechTagging(List<Vertex> vertexList)
    {
        Viterbi.compute(vertexList, CoreDictionaryTransformMatrixDictionary.transformMatrixDictionary);
    }


}
