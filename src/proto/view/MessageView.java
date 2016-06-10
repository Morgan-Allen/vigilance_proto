

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
    
    final int w = mainView.vw, h = mainView.vh;
    this.relBounds.set(w / 4, h / 4, w / 2, h / 2);
  }
  
  
  void renderTo(Surface surface, Graphics2D g) {
    surface.tryHover(0, 0, surface.getWidth(), surface.getWidth(), this);
    
    g.setColor(background);
    g.fillRect(vx, vy, vw, vh);
    g.setColor(Color.WHITE);
    g.drawRect(vx, vy, vw, vh);
    
    int optionsSize = options.length * 20;
    g.drawString(title, vx + 5, vy + 20);
    renderContent(surface, g, optionsSize);
    
    int down = vh - (optionsSize + 10);
    
    for (int i = 0; i < options.length; i++) {
      final int index = i;
      final MessageView view = this;
      
      final StringButton button = new StringButton(
        options[i], 5, down, vw - 10, 20, this
      ) {
        void whenClicked() {
          view.whenClicked(options[index], index);
        }
      };
      button.valid = optionValid(index);
      button.refers = options[index];
      button.updateAndRender(surface, g);
      down += 20;
    }
  }
  
  
  protected void renderContent(Surface surface, Graphics2D g, int optionsSize) {
    ViewUtils.drawWrappedString(
      mainText, g,
      vx + 5, vy + 25, vw - 10, vh - (25 + 10 + optionsSize)
    );
  }
  
  
  protected boolean optionValid(int optionID) {
    return true;
  }
  
  
  protected abstract void whenClicked(String option, int optionID);
  
}










