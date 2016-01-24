

package proto;
import util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;



public class World {
  
  Nation nations[];
  Base base;
  
  RunGame game;
  int currentTime;
  boolean amWatching = false;
  Scene enteredScene = null;
  
  final static String IMG_DIR = "media assets/world map/";
  Image mapImage, alertMarker, selectCircle;
  
  Nation selectedNation;
  Person selectedPerson;
  Object lastSelected;
  ///Scene  selectedMission;
  
  
  World(RunGame game) {
    this.game = game;
    loadMedia();
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
    
    Nation NA = new Nation();
    NA.name = "North America";
    NA.cities = new String[] { "Toronto", "Metropolis", "Gotham" };
    NA.colourKey = -38656;
    NA.loadOutline("NA_outline.png", 136, 82);
    NA.fundingLevel = 200;
    NA.trust = 0.5f;
    NA.leagueMember = true;
    
    Nation SA = new Nation();
    SA.name = "South America";
    SA.cities = new String[] { "Brazilia", "Buenos Aires", "Atlantis"};
    SA.colourKey = -11740822;
    SA.loadOutline("SA_outline.png", 463, 337);
    SA.fundingLevel = 150;
    
    Nation AF = new Nation();
    AF.name = "Africa";
    AF.cities = new String[] { "Capetown", "Cairo", "Timbuktu" };
    AF.colourKey = -989079;
    AF.loadOutline("AF_outline.png", 665, 243);
    AF.fundingLevel = 120;
    
    Nation EU = new Nation();
    EU.name = "Europe";
    EU.cities = new String[] { "London", "Paris", "Themyscira" };
    EU.colourKey = -3956789;
    EU.loadOutline("EU_outline.png", 663, 96);
    EU.fundingLevel = 180;
    
    Nation RU = new Nation();
    RU.name = "Soviet Bloc";
    RU.cities = new String[] { "Moscow", "Leningrad", "Berlin" };
    RU.colourKey = -3162470;
    RU.loadOutline("RU_outline.png", 843, 54);
    RU.fundingLevel = 150;
    
    Nation OC = new Nation();
    OC.name = "Oceania";
    OC.cities = new String[] { "Perth", "Singapore", "Tokyo" };
    OC.colourKey = -9335063;
    OC.loadOutline("OC_outline.png", 926, 160);
    OC.fundingLevel = 150;
    
    this.nations = new Nation[] { NA, SA, AF, EU, RU, OC };
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
    for (Nation n : nations) if (n.colourKey == pixVal) nationHovered = n;
    
    if (nationHovered != null && surface.mouseClicked) {
      this.selectedNation = nationHovered;
      this.lastSelected   = nationHovered;
    }
    
    renderOutline(selectedNation, surface, g, mapWRatio, mapHRatio);
    renderOutline(nationHovered , surface, g, mapWRatio, mapHRatio);
    
    for (Nation n : nations) if (n.mission != null) {
      int x = n.outlineX + (n.outline.getWidth (null) / 2);
      int y = n.outlineY + (n.outline.getHeight(null) / 2);
      x /= mapWRatio;
      y /= mapHRatio;
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
    if (n == null || n.outline == null) return;
    int
      x = (int) (n.outlineX / mapWRatio),
      y = (int) (n.outlineY / mapHRatio),
      w = (int) (n.outline.getWidth (null) / mapWRatio),
      h = (int) (n.outline.getHeight(null) / mapHRatio);
    g.drawImage(n.outline, x, y, w, h, null);
  }
  
  
  String description() {
    final StringBuffer s = new StringBuffer();
    
    final Nation n = this.selectedNation;
    final Person p = this.selectedPerson;
    
    if (n != null && n == lastSelected) {
      int trustPercent = (int) (n.trust * 100);
      int crimePercent = (int) (n.crime * 100);
      String crisisName = n.mission == null ? "None" : n.mission.name;
      
      s.append("\nRegion:  "+n.name);
      s.append("\nFunding: "+n.fundingLevel+" M$");
      s.append("\nTrust:   "+trustPercent+"%");
      s.append("\nCrime:   "+crimePercent+"%");
      s.append("\nLeague Member: "+n.leagueMember);
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
    
    
    return s.toString();
  }
}


class Nation {
  
  String name;
  String cities[] = {};
  
  int colourKey;
  Image outline;
  int outlineX, outlineY;
  
  float trust = 0.25f;
  float crime = 0.25f;
  boolean leagueMember;
  int fundingLevel;
  
  Scene mission;
  
  
  
  /**  Life cycle and update methods-
    */
  Scene generateCrisis(World world) {
    final Scene s = new UrbanScene(world, 100);
    s.name = "Hostage situation in "+Rand.pickFrom(cities);
    s.expireTime = world.currentTime + 1 + Rand.index(3);
    s.site = this;
    return s;
  }
  
  
  
  /**  Rendering and debug methods-
    */
  void loadOutline(String imgFile, int offX, int offY) {
    try {
      outline = ImageIO.read(new File(World.IMG_DIR+imgFile));
      outlineX = offX;
      outlineY = offY;
    }
    catch (Exception e) {
      I.report(e);
    }
  }
}






