
package demo;

//import src.main.java.com.hankcs.hanlp.HanLP;
//import src.main.java.com.hankcs.hanlp.summary.TextRankSentence;
import hanlp.HanLP;
import hanlp.summary.TextRankSentence;
import hanlp.summary.TextRankSentenceMultiThreading;

import javax.swing.*;
import java.util.List;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.FlowLayout;
/**
 * 自动摘要
 */
public class DemoSummary extends JFrame implements ActionListener
{   
	static DemoSummary mainFrame=new DemoSummary();
    static JButton btn_viterbi=new JButton();
    static JButton btn_thread=new JButton();
    static JButton btn_close=new JButton();
    static JLabel label=new JLabel();
    static JLabel label2=new JLabel();
    static JTextField field = new JTextField(35); //新增一個文字框
    static JTextField field_num = new JTextField(2); //新增一個文字框
	static TextArea txt=new TextArea("",200,200,TextArea.SCROLLBARS_BOTH);
		
		  
	
    public static void main(String[] args)
    {   
    	  
    	//全選文字框，方便使用者更改
    	  field.selectAll();
          field_num.selectAll();
          
		  //設定按鈕文字
		  btn_thread.setText("Load Dijkstra-MultiThreading");
		  btn_viterbi.setText("StandardTokenizer");
		  btn_close.setText("Exit");
		  //設定文字標籤提示訊息
		  label.setText("選擇使用的分詞");
		   
          mainFrame.setLayout(new FlowLayout(FlowLayout.LEFT));
		  mainFrame.setSize(500,500);
		  		  
		  mainFrame.add(field);
		  mainFrame.add(field_num);
		  mainFrame.add(btn_thread);
		  mainFrame.add(btn_viterbi);
		  mainFrame.add(btn_close);
		  mainFrame.add(label);
		  mainFrame.add(txt);
          mainFrame.setVisible(true);
		   
		  //建立按鈕的監聽事件，呼叫自己這個的類別，去實作ActionListener
		  //和一個actionPerformed方法
		  btn_thread.addActionListener(mainFrame);
		  btn_viterbi.addActionListener(mainFrame);
		  btn_close.addActionListener(mainFrame);
      
       

    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		    
		
		    JButton btn2=(JButton) e.getSource();//擷取button按下的選項狀態
		    
		    String text=field.getText();
		    int text_num=Integer.parseInt(field_num.getText());
	        
		   
		    
		    if(btn2==btn_thread){
	       
            List<String> sentenceList= TextRankSentenceMultiThreading.getTopSentenceList(text, text_num);
	        System.out.println(sentenceList);
	        
		        //顯示分析完的句子
	        
		        txt.setText(sentenceList.toString()); 

            }
            else if(btn2==btn_viterbi){
    	        
                List<String> sentenceList= TextRankSentence.getTopSentenceList(text, text_num);
    	        System.out.println(sentenceList);
    	        
	    	        //顯示分析完的句子
	    	        txt.setText(sentenceList.toString());
    	        
            }
            else{
            	System.exit(0);}
           
	        
		
	}
}
