// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 * Demonstrates the usage of {@link JMapViewer}
 *
 * @author Jan Peter Stotz
 *
 */
public class JMapViewerCustom extends JFrame implements JMapViewerEventListener {

    private static final long serialVersionUID = 1L;

    private final JMapViewerTree treeMap;

    private final JLabel zoomLabel;
    private final JLabel zoomValue;

    private final JLabel mperpLabelName;
    private final JLabel mperpLabelValue;
    
    private MapMarkerDot marker;

    /**
     * Constructs the {@code JMapViewerCustom}.
     */
    public JMapViewerCustom(Coordinate currentCord) {
        super("VT Map");
        setSize(600, 600);
        
        treeMap = new JMapViewerTree("Zones");

        map().addJMVListener(this);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();
        
        marker = createAnchorMarker(currentCord);
        map().addMapMarker(marker);
        
        map().setDisplayPosition(currentCord, 15);
        
        mperpLabelName = new JLabel("Toạ độ: ");
        mperpLabelValue = new JLabel(String.format("%s", getCurrentLocation().toString()));

        zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));

        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);
        JLabel helpLabel = new JLabel("Click chuột trái 1 lần để đổi tọa độ,\n "
                + "Lăn chuột giữa để zoom.");
        helpPanel.add(helpLabel);

        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
                new OsmTileSource.Mapnik(),
                //new OsmTileSource.CycleMap(),
                //new OsmTileSource.TransportMap(),
               // new OsmTileSource.LandscapeMap(),
               // new OsmTileSource.OutdoorsMap(),
                //new BingTileSource(),
                new BingAerialTileSource()
        });
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });
        
        map().setTileLoader(new OsmTileLoader(map()));
        panelTop.add(tileSourceSelector);
        
        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(mperpLabelName);
        panelTop.add(mperpLabelValue);

        add(treeMap, BorderLayout.CENTER);
        
        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                    updateAnchorMarkerPosition(e.getPoint());
                    updateZoomParameters();
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (true) map().setToolTipText(map().getPosition(p).toString());
            }
        });
    }

    private JMapViewer map() {
        return treeMap.getViewer();
    }
    
    private MapMarkerDot createAnchorMarker(Coordinate cord) {
        Style customStyle = MapMarkerDot.getDefaultStyle();
        customStyle.setColor(Color.RED);
        customStyle.setBackColor(Color.RED);
        Layer vietnamLayer = treeMap.addLayer("Vietnam");
        return new MapMarkerDot(vietnamLayer, "Toạ độ", cord, customStyle);
    }
    
    private void updateAnchorMarkerPosition(Point p) {
    	ICoordinate iCord = map().getPosition(p);
    	Coordinate cord = new Coordinate(iCord.getLat(), iCord.getLon());
    	map().removeMapMarker(marker);
    	marker = createAnchorMarker(cord);
    	map().addMapMarker(marker);
    }
    /**
     * Get current location
     */
    public Coordinate getCurrentLocation() {
    	return marker.getCoordinate();
    }
    
    public void setCurrentLocation(Coordinate cord) {
    	map().removeMapMarker(marker);
    	marker = createAnchorMarker(cord);
        map().addMapMarker(marker);        
        map().setDisplayPosition(cord, 15);
    }
    /*

    public static void main(String[] args) {
    	mapViewer = new JMapViewerCustom(new Coordinate(10.8533401,106.6144926));
    	mapViewer.setVisible(true);
    }
    private static JMapViewerCustom mapViewer = null;
    
    */
    private void updateZoomParameters() {
        if (mperpLabelValue != null)
            mperpLabelValue.setText(String.format("%s", getCurrentLocation().toString()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }

    @Override
    public void processCommand(JMVCommandEvent command) {
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
    }
}
