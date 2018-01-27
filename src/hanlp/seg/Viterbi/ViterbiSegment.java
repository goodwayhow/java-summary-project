
package hanlp.seg.Viterbi;

import hanlp.HanLP;
//import hanlp.recognition.nr.PersonRecognition;
//import hanlp.recognition.ns.PlaceRecognition;
//import hanlp.recognition.nt.OrganizationRecognition;
import hanlp.seg.WordBasedGenerativeModelSegment;
import hanlp.seg.common.Term;
import hanlp.seg.common.Vertex;
import hanlp.seg.common.WordNet;

import java.util.LinkedList;
import java.util.List;

/**
 * Viterbi分词器<br>
 */
public class ViterbiSegment extends WordBasedGenerativeModelSegment
{
    @Override
    protected List<Term> segSentence(char[] sentence)
    {

        WordNet wordNetAll = new WordNet(sentence);        
        ////////////////生成一元词网////////////////////        
        GenerateWordNet(wordNetAll); //沒有這行的話,詞網是空白的  
        ///////////////////////////////////////////////     做完wordNet字典自動分詞後，才能找出詞語
        if (HanLP.Config.DEBUG)
        {
            System.out.printf("粗分词网：\n%s\n", wordNetAll);
        }

        List<Vertex> vertexList = viterbi(wordNetAll);


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

    private static List<Vertex> viterbi(WordNet wordNet)//粗分詞網後做Viterbi
    {
        // 避免生成对象，优化速度
        LinkedList<Vertex> nodes[] = wordNet.getVertexes();//從詞網一列列抓 節點到 node[]陣列裡面
        LinkedList<Vertex> vertexList = new LinkedList<Vertex>();
        
        //
        //System.out.println("nodes length(會多+2因為前後兩個##)="+nodes.length);
        //
        
        for (Vertex node : nodes[1])
        {
            node.updateFrom(nodes[0].getFirst());// 目前節點和 前一列的 節點 去計算 weight大小, 找最短路徑 
        }
        for (int i = 1; i < nodes.length - 1; ++i)
        {
            LinkedList<Vertex> nodeArray = nodes[i];
            if (nodeArray == null) continue;
            for (Vertex node : nodeArray)
            {   
            	//////////////////////////////////////////////////
            	System.out.print(node.realWord+" "+node.weight+" ");
            	///////////////////////////////////////////////////
                if (node.from == null) continue;
                for (Vertex to : nodes[i + node.realWord.length()])
                {
                    to.updateFrom(node);  //目前節點和 前一列的 節點 去計算 weight大小, 找最短路徑 
                }
            }System.out.println(" ");
        }
        Vertex from = nodes[nodes.length - 1].getFirst();
        while (from != null)
        {
            vertexList.addFirst(from);
            from = from.from;
        }
        return vertexList;
    }

}
