

package proto.view;
import proto.util.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public abstract class MessageView {
  
  
  WorldView parent;
  Box2D viewBounds;
  Color background;
  
  Image mainImage;
  String title;
  String mainText;
  String options[];
  
  
  protected MessageView(
    Image mainImage, String title, String mainText, String... options
  ) {
    viewBounds = new Box2D();
    background = new Color(0, 0, 0, 0.75f);
    this.mainImage = mainImage;
    this.title     = title    ;
    this.mainText  = mainText ;
    this.options   = options  ;
  }
  
  
  void attachTo(WorldView parent, int wide, int high) {
    this.parent = parent;
    this.viewBounds.set(600 - (wide / 2), 300 - (high / 2), wide, high);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    
    final Box2D b = this.viewBounds;
    final int
      vx = (int) b.xpos(),
      vy = (int) b.ypos(),
      vw = (int) b.xdim(),
      vh = (int) b.ydim()
    ;
    
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
      
      final boolean hovered = surface.mouseIn(
        vx + 5, vy + down + 5, vw - 10, 15, this
      );
      if (hovered) g.drawRect(vx + 3, vy + down + 3, vw - 6, 19);
      if (hovered && surface.mouseClicked(this)) whenClicked(options[i], i);
    }
  }
  
  
  protected abstract void whenClicked(String option, int optionID);
  
}










