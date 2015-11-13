package main.java.nl.tue.ieis.is.correlation.graph;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import main.java.nl.tue.tm.is.ptnet.Arc;
import main.java.nl.tue.tm.is.ptnet.Node;

import org.jgrapht.graph.SimpleDirectedGraph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;

public class GraphUtil extends JApplet implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private SimpleDirectedGraph<Node, Arc> graph;
	private JGraphXAdapter<Node, Arc> jgraphX;
	private mxGraphComponent graphComponent;
	
	private Set<Node> nodeList;
	private Set<Arc> edgeList;
	
	public GraphUtil(Set<Node> nodeList,Set<Arc> edgeList) throws HeadlessException {
		super();
		graph = new SimpleDirectedGraph<Node, Arc>(Arc.class);
		this.nodeList = nodeList;
		this.edgeList = edgeList;
		addNodesToGraph();
		addEdgesToGraph();
	}
	
	public SimpleDirectedGraph<Node, Arc> getGraph() {
		return graph;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {		
		if (e.getWheelRotation() < 0) graphComponent.zoomIn();
		else graphComponent.zoomOut();
	}
	
	public void draw (String title) {
		jgraphX = new JGraphXAdapter<Node, Arc>(graph);
        graphComponent = new mxGraphComponent(jgraphX);        

        JFrame frame2 = new JFrame();
        frame2.getContentPane().add(new JScrollPane (graphComponent));
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.addMouseWheelListener(this);
        
        frame2.setSize(600, 400);
        frame2.setTitle("Generated Petri Net");
        frame2.setVisible(true);
        frame2.setTitle(title);
        frame2.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        try {
	        jgraphX.getModel().beginUpdate();
	        for (mxCell cell : jgraphX.getVertexToCellMap().values()) {
	        	jgraphX.getModel().setGeometry(cell, new mxGeometry(20, 20, 20, 20));
	            jgraphX.updateCellSize(cell);
	        }
        } finally {
        	jgraphX.getModel().endUpdate();
        }
        
        mxGraphComponent graphComponent = new mxGraphComponent(jgraphX);    
        graphComponent.setConnectable(false);
        graphComponent.setAutoExtend(false);
        graphComponent.setDragEnabled(false);
        
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_STROKEWIDTH, 3);
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_FONTCOLOR, "#EA7201");
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_FONTSIZE, 13);
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
        jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        //jgraphX.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");

        jgraphX.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKECOLOR, "#FF0000");
        jgraphX.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        jgraphX.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTFAMILY, "Times New Roman");
        jgraphX.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTSIZE, 15);
        jgraphX.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_SPACING, "10");
        
        jgraphX.setAutoSizeCells(true);
        jgraphX.setAutoOrigin(true);
        jgraphX.setCellsDeletable(false);
        jgraphX.setCellsResizable(true);
        jgraphX.setCellsEditable(false);
        jgraphX.setCellsSelectable(true);
        jgraphX.setLabelsVisible(true);
        jgraphX.setLabelsClipped(false);
        jgraphX.setCollapseToPreferredSize(true);
        jgraphX.setSplitEnabled(false);
        jgraphX.setResetEdgesOnConnect(false);
        jgraphX.setResetEdgesOnMove(true);
        jgraphX.setResetEdgesOnResize(true);
        jgraphX.setResetViewOnRootChange(true);
        jgraphX.setCellsDisconnectable(false);
        jgraphX.setConnectableEdges(false);
        jgraphX.setCellsDisconnectable(false);
        jgraphX.setConnectableEdges(false);
        jgraphX.setCellsCloneable(false);
        jgraphX.setCellsEditable(false);
        jgraphX.setAllowDanglingEdges(false);

        mxHierarchicalLayout layout2 = new mxHierarchicalLayout(jgraphX);
        layout2.setDisableEdgeStyle(false);
        layout2.setFineTuning(true);
        layout2.setMoveParent(true);
        layout2.setResizeParent(true);
        layout2.setInterRankCellSpacing(50);
        layout2.setOrientation(SwingConstants.WEST);

        Object cell = jgraphX.getDefaultParent();
		jgraphX.getModel().beginUpdate();
		try {
			layout2.execute(cell);
		} finally {
			jgraphX.getModel().endUpdate();
		}
	}

	private void addNodesToGraph() {
		for(Node n : nodeList) {
			graph.addVertex(n);
		}
	}

	private void addEdgesToGraph() {
		for(Arc e : edgeList) {
			graph.addEdge(e.getSource(), e.getTarget(), e);
		}
	}

	
	public BufferedImage generatePicture(String dest) {
		BufferedImage image = mxCellRenderer.createBufferedImage(jgraphX, null, 1, Color.WHITE, true, null);
		//"C:\\Temp\\graph.png"
		try {
			ImageIO.write(image, "PNG", new File(dest));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
}
