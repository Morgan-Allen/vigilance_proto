

package proto.game.world;
import proto.common.*;
import proto.util.*;
import proto.view.*;



public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  final public String name;
  final public RegionView view = new RegionView();
  String cities[] = {};
  
  float 
    defaultCrime       = 0.25f,
    defaultWealth      = 0.50f,
    defaultEnvironment = 0.75f,
    defaultEducation   = 0.50f,
    defaultEquality    = 0.50f,
    defaultFreedom     = 0.50f;
  
  int     defaultFunding = 100  ;
  float   defaultTrust   = 0.25f;
  boolean defaultMember  = false;
  
  
  final static Region
    PORT_ADAMS     = new Region("Port Adams"    , "region_pa"),
    GOTHAM_CENTRAL = new Region("Gotham Central", "region_gc"),
    THE_TRICORNE   = new Region("The Tricorne"  , "region_tt"),
    WEST_HILL      = new Region("West Hill"     , "region_wh"),
    
    ALL_REGIONS[] = { PORT_ADAMS, GOTHAM_CENTRAL, THE_TRICORNE, WEST_HILL};
    
  
  static {
    final Region PA = PORT_ADAMS;
    PA.view.attachColourKey(-12797832, "PA");
    
    final Region GC = GOTHAM_CENTRAL;
    GC.view.attachColourKey(-5450637 , "GC");
    
    final Region TT = THE_TRICORNE;
    TT.view.attachColourKey(-3874917 , "TT");
    
    final Region WH = WEST_HILL;
    WH.view.attachColourKey(-6040420 , "WH");
  }
  
  
  Region(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static Region loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
}








