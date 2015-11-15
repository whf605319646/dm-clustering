package org.shirdrn.dm.clustering.dbscan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.shirdrn.dm.clustering.common.ClusterPoint2D;
import org.shirdrn.dm.clustering.common.utils.FileUtils;
import org.shirdrn.dm.clustering.tool.common.ClusteringXYChart;
import org.shirdrn.dm.clustering.tool.utils.ChartUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DBSCANClusteringXYChart extends JFrame implements ClusteringXYChart {

	private static final long serialVersionUID = 1L;
	private String chartTitle;
	private File clusterPointFile;
	private Map<Integer, Set<ClusterPoint2D>> clusterPoints = Maps.newHashMap();
	private final List<Color> colorSpace = Lists.newArrayList();
	private final Set<ClusterPoint2D> noisePoints = Sets.newHashSet();
	private int noisePointClusterId;
	private XYSeries noiseXYSeries;
	
	public DBSCANClusteringXYChart(String chartTitle) throws HeadlessException {
		super();
		this.chartTitle = chartTitle;
	}
	
	private XYSeriesCollection buildXYDataset() {
		FileUtils.read2DClusterPointsFromFile(clusterPoints, noisePoints, "[\t,;\\s]+", clusterPointFile);
		return ChartUtils.createXYSeriesCollection(clusterPoints);
	}

	@Override
	public void renderXYChart() {
		// create xy dataset from points file
		final XYSeriesCollection xyDataset = buildXYDataset();
		noiseXYSeries = new XYSeries(noisePointClusterId);
		xyDataset.addSeries(noiseXYSeries);
		
		// create chart & configure xy plot
		JFreeChart jfreechart = ChartFactory.createScatterPlot(null, "X", "Y", xyDataset, PlotOrientation.VERTICAL, true, true, false);
		TextTitle title = new TextTitle(chartTitle, new Font("Lucida Sans Unicode", Font.BOLD, 14), 
				Color.DARK_GRAY, RectangleEdge.TOP, HorizontalAlignment.CENTER, 
				VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
		jfreechart.setTitle(title);
		
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		
		// render clustered series
		final XYItemRenderer renderer = xyPlot.getRenderer();
		Map<Integer, Color> colors = ChartUtils.generateXYColors(clusterPoints.keySet(), colorSpace);
		Iterator<Entry<Integer, Color>> iter = colors.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, Color> entry = iter.next();
			renderer.setSeriesPaint(entry.getKey(), entry.getValue());
		}
		
		// render noise series
		renderer.setSeriesPaint(noisePointClusterId, Color.RED);
		
		NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
		domain.setVerticalTickLabels(true);
		
		final ChartPanel chartPanel = new ChartPanel(jfreechart);
		this.add(chartPanel, BorderLayout.CENTER);
        
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, false));
        
        // display/hide noise points
		ChartUtils.createToggledButtons(panel, noiseXYSeries, noisePoints, "Display Noise Points", "Hide Noise Points");
        this.add(panel, BorderLayout.SOUTH);
	}
	
	@Override
	public void setNoisePointClusterId(int noisePointCluterId) {
		this.noisePointClusterId = noisePointCluterId;
	}

	@Override
	public void setclusterPointFile(File clusterPointFile) {
		this.clusterPointFile = clusterPointFile;
	}
	
	private static File getClusterPointFile(String[] args, String dir, int minPts, double eps) {
		if(args.length > 0) {
			return new File(args[0]);
		}
		return new File(new File(dir), minPts + "_" + eps + ".txt");		
	}
	
	public static void main(String args[]) {
//		int minPts = 4;
//		double eps = 0.0025094814205335555;
//		double eps = 0.004417483559674606;
//		double eps = 0.006147849217403014;
		
		int minPts = 8;
//		double eps = 0.004900098978598581;
//		double eps = 0.009566439044911;
		double eps = 0.013621050253196359;
		
		String chartTitle = "DBSCAN [Eps=" + eps + ", MinPts=" + minPts + "]";
		String dir = "C:\\Users\\yanjun\\Desktop";
		File clusterPointFile = getClusterPointFile(args, dir, minPts, eps);
		
		final ClusteringXYChart chart = new DBSCANClusteringXYChart(chartTitle);
		chart.setclusterPointFile(clusterPointFile);
		chart.setNoisePointClusterId(9999);
        ChartUtils.generateXYChart(chart);
    }

}
