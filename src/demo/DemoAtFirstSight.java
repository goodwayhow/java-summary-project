
package demo;

import hanlp.HanLP;
import hanlp.seg.Segment;
import hanlp.seg.Dijkstra.DijkstraSegment;

/**
 * 第一个Demo，惊鸿一瞥
 *
 */
public class DemoAtFirstSight
{
    public static void main(String[] args)
    {   
    	Segment segment=new DijkstraSegment();
        System.out.println("首次编译运行时，HanLP会自动构建词典缓存，请稍候……");
        HanLP.Config.enableDebug();         // 为了避免你等得无聊，开启调试模式说点什么:-)
        //System.out.println(HanLP.segment(HanLP.convertToSimplifiedChinese("他说的确实在理，他說得確實正確。")));
        System.out.println(segment.seg(HanLP.convertToSimplifiedChinese("他说的确实在理。")));
    }
}
