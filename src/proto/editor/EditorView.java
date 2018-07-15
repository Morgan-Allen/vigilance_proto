

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
  int defaultFacing = N;
  
  
  public EditorView(SceneView parent) {
    super(parent);
    this.parent = parent;
  }

  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    GameSettings.debugScene = true;
    GameSettings.pauseScene = true;
    
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
    
    s.append("\n  Available props:");
    char key = '1';
    for (PropType type : editor.propTypes) {
      s.append("\n    "+type.name()+" ("+key+")");
      if (surface.isPressed(key) && (placing == null || placing.kind() != type)) {
        placing = new Prop(type, world);
        placing.setFacing(defaultFacing);
      }
      key += 1;
    }
    
    s.append("\n");
    
    s.append("\n  Press R to rotate, click to place");
    if (surface.isPressed('r') && placing != null) {
      int facing = defaultFacing = (placing.facing() + 2) % 8;
      placing.setFacing(facing);
    }
    s.append("\n  Press D to deselect");
    if (surface.isPressed('d')) {
      placing = null;
    }
    s.append("\n  Press Z to un/zoom");
    if (surface.isPressed('z')) {
      parent.setTileZoom(! parent.tileZoomed());
    }
    
    s.append("\n\n  Press X to export");
    if (surface.isPressed('x')) {
      editor.exportToXML(scene);
    }
    
    return s.toString();
  }
  
  
  public boolean previewPropPlacement(
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
        placing = new Prop(placing.kind(), world);
        placing.setFacing(defaultFacing);
      }
      else {
        Color tint = canPlace ? PLACE_OKAY : PLACE_BAD;
        placing.setOrigin(at);
        placing.renderTo(scene, parent, surface, g, tint);
      }
    }
    else if (surface.mouseClicked()) {
      parent.setZoomPoint(at);
    }
    
    return true;
  }
  
  
  
}









