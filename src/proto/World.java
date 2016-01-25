

package proto;
import util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;



public class World implements Session.Saveable {

  
  RunGame game;
  String savePath;
  
  Nation nations[];
  Base base;
  
  int currentTime;
  boolean amWatching = false;
  Scene enteredScene = null;
  
  final static String IMG_DIR = "media assets/world map/";
  Image mapImage, alertMarker, selectCircle;
  
  Nation selectedNation;
  Person selectedPerson;
  Facility selectedFacility;
  Object lastSelected;
  
  
  
  World(RunGame game, String savePath) {
    this.game     = game;
    this.savePath = savePath;
    loadMedia();
  }
  
  
  public World(Session s) throws Exception {
    s.cacheInstance(this);
    
    nations      = (Nation[]) s.loadObjectArray(Nation.class);
    base         = (Base) s.loadObject();
    currentTime  = s.loadInt();
    amWatching   = s.loadBool();
    enteredScene = (Scene) s.loadObject();
    
    loadMedia();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObjectArray(nations);
    s.saveObject(base);
    s.saveInt(currentTime);
    s.saveBool(amWatching);
    s.saveObject(enteredScene);
  }
  
  
  void loadMedia() {
    try {
      mapImage     = ImageIO.read(new File(IMG_DIR+"world_map_image.png"));
      alertMarker  = ImageIO.read(new File(IMG_DIR+"alert_symbol.png"   ));
      selectCircle = ImageIO.read(new File(IMG_DIR+"select_circle.png"  ));
    }
    catch (Exception e) {
      I.report(e);
    }
  }
  
  
  void initDefaultNations() {
    int numN = Region.ALL_REGIONS.length;
    this.nations = new Nation[numN];
    for (int n = 0; n < numN; n++) {
      nations[n] = new Nation(Region.ALL_REGIONS[n]);
    }
  }
  
  
  void initDefaultBase() {
    this.base = new Base();
    base.roster.add(new Person(Common.NOCTURNE, "Batman"      ));
    base.roster.add(new Person(Common.KESTREL , "Robin"       ));
    base.roster.add(new Person(Common.CORONA  , "Superman"    ));
    base.roster.add(new Person(Common.GALATEA , "Wonder Woman"));
  }
  
  

  /**  Regular updates and activity cycle:
    */
  void updateWorld() {
    if (enteredScene != null) {
      enteredScene.updateScene();
    }
    else if (amWatching) {
      for (Nation n : nations) {
        
        if (Rand.num() < n.crime && n.mission == null) {
          final Scene s = n.generateCrisis(this);
          n.mission = s;
          amWatching = false;
        }
        
        if (n.mission != null && n.mission.expireTime <= currentTime) {
          n.mission.resolveAsIgnored();
          n.mission = null;
        }
      }
      currentTime += 1;
    }
  }
  
  
  Batch <Scene> missions() {
    Batch <Scene> all = new Batch();
    for (Nation n : nations) if (n.mission != null) all.add(n.mission);
    return all;
  }
  
  
  Batch <Person> assignedToMissions() {
    Batch <Person> all = new Batch();
    for (Scene m : missions()) for (Person p : m.playerTeam) all.add(p);
    return all;
  }
  
  
  void beginNextMission() {
    Scene toEnter = missions().first();
    if (toEnter != null) {
      toEnter.setupScene();
      toEnter.beginScene();
    }
    this.enteredScene = toEnter;
  }
  
  
  void exitFromMission(Scene mission) {
    for (Nation n : nations) if (n.mission == mission) n.mission = null;
    beginNextMission();
  }
  
  
  
  /**  Graphical/display routines:
    */
  void renderTo(Surface surface, Graphics2D g) {
    
    //
    //  Draw the background image first!
    g.drawImage(mapImage, 0, 0, surface.getWidth(), surface.getHeight(), null);
    Nation nationHovered = null;
    Person personHovered = null;
    
    //
    //  Then draw the nations of the world on the satellite map.
    int viewWide = surface.getWidth(), viewHigh = surface.getHeight();
    float mapWRatio = 1, mapHRatio = 1;
    mapWRatio *= mapImage.getWidth (null) * 1f / viewWide;
    mapHRatio *= mapImage.getHeight(null) * 1f / viewHigh;
    
    int mX = surface.mouseX, mY = surface.mouseY;
    mX *= mapWRatio;
    mY *= mapHRatio;
    int pixVal = ((BufferedImage) mapImage).getRGB(mX, mY);
    for (Nation n : nations) if (n.region.colourKey == pixVal) {
      nationHovered = n;
    }
    
    if (nationHovered != null && surface.mouseClicked) {
      this.selectedNation = nationHovered;
      this.lastSelected   = nationHovered;
    }
    
    renderOutline(selectedNation, surface, g, mapWRatio, mapHRatio);
    renderOutline(nationHovered , surface, g, mapWRatio, mapHRatio);
    
    for (Nation n : nations) if (n.mission != null) {
      int x = (int) (n.region.centerX / mapWRatio);
      int y = (int) (n.region.centerY / mapHRatio);
      g.drawImage(alertMarker, x - 25, y - 25, 50, 50, null);
    }
    
    //
    //  Then draw the monitor-status active at the moment-
    int x = (viewWide / 2) - 65, y = 330;
    Batch <Scene> missions = missions();
    
    if (amWatching) {
      g.setColor(Color.ORANGE);
      g.drawString("Monitoring...", x, y);
    }
    else if (! missions.empty()) {
      g.setColor(Color.RED);
      for (Scene m : missions) {
        g.drawString("Crisis: "+m.name, x, y);
        y -= 20;
      }
    }
    else {
      g.setColor(Color.BLUE);
      g.drawString("Monitoring paused...", x, y);
    }
    
    //
    //  And finally, draw the roster for the current base!
    int offX = 360, offY = 360;
    int maxAcross = 300, across = 0, down = 0, size = 64;
    
    for (Person p : base.roster) {
      x = offX + across;
      y = offY + down;
      g.drawImage(p.kind.sprite, x, y, size, size, null);
      if (surface.mouseIn(x, y, size, size)) {
        personHovered = p;
        g.drawImage(selectCircle, x, y, size, size, null);
      }
      if (p.scene != null) {
        g.drawImage(alertMarker, x + size - 20, y + size - 20, 20, 20, null);
      }
      across += size;
      if (across >= maxAcross) { across = 0; down += size; }
    }
    if (personHovered != null && game.surface.mouseClicked) {
      this.selectedPerson = personHovered;
      this.lastSelected   = personHovered;
    }
  }
  
  
  void renderOutline(
    Nation n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.region.outline == null) return;
    Region r = n.region;
    int
      x = (int) (r.outlineX / mapWRatio),
      y = (int) (r.outlineY / mapHRatio),
      w = (int) (r.outline.getWidth (null) / mapWRatio),
      h = (int) (r.outline.getHeight(null) / mapHRatio);
    g.drawImage(r.outline, x, y, w, h, null);
  }
  
  
  String description() {
    final StringBuffer s = new StringBuffer();
    
    final Nation   n = this.selectedNation;
    final Person   p = this.selectedPerson;
    final Facility f = this.selectedFacility;
    
    if (f != null && f == lastSelected) {
      
    }
    
    if (n != null && n == lastSelected) {
      int trustPercent = (int) (n.trust * 100);
      int crimePercent = (int) (n.crime * 100);
      String crisisName = n.mission == null ? "None" : n.mission.name;
      
      s.append("\nRegion:  "+n.region.name);
      s.append("\nFunding: "+n.funding+" M$");
      s.append("\nTrust:   "+trustPercent+"%");
      s.append("\nCrime:   "+crimePercent+"%");
      s.append("\nLeague Member: "+n.member);
      s.append("\n\nCurrent crisis: "+crisisName);
      
      if (n.mission != null) {
        final Scene m = n.mission;
        s.append("\n  Threat level: "+"Moderate");
        s.append("\n  Team Selected (Press 1-9):");
        for (Person t : m.playerTeam) {
          s.append("\n  "+t.name);
        }
        Person picks = null;
        for (int i = 1; i <= 9; i++) {
          if (game.description.isPressed((char) ('0' + i))) {
            picks = base.roster.atIndex(i -1);
          }
        }
        if (picks != null) {
          if (picks.availableForMission()) m.addToTeam(picks);
          else if (picks.scene == m) m.removePerson(picks);
        }
      }
    }
    
    else if (p != null && p == lastSelected) {
      s.append("\nCodename: "+p.name);
      s.append("\nAbilities: ");
      for (Ability a : p.abilities) {
        int level = (int) p.abilityLevels.valueFor(a);
        s.append("\n  "+a.name);
        s.append(" (Level "+level+")");
      }
      String crisisName = p.scene == null ? "None" : p.scene.name;
      s.append("\n\nAssignment: "+crisisName);
    }
    
    s.append("\n");
    if (! assignedToMissions().empty()) {
      s.append("\n  Press M to begin missions.");
      if (game.description.isPressed('m')) {
        beginNextMission();
      }
    }
    else if (! amWatching) {
      s.append("\n  Press M to resume monitoring.");
      if (game.description.isPressed('m')) this.amWatching = true;
    }
    else {
      s.append("\n  Press M to pause monitoring.");
      if (game.description.isPressed('m')) this.amWatching = false;
    }
    s.append("\n  Save (S)");
    if (game.description.isPressed('s')) try {
      Session.saveSession(savePath, this);
    }
    catch (Exception e) { I.report(e); }
    
    return s.toString();
  }
}






