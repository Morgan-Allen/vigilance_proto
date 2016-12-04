

package proto.common;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.places.*;

import java.awt.EventQueue;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DebugScene ex = new DebugScene();
        ex.setVisible(true);
      }
    });
  }
  
  
  DebugScene() {
    super("saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultNations(world);
    DefaultGame.initDefaultBase   (world);
    DefaultGame.initDefaultCrime  (world);
    
    //  TODO:  At the moment you have a problem where the needs for an initial
    //  goal might be impossible to satisfy (e.g, nobody has any initial
    //  engineering skills, so alarm-crackers can't be found.)
    
    //  So, you'll need some specialists:
    
    //  Civilians with modest random skills
    //  Doctors, for anatomy & pharmacy
    //  Inventors, for engineering & informatics
    //  Politicians, for law/finance & suasion
    //  Police, for combat & social skills
    //  The various individual heroes
    
    //  Goons with some basic muscle
    //  Mobsters, for vehicles, marksman & languages
    //  Fences, for law/finance & suasion
    //  Hitmen, for stealth, disguise & weapon skill
    //  Psychos, for the occult and random expertise
    //  The various individual villains
    
    //  TODO:  You still need to generate tipoffs to indicate a plan is in
    //  progress.
    
    //  TODO:  Also, you need to avoid different bosses interfering with
    //  eachother (if reasonably possible.)
    
    return world;
  }
}




