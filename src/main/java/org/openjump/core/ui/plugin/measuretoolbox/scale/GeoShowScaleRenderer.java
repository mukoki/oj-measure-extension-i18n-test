package org.openjump.core.ui.plugin.measuretoolbox.scale;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.openjump.core.ui.plugin.measuretoolbox.plugins.ToolboxMeasurePlugIn;
import org.openjump.core.ui.plugin.measuretoolbox.utils.CoordinateListMetrics_extended;
import org.openjump.core.ui.util.ScreenScale;

import org.locationtech.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;

public class GeoShowScaleRenderer extends SimpleRenderer {

	/**
	 * - Modified from ShowScaleRenderer.class to work with Geographic coordinates (EPSG 4326)
     * @author Giuseppe Aruta - Sept 1th 2015)
	*/
    public final static String CONTENT_ID = "GEO_SCALE_SHOW";
    /**
     * Height of the increment boxes, in view-space units.
     */
    private final static int BAR_HEIGHT = 13;
    private final static Color FILL1 = Color.WHITE;

    /**
     * In view-space units; the actual increment may be a bit larger or smaller
     * than this amount.
     */
    private final static Color LINE_COLOR = Color.GRAY;
    private final static int TEXT_BOTTOM_MARGIN = 1;
    private final static Color TEXT_COLOR = Color.black;

    /**
     * Distance from the bottom edge, in view-space units.
     */
    private final int FONTSIZE = 14;
    private final static int VERTICAL_MARGIN = 22;
    private final static String ENABLED_KEY = "GEO_SCALE_SHOW_ENABLED";
    private Font FONT = new Font("Dialog", Font.BOLD, FONTSIZE);
    private Font FONT2 = new Font("Dialog", Font.BOLD + Font.ITALIC, FONTSIZE);
    private Stroke stroke = new BasicStroke();
    private static int resolution = 96;

    public GeoShowScaleRenderer(LayerViewPanel panel) {
        super(CONTENT_ID, panel);
    }

    public static double screenScale;

    @Override
	protected void paint(Graphics2D g) {
        if (!isEnabled(panel)) {
            return;
        }

        // Override dashes set in GridRenderer [Jon Aquino]
        g.setStroke(stroke);
        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
        	 

         screenScale = getHorizontalGeographicMapScale(panel.getViewport());

        } else {

        	 
           screenScale = getHorizontalProjectedMapScale(panel.getViewport());
        }

        paintScaleLabel(g, screenScale);
    }

    public static double getScale(Viewport port) {
        double horizontalScale = 0;
        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
            double INCHTOCM = 2.54; // cm
            double panelWidth = port.getPanel().getWidth(); // pixel
            Envelope envelope = port.getEnvelopeInModelCoordinates();
            double minx = envelope.getMinX();
            double maxx = envelope.getMaxX();
            double miny = envelope.getMinY();
            double maxy = envelope.getMaxY();
            double modelWidth = CoordinateListMetrics_extended.computeGeographicLenght(maxy, maxx, miny, minx);
            horizontalScale = modelWidth * 100
                    / (INCHTOCM / resolution * panelWidth);
        } else {
            double INCHTOCM = 2.54; // cm
            double panelWidth = port.getPanel().getWidth(); // pixel
            double modelWidth = port.getEnvelopeInModelCoordinates().getWidth(); // m
            horizontalScale = modelWidth * 100
                    / (INCHTOCM / resolution * panelWidth);
        }
        return horizontalScale;
    }

    public static double getHorizontalGeographicMapScale(Viewport port) {

        double horizontalScale = 0;

        double INCHTOCM = 2.54; // cm

        double panelWidth = port.getPanel().getWidth(); // pixel
        Envelope envelope = port.getEnvelopeInModelCoordinates();
        double minx = envelope.getMinX();
        double maxx = envelope.getMaxX();
        double miny = envelope.getMinY();
        double maxy = envelope.getMaxY();
        double modelWidth =  CoordinateListMetrics_extended.computeGeographicLenght(maxy, maxx, miny, minx);
        //double modelWidth = Formula.VicentyDistance(maxy, maxx, miny, minx);
        // -----
        // example:
        // screen resolution: 72 dpi
        // 1 inch = 2.54 cm
        // ratio = 2.54/72 (cm/pix) ~ 0.35mm
        // mapLength[cm] = noPixel * ratio
        // scale = realLength *100 [m=>cm] / mapLength
        // -----
        horizontalScale = modelWidth * 100
                / (INCHTOCM / resolution * panelWidth);

        return horizontalScale;
    }

    public static double getHorizontalProjectedMapScale(Viewport port) {

        double horizontalScale = 0;

        double INCHTOCM = 2.54; // cm

        double panelWidth = port.getPanel().getWidth(); // pixel
        double modelWidth = port.getEnvelopeInModelCoordinates().getWidth(); // m
        // -----
        // example:
        // screen resolution: 72 dpi
        // 1 inch = 2.54 cm
        // ratio = 2.54/72 (cm/pix) ~ 0.35mm
        // mapLength[cm] = noPixel * ratio
        // scale = realLength *100 [m=>cm] / mapLength
        // -----
        horizontalScale = modelWidth * 100
                / (INCHTOCM / resolution * panelWidth);

        return horizontalScale;
    }

    private int barBottom() {
        return panel.getHeight() - VERTICAL_MARGIN;
    }

    private int barTop() {
        return barBottom() - BAR_HEIGHT;
    }

    private TextLayout createTextLayout(String text, Font font, Graphics2D g) {
        return new TextLayout(text, font, g.getFontRenderContext());
    }

    public String text;

    
    
 
 /*   
    private void paintScaleLabel(Graphics2D g, double scale) {

        Integer scaleD = new Integer((int) Math.floor(scale));
        
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
                .getInstance();
 

        
        String formatPattern = "#,##0";
              
        decimalFormat.applyPattern(formatPattern);
       
        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
            text = "1 : " + decimalFormat.format(scaleD.doubleValue());}
        else {
        	
        	 text = "1 : " + decimalFormat.format(scaleD.doubleValue());
        }
        
        int length = text.length();

        
        Rectangle2D.Double shape = new Rectangle2D.Double(panel.getWidth() / 2
                - (length + 13) * 3.6, barTop(), (length + 24) * 3.6 - 3,
                barBottom() - barTop());

        
        g.setColor(FILL1);
        g.fill(shape);
        g.setColor(LINE_COLOR);
        g.draw(shape);
 
        Font font;
        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
        	font = FONT2;
        } else{
       font = FONT;}

        g.setColor(TEXT_COLOR);

        int textBottomMargin = TEXT_BOTTOM_MARGIN;

        TextLayout layout = createTextLayout(text, font, g);
        layout.draw(g, (float) (panel.getWidth() / 2 - (length + 11) * 3.6),
                barBottom() - textBottomMargin);
    }

 */
    
    
    private void paintScaleLabel(Graphics2D g, double scale) {
        
    	Integer scaleD = new Integer((int)Math.floor(scale));
    	 DecimalFormat decimalFormat = (DecimalFormat) NumberFormat
                 .getInstance();
    	
       String formatPattern = "#,##0";
              
        decimalFormat.applyPattern(formatPattern);
       
        if (ToolboxMeasurePlugIn.coordinateCheck.isSelected()) {
            text = "1 : " + decimalFormat.format(scaleD.doubleValue());}
        else {
        	
        	 text = "1 : " + decimalFormat.format(scaleD.doubleValue());
        }
        int length = text.length();

    	 
        Rectangle2D.Double shape =
            new Rectangle2D.Double(panel.getWidth()- (length+13)*3.6, 
            						barTop(), (length+12)*3.6-3, barBottom() - barTop());
        g.setColor(FILL1);
        g.fill(shape);
        g.setColor(LINE_COLOR);
        g.draw(shape);
        
        Font font = FONT;
        g.setColor(TEXT_COLOR);

        int textBottomMargin = TEXT_BOTTOM_MARGIN;

        TextLayout layout = createTextLayout(text, font, g);
        layout.draw(g,
                (float) (panel.getWidth()- (length+11)*3.6),
                (float) (barBottom() - textBottomMargin));
      	
    }
   
    
    
    /*********** getters and setters ******************/

    /**
     * 
     * @param panel
     * @return true if the scale is enabled in the LayerViewPanel
     */
    public static boolean isEnabled(LayerViewPanel panel) {
        return panel.getBlackboard().get(ENABLED_KEY, false);
    }

    public static void setEnabled(boolean enabled, LayerViewPanel panel) {
        panel.getBlackboard().put(ENABLED_KEY, enabled);
    }

    /**
     * @param myPlugInContext
     *            The myPlugInContext to set.
     */

}
