

package proto.view;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public abstract class MessageView extends UINode {
  
  
  Color background;
  Image mainImage;
  String title;
  String mainText;
  String options[];
  
  
  protected MessageView(
    UINode parent,
    Image mainImage, String title, String mainText, String... options
  ) {
    super(parent);
    background = new Color(0, 0, 0, 0.75f);
    this.mainImage = mainImage;
    this.title     = title    ;
    this.mainText  = mainText ;
    this.options   = options  ;
  }
  
  
  void attachAt(int wide, int high) {
    this.relBounds.set(600 - (wide / 2), 300 - (high / 2), wide, high);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    g.setColor(background);
    g.fillRect(vx, vy, vw, vh);
    g.setColor(Color.WHITE);
    g.drawRect(vx, vy, vw, vh);
    
    int optionsSize = options.length * 15;
    
    g.drawString(title, vx + 5, vy + 20);
    ViewUtils.drawWrappedString(
      mainText, g,
      vx + 5, vy + 25, vw - 10, vh - (25 + 10 + optionsSize)
    );
    
    int down = vh - (optionsSize + 10);
    for (int i = 0; i < options.length; i++) {
      g.drawString(options[i], vx + 5, vy + down + 5 + 15);
      
      final boolean hovered = surface.tryHover(
        vx + 5, vy + down + 5, vw - 10, 15, this
      );
      if (hovered) g.drawRect(vx + 3, vy + down + 3, vw - 6, 19);
      if (hovered && surface.mouseClicked()) whenClicked(options[i], i);
    }
  }
  
  
  protected abstract void whenClicked(String option, int optionID);
  
}










