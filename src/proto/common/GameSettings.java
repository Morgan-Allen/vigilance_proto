

package proto.common;
import proto.game.world.World;


public class GameSettings {
  
  final public static boolean
    EASY_MODE     = true
  ;
  
  final public static int
    WEEK_IN_HOURS = World.HOURS_PER_DAY * World.DAYS_PER_WEEK,
    DEF_ABILITY_TRAIN_TIME = WEEK_IN_HOURS * 1,
    MIN_PLOT_THINKING_TIME = WEEK_IN_HOURS * 1,
    MAX_PLOT_THINKING_TIME = WEEK_IN_HOURS * 2,
    MIN_PLOT_STEP_DELAY    = World.HOURS_PER_DAY * 1,
    MAX_PLOT_STEP_DELAY    = World.HOURS_PER_DAY * 2
  ;
  
  public static boolean
    freeTipoffs     = false,
    noTipoffs       = false,
    noTrustDecay    = false,
    noDeterDecay    = false
  ;
  
  public static boolean
    reportMediaMiss = false,
    reportWorldInit = false,
    debugScene      = false,
    pauseScene      = false,
    debugLineSight  = false,
    viewSceneBlocks = false,
    eventsVerbose   = false
  ;
  
}
