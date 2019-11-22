import java.util.* ;
import java.util.concurrent.*; 
import java.util.concurrent.locks.*; 
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*; 
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.color.*;

public class SchedulingAlgorithms{
	static int xH = 0;
	static int xF = 0;
	static ProcessStatsHRRN pHRRN[] = new ProcessStatsHRRN[5];
	static ProcessStatsFCFS pFCFS[] = new ProcessStatsFCFS[5];
	static Lock lock = new ReentrantLock();
	static DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
	static JFrame frame = new JFrame("Menu");
	static JFrame frame1 = new JFrame("FCFS");
	static JFrame frame2 = new JFrame("HRRN");
	static JFrame frame3 = new JFrame("Plot");
    static Container c1 = frame1.getContentPane();
    static Container c2 = frame2.getContentPane();
	public static void main(String[] args)
	{
		ExecutorService executor1 = Executors.newCachedThreadPool();
		ExecutorService executor2 = Executors.newCachedThreadPool();
		int n=5, j =0, sum_bt = 0; 
		int k = 0;
		int m = 0;
		JButton PDetailsFCFS = new JButton("Process Details of FCFS");
		PDetailsFCFS.setPreferredSize(new Dimension(200, 200));
		PDetailsFCFS.setBackground(new Color(141, 217, 91));
		JButton PDetailsHRRN = new JButton("Process Details of HRRN");
		PDetailsHRRN.setPreferredSize(new Dimension(200, 200));
		PDetailsHRRN.setBackground(new Color(97, 179, 41));
		JButton Plot = new JButton("Plot");
		Plot.setPreferredSize(new Dimension(200, 200));
		Plot.setBackground(new Color(191, 234, 163));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(PDetailsHRRN, BorderLayout.NORTH);
	    frame.add(PDetailsFCFS, BorderLayout.CENTER);
	    frame.add(Plot, BorderLayout.SOUTH);
	    frame.setSize(600,600);
		frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame3.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    c1.setLayout(new BoxLayout(c1, BoxLayout.Y_AXIS));
	    c2.setLayout(new BoxLayout(c2, BoxLayout.Y_AXIS));
	    
		for(int i = 0 ; i < 5; i++)
		{
			k = 0;
			sum_bt = 0;
			j = 0;
			Process p[] = new Process[n];
			try {Thread.sleep(200);}
			catch(Exception e) {}
			while(k < 5)
			{
				m++;
				int arrtime = ThreadLocalRandom.current().nextInt(j,j+2);
				j  += (arrtime-j) + 1;
				int exetime = ThreadLocalRandom.current().nextInt(1,5); 
				sum_bt += exetime;
				p[k] = new Process();
				p[k].updateVal(m, arrtime, exetime);
				k++;
				
			}
			executor1.execute(new CalHRRN(p, sum_bt));
			executor2.execute(new CalFCFS(p));
		}
		executor1.shutdown();
		while(!executor1.isTerminated()) {
			
		}
		LineChart chart = new LineChart("HRRN vs FCFS" ,"HRRN vs FCFS", dataset);
		ChartPanel cp = chart.getChartPanel();
		frame3.add(cp);
		frame3.setSize( 1120 , 734 );
		frame3.validate();
		chart.validate();
//	    RefineryUtilities.centerFrameOnScreen( chart );
	    frame1.pack();
	    frame1.setSize(1920,1200);
	    frame2.pack();
	    frame2.setSize(1920,1200);
		PDetailsFCFS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				frame1.setVisible(true);				
			}
		});
		PDetailsHRRN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				frame2.setVisible(true);				
			}
		});
		Plot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {								
				frame3.setVisible( true );
			}
		});
		frame.setVisible(true);
	}
	public static void appendHRRN(ProcessStatsHRRN pr)
	{
		lock.lock();
		try {
		pHRRN[xH] = pr;
		frame2 = pHRRN[xH].print(frame2);
		dataset.addValue(pHRRN[xH].avgtt, "Average Turn Around Time | HRRN", Integer.toString(xH+1));
		dataset.addValue(pHRRN[xH].avgwt, "Average Waiting Time | HRRN", Integer.toString(xH+1));
		xH++;
		}
		finally {
			lock.unlock();
		}
	}
	public static void appendFCFS(ProcessStatsFCFS pr)
	{
		lock.lock();
		try {
		pFCFS[xF] = pr;
		frame1 = pFCFS[xF].print(frame1);
		dataset.addValue(pFCFS[xF].avgtt, "Average Turn Around Time | FCFS", Integer.toString(xF+1));
		dataset.addValue(pFCFS[xF].avgwt, "Average Waiting Time | FCFS", Integer.toString(xF+1));
		xF++;
		}
		finally {
			lock.unlock();
		}
	}
}
class CalHRRN implements Runnable
{
	int n = 5, t = 0;
	float avgwt = 0, avgtt = 0,temp;
	int ct[] = new int[n];
	int wt[] = new int[n];	
	int tt[] = new int[n]; 
	float ntt[] = new float[n];
	int pwt[] = new int[n];	
	int ptt[] = new int[n]; 
	float pntt[] = new float[n];
	int arriv[] = new int[n];
	int burst[] = new int[n];
	int sum_bt = 0;
	int cnt = 0;
	int compl[] = new int[n];
	Process p[] = new Process[n];
	Process pp[] = new Process[n];
	ProcessStatsHRRN prs;
	static Lock lock = new ReentrantLock();
	SchedulingAlgorithms t1 = new SchedulingAlgorithms();
	public CalHRRN(Process[] pr, int s_bt)
	{
		p = pr;
		sum_bt = s_bt;
	}
	public void run() {
		cnt = 0;
		for (t = p[0].at; t < sum_bt+p[0].at;) 
		{ 
			float hrr = -999;   // setting lower limit

			// Variable to store next process selected 
			int loc = 0; 

			for (int i = 0; i < n; i++) 
			{ 

				// Checking if process has arrived and is Incomplete 
				if (p[i].at <= t && compl[i] == 0) { 

					// Calculating Response Ratio 
						temp = (p[i].bt + (t - p[i].at)) / p[i].bt; 	
						if (hrr < temp)
						{ 
							hrr = temp; 
							loc = i; 
						}
					// Checking for Highest Response Ratio 
 
				} 
			} 

			t += p[loc].bt; 
			//waiting time
			wt[loc] = t - p[loc].at - p[loc].bt; 

			// TT 
			tt[loc] = t - p[loc].at; 
			
			avgtt += tt[loc]; 

			// Normalized TT 
			ntt[loc] = ((float)tt[loc] / p[loc].bt); 

			// Updating Completion Status 
			compl[loc] = 1; 

			
			avgwt += wt[loc];  
			pwt[cnt] = wt[loc];
			ptt[cnt] = tt[loc];
			pntt[cnt] = ntt[loc];
			pp[cnt] = p[loc];
			IncrCNT();
			if(cnt == 5)
			{
				ZeroCNT();
			}
		}
		prs = new ProcessStatsHRRN();
		prs.updateVal(pp, avgwt, avgtt, pwt, ptt, pntt);
		t1.appendHRRN(prs);
	}
	void IncrCNT()
	{
		lock.lock();
		try {
			cnt++;
		}
		finally {
			lock.unlock();
		}
	}
	void ZeroCNT()
	{
		lock.lock();
		try {
			cnt = 0;
		}
		finally {
			lock.unlock();
		}
	}
}
class CalFCFS implements Runnable{
	int n = 5;
	float avgwt = 0, avgtt = 0;
	int ct[] = new int[n];     // completion times
	int ta[] = new int[n];     // turn around times
	int wt[] = new int[n];     // waiting times 
	int pwt[] = new int[n];	
	int ptt[] = new int[n]; 
	float pntt[] = new float[n];
	int compl[] = new int[n];
	Process p[] = new Process[n];
	Process pp[] = new Process[n];
	int temp;
	ProcessStatsFCFS prs;
	static Lock lock = new ReentrantLock();
	SchedulingAlgorithms t1 = new SchedulingAlgorithms();
	public CalFCFS(Process[] pr)
	{
		p = pr;
	}
	public void run()
	{
		for(int  i = 0 ; i < n; i++)
		{
			if( i == 0)
			{	
				ct[i] = p[i].at + p[i].bt;
			}
			else
			{
				if( p[i].at > ct[i-1])
				{
					ct[i] = p[i].at + p[i].bt;
				}
				else
					ct[i] = ct[i-1] + p[i].bt;
			}
			ta[i] = ct[i] - p[i].at ;          // turnaround time= completion time- arrival time
			wt[i] = ta[i] - p[i].bt ;          // waiting time= turnaround time- burst time
			avgwt += wt[i] ;               // total waiting time
			avgtt += ta[i] ;               // total turnaround time
			
		}
		prs = null;
		prs = new ProcessStatsFCFS();
		prs.updateVal(p, avgwt, avgtt, wt, ta);
		t1.appendFCFS(prs);
	}
}
class Process {
	int name = 0;
	int at = 0;
	int bt = 0;
	static Lock lock = new ReentrantLock();
	void updateVal(int n, int a, int b)
	{
		lock.lock();
		try {
		name = n;
		at = a;
		bt = b;
		}
		finally {
			lock.unlock();
		}
		
	}
}
class ProcessStatsHRRN {
	int n = 5;
	float avgwt = 0, avgtt = 0;
	int wt[] = new int[n];	
	int tt[] = new int[n]; 
	float ntt[] = new float[n]; 
	Process[] pr;
	static Lock lock = new ReentrantLock();
	void updateVal(Process[] p, float awt, float att, int[] w, int[] t, float[] nt)
	{
		lock.lock();
		try {
		pr = p;
		avgwt = awt;
		avgtt = att;
		wt = w;
		tt = t;
		ntt = nt;
		}
		finally {
			lock.unlock();
		}
	}
	JFrame print(JFrame f)
	{
		int k = 0;
		System.out.println("\t\t\t\t\tHRRN");
		System.out.print("\nName\tArrival Time\tBurst Time\tWaiting Time");
		System.out.println("\tTurnAround Time\t Normalized TT");
		String data[][] = new String[8][6];
		String coloums[] = {"Name", "Arrival Time", "Burst Time", "Waiting Time", "TurnAround Time", "Normalized TT"};
		while(k < 5)
		{
			data[k][0] = Integer.toString(pr[k].name);
			data[k][1] = Integer.toString(pr[k].at);
			data[k][2] = Integer.toString(pr[k].bt);
			data[k][3] = Integer.toString(wt[k]);
			data[k][4] = Integer.toString(tt[k]);
			data[k][5] = Float.toString(ntt[k]);
			System.out.print("\n"+pr[k].name+"\t\t"+ pr[k].at+"\t\t"); 
			System.out.print(pr[k].bt+"\t\t"+wt[k]+"\t\t"); 
			System.out.print(tt[k]+"\t\t"+ntt[k]);
			k++;
		}
		int i = 0;
		while(i < 6)
		{
			data[k][i] = "";
			i++;
		}
		k++;
		data[k][0] = "Average Waiting time:";
		data[k][1] = Float.toString(avgwt);
		k++;
		data[k][0] = "Average Turn Around time:";
		data[k][1] = Float.toString(avgtt);
		System.out.println("\nAverage Waiting time:"+avgwt /n +"\n" ); 
		System.out.println("Average Turn Around time:"+avgtt /n +"\n");
		JTable jt=new JTable(data,coloums);
		jt.setBackground(Color.getHSBColor((float)1.5, (float)0.0392, (float)1));
		jt.setFont(new Font("Gill Sans", Font.PLAIN, 16));
		JScrollPane sp=new JScrollPane(jt); 
		f.add(sp, BorderLayout.SOUTH);
		return f;
	}
}
class ProcessStatsFCFS{
	int n = 5;
	float avgwt = 0, avgtt = 0;
	int wt[] = new int[n];	
	int tt[] = new int[n]; 
	float ntt[] = new float[n]; 
	Process[] pr;
	static Lock lock = new ReentrantLock();
	void updateVal(Process[] p, float awt, float att, int[] w, int[] t)
	{
		lock.lock();
		try {
		pr = p;
		avgwt = awt;
		avgtt = att;
		wt = w;
		tt = t;
		}
		finally {
			lock.unlock();
		}
	}
	
	JFrame print(JFrame f)
	{
		int k = 0;
		System.out.println("\t\t\t\t\tFCFS");
		System.out.println("\nName\tArrival Time\tBurst Time\tWaiting Time\tTurnAround Time");
		String data[][] = new String[8][5];
		String coloums[] = {"Name", "Arrival Time", "Burst Time", "Waiting Time", "TurnAround Time"};
		while(k < 5)
		{
			data[k][0] = Integer.toString(pr[k].name);
			data[k][1] = Integer.toString(pr[k].at);
			data[k][2] = Integer.toString(pr[k].bt);
			data[k][3] = Integer.toString(wt[k]);
			data[k][4] = Integer.toString(tt[k]);
			System.out.print("\n"+pr[k].name+"\t\t"+ pr[k].at+"\t\t"); 
			System.out.print(pr[k].bt+"\t\t"+wt[k]+"\t\t"+tt[k]);
			k++;
		}
		int i = 0;
		while(i < 5)
		{
			data[k][i] = "";
			i++;
		}
		k++;
		data[k][0] = "Average Waiting time:";
		data[k][1] = Float.toString(avgwt);
		k++;
		data[k][0] = "Average Turn Around time:";
		data[k][1] = Float.toString(avgtt);
		System.out.println("\nAverage Waiting time:"+avgwt /n +"\n" ); 
		System.out.println("Average Turn Around time:"+avgtt /n +"\n");
		JTable jt=new JTable(data,coloums);
		jt.setBackground(Color.getHSBColor((float)1.5, (float)0.0392, (float)1));
		jt.setFont(new Font("Gill Sans", Font.PLAIN, 16));
		JScrollPane sp=new JScrollPane(jt); 
		f.add(sp, BorderLayout.SOUTH);
		return f;
	}
}
class LineChart extends ApplicationFrame {
ChartPanel chartPanel;
JFreeChart lineChart;
public LineChart( String applicationTitle , String chartTitle , DefaultCategoryDataset dataset) {
    super(applicationTitle);
    JFreeChart lineChart = ChartFactory.createLineChart(
       chartTitle,
       "Process","Time",
       dataset,
       PlotOrientation.VERTICAL,
       true,true,false);
    lineChart.getPlot().setBackgroundPaint(Color.getHSBColor((float)1.5, (float)0.0392, (float)1));

    chartPanel = new ChartPanel( lineChart );
//    chartPanel.[setBackground][2]( Color.RED );
//    chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 }
ChartPanel getChartPanel()
{
	return chartPanel;
}
}
