package org.openjump.core.ui.plugin.measuretoolbox.icons;

import java.awt.Image;
import javax.swing.ImageIcon;

public class IconLoader {

  public static ImageIcon icon(String filename) {
    return new ImageIcon(IconLoader.class.getResource(filename));
  }

  public static Image image(String filename) {
    return icon(filename).getImage();
  }
}
