/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/29 15:14</create-date>
 *
 * <copyright file="Segment.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package hanlp.seg.Dijkstra;

import hanlp.HanLP;
import hanlp.seg.Dijkstra.Path.State;
import hanlp.seg.WordBasedGenerativeModelSegment;
import hanlp.seg.common.*;

import java.util.*;

/**
 * 最短路径分词
 * @author hankcs
 */
public class DijkstraSegment extends WordBasedGenerativeModelSegment
{
    @Override
    public List<Term> segSentence(char[] sentence)
    {
        WordNet wordNetOptimum = new WordNet(sentence);
        WordNet wordNetAll = new WordNet(wordNetOptimum.charArray); 
        

        ////////////////生成词网//////////////////////為一個句子生成空白词网
        
        GenerateWordNet(wordNetAll);//生成一元词网
			        //////////////////////////////////////////
			        System.out.print(wordNetAll);
			        System.out.println("印出wordNet");
			        ///////////////生成二元词图////////////////
        Graph graph = GenerateBiGraph(wordNetAll);
        
        				System.out.println(graph);
        				System.out.println("生成二元词图");
        //////////////////////////////////////////
        
        if (HanLP.Config.DEBUG)
        {
            System.out.printf("粗分词图：%s\n", graph.printByTo());
        }
        List<Vertex> vertexList = dijkstra(graph);



        if (HanLP.Config.DEBUG)
        {
            System.out.println("粗分结果" + convert(vertexList, false));
        }

        // 数字识别
        if (config.numberQuantifierRecognize)
        {
            mergeNumberQuantifier(vertexList, wordNetAll, config);
        }



        // 是否标注词性
        if (config.speechTagging)
        {
            speechTagging(vertexList);
        }

        return convert(vertexList, config.offset);
    }

    /**
     * dijkstra最短路径
     * @param graph
     * @return
     */
    private static List<Vertex> dijkstra(Graph graph)
    {
        List<Vertex> resultList = new LinkedList<Vertex>();
        Vertex[] vertexes = graph.getVertexes();
        List<EdgeFrom>[] edgesTo = graph.getEdgesTo();
        double[] d = new double[vertexes.length];
        Arrays.fill(d, Double.MAX_VALUE);
        d[d.length - 1] = 0;
        int[] path = new int[vertexes.length];
        Arrays.fill(path, -1);
        PriorityQueue<State> que = new PriorityQueue<State>();
        que.add(new State(0, vertexes.length - 1)); //當前位置的(路徑cost,位置)
        while (!que.isEmpty())
        {
            State p = que.poll();
            if (d[p.vertex] < p.cost) continue;//p.cost==0
            for (EdgeFrom edgeFrom : edgesTo[p.vertex])
            {
                if (d[edgeFrom.from] > d[p.vertex] + edgeFrom.weight)
                {
                    d[edgeFrom.from] = d[p.vertex] + edgeFrom.weight;
                    que.add(new State(d[edgeFrom.from], edgeFrom.from));
                    path[edgeFrom.from] = p.vertex;
                }
            }
        }
        for (int t = 0; t != -1; t = path[t])
        {
            resultList.add(vertexes[t]);
        }
        return resultList;
    }

}
