

package proto.editor;

import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.view.common.*;
import proto.view.scene.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class EditorView extends UINode implements TileConstants {
  
  
  final static Color
    PLACE_OKAY = new Color(0, 1, 0, 0.33f),
    PLACE_BAD  = new Color(1, 0, 0, 0.33f)
  ;
  
  final SceneView parent;
  Prop placing = null;
  
  
  EditorView(SceneView parent) {
    super(parent);
    this.parent = parent;
  }

  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    g.setColor(new Color(0, 0, 0, 0.66f));
    g.fillRect(vx, vy, vw, vh);
    String desc = description(surface);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(desc, g, vx, vy, vw, vh);
    
    
    
    return true;
  }
  
  
  public Editor editor() {
    World world = mainView.world();
    if (world.game() instanceof Editor) return (Editor) world.game();
    return null;
  }
  

  String description(Surface surface) {
    
    final World  world  = mainView.world();
    final Scene  scene  = world.activeScene();
    final Editor editor = editor();
    
    final StringBuffer s = new StringBuffer();
    
    s.append("\nAvailable props:");
    char key = '1';
    
    
    for (PropType type : editor.propTypes) {
      
      s.append("\n    "+type.name()+" ("+key+")");
      
      if (surface.isPressed(key) && placing.kind() != type) {
        placing = new Prop(type, world);
      }
      
      key += 1;
    }
    
    s.append("\n    Press R to rotate, click to place");
    if (surface.isPressed('r') && placing != null) {
      int facing = (placing.facing() + 2) % 8;
      placing.setFacing(facing);
    }
    
    return s.toString();
  }
  
  
  boolean previewPropPlacement(
    Object hovered, Tile at, Surface surface, Graphics2D g
  ) {
    final World  world  = mainView.world();
    final Scene  scene  = world.activeScene();
    final Editor editor = editor();
    
    if (placing != null) {
      boolean canPlace = Prop.hasSpace(
        scene, placing.kind(), at.x, at.y, placing.facing()
      );
      
      if (canPlace && surface.mouseClicked()) {
        placing.enterScene(scene, at.x, at.y, placing.facing());
      }
      else {
        Color tint = canPlace ? PLACE_OKAY : PLACE_BAD;
        placing.setOrigin(at);
        placing.renderTo(scene, parent, surface, g, tint);
      }
    }
    
    return true;
  }
  
  
  
}









