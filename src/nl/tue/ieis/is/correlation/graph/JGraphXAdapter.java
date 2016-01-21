package nl.tue.ieis.is.correlation.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;

import nl.tue.tm.is.ptnet.Node;
import nl.tue.tm.is.ptnet.Place;
import nl.tue.tm.is.ptnet.Transition;

import org.jgrapht.Graph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class JGraphXAdapter<V, E> extends mxGraph implements GraphListener<V, E>  {
	
	private HashMap<V, mxCell> vertexToCellMap = new HashMap<V, mxCell>();
	private HashMap<E, mxCell> edgeToCellMap = new HashMap<E, mxCell>(); 
	private HashMap<mxCell, V> cellToVertexMap = new HashMap<mxCell, V>();
	private HashMap<mxCell, E> cellToEdgeMap = new HashMap<mxCell, E>();
	
	private HashMap<V, Object> vertexToObjectMap = new HashMap<V, Object>();
	private HashMap<E, Object> edgeToObjectMap = new HashMap<E, Object>();
	private HashMap<Object, V> objectToVertexMap = new HashMap<Object, V>();
	private HashMap<Object, E> objectToEdgeMap = new HashMap<Object, E>();
	
	private Graph<V,E> graphT;

	public JGraphXAdapter(final Graph<V, E> graphT) {
		super();
	    this.graphT = graphT;
	    //graphT.addGraphListener(this);
	    insertJGraphT(graphT);
	}
	
	public void addJGraphTVertex(V vertex) {
		
		Hashtable<String, Object> placeStyle = new Hashtable<String, Object>();
		placeStyle.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.LIGHT_GRAY));
		placeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		
		Hashtable<String, Object> transitionStyle = new Hashtable<String, Object>();
		transitionStyle.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.YELLOW));
		transitionStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		
		mxStylesheet stylesheet = getStylesheet();
		stylesheet.putCellStyle("placeStyle", placeStyle);
		stylesheet.putCellStyle("transitionStyle", transitionStyle);
		
		getModel().beginUpdate();
		try {
			mxCell cell = new mxCell(vertex);
			cell.setVertex(true);
			Node n = (Node)vertex;
			cell.setId(n.getId());
			//addCell(cell, defaultParent);
			Object o = null;
			if(n instanceof Place) {
				int placeWidth = 30;
				if(n.getName().length() > 1) placeWidth = n.getName().length() * 10;
				o = insertVertex(defaultParent, n.getId(), n.getName(), 0, 0, placeWidth, 30, "placeStyle");
				
			} else if(n instanceof Transition) {
				int transitionWidth = 30;
				if(n.getName().length() > 1) transitionWidth = n.getName().length() * 10;
				o = insertVertex(defaultParent, n.getId(), n.getName(), 0, 0, transitionWidth, 30, "transitionStyle");
			}
			vertexToObjectMap.put(vertex, o);
			objectToVertexMap.put(o, vertex);
			vertexToCellMap.put(vertex, cell);
			cellToVertexMap.put(cell, vertex);
		} finally {
			getModel().endUpdate();
		}
		
	}
	
	public void addJGraphTEdge(E edge) {
		getModel().beginUpdate();
		try {
			V source = graphT.getEdgeSource(edge);
			V target = graphT.getEdgeTarget(edge);
			mxCell cell = new mxCell(edge);
			cell.setEdge(true);
			cell.setId(null);
			cell.setGeometry(new mxGeometry());
			cell.getGeometry().setRelative(true);
			
			Object o = insertEdge(defaultParent, UUID.randomUUID().toString(), "", vertexToObjectMap.get(source),  vertexToObjectMap.get(target));
			//addEdge(cell, defaultParent, vertexToCellMap.get(source),  vertexToCellMap.get(target), null);
			edgeToObjectMap.put(edge, o);
			objectToEdgeMap.put(o,edge);
			edgeToCellMap.put(edge, cell);
			cellToEdgeMap.put(cell, edge);
			
			
		} finally {
			getModel().endUpdate();
		}
	}

    public HashMap<V, mxCell> getVertexToCellMap() {
        return vertexToCellMap;
    }

    public HashMap<E, mxCell> getEdgeToCellMap() {
        return edgeToCellMap;
    }

    public HashMap<mxCell, E> getCellToEdgeMap() {
        return cellToEdgeMap;
    }

    public HashMap<mxCell, V> getCellToVertexMap() {
    	return cellToVertexMap;
    }

    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e) {
        addJGraphTVertex(e.getVertex());
    }

    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e) {
        mxCell cell = vertexToCellMap.remove(e.getVertex());
        removeCells(new Object[] { cell } );
    }

    @Override
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e) {
        addJGraphTEdge(e.getEdge());
    }

    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e) {
        mxCell cell = edgeToCellMap.remove(e.getEdge());
        removeCells(new Object[] { cell } );
    }

    private void insertJGraphT(Graph<V, E> graphT) {        
        getModel().beginUpdate();
        try {
            for (V vertex : graphT.vertexSet())
                addJGraphTVertex(vertex);
            for (E edge : graphT.edgeSet())
                addJGraphTEdge(edge);
        } finally {
            getModel().endUpdate();
        }

    }
}
