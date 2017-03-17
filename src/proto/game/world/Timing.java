

package proto.game.world;
import proto.common.*;
import proto.util.*;



public class Timing {
  
  
  final static int MONTH_LENGTHS[] = {
    31,  //  January
    28,  //  February
    31,  //  March
    30,  //  April
    31,  //  May
    30,  //  June
    31,  //  July
    31,  //  August
    30,  //  September
    31,  //  October
    30,  //  November
    31,  //  December
  };
  final public static String MONTH_NAMES[] = {
    "January", "February", "March"    ,
    "April"  , "May"     , "June"     ,
    "July"   , "August"  , "September",
    "October", "November", "December" ,
  };
  
  
  final World world;

  float hoursInTick, timeHours = 0;
  int timeDays = 0, daysMonth = 0, monthsYear = 0, timeYears = 0;
  boolean dayIsUp, monthIsUp;
  
  
  Timing(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    hoursInTick = s.loadFloat();
    timeHours   = s.loadFloat();
    timeDays    = s.loadInt  ();
    daysMonth   = s.loadInt  ();
    monthsYear  = s.loadInt  ();
    timeYears   = s.loadInt  ();
    dayIsUp     = s.loadBool ();
    monthIsUp   = s.loadBool ();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveFloat(hoursInTick);
    s.saveFloat(timeHours  );
    s.saveInt  (timeDays   );
    s.saveInt  (daysMonth  );
    s.saveInt  (monthsYear );
    s.saveInt  (timeYears  );
    s.saveBool (dayIsUp    );
    s.saveBool (monthIsUp  );
  }
  
  
  
  /**  Regular updates and initial setup-
    */
  public void setStartDate(int days, int month, int year) {
    this.timeHours  = 0;
    this.timeDays   = 0;
    this.daysMonth  = days  - 1;
    this.monthsYear = month - 1;
    this.timeYears  = year;
  }
  
  
  public void advanceTime(float hoursGone) {
    hoursInTick = hoursGone;
    timeHours += hoursInTick;
    dayIsUp = monthIsUp = false;
    
    while (timeHours > World.HOURS_PER_DAY) {
      timeDays++;
      daysMonth++;
      timeHours -= World.HOURS_PER_DAY;
      dayIsUp = true;
      
      if (dayIsUp && daysMonth >= daysInMonth()) {
        daysMonth = 0;
        monthsYear++;
        monthIsUp = true;
      }
      
      if (monthIsUp && monthsYear >= 12) {
        monthsYear = 0;
        timeYears++;
      }
    }
  }
  
  
  void updateTiming() {
    final float realGap = 1f / RunGame.FRAME_RATE;
    advanceTime(realGap * World.GAME_HOURS_PER_REAL_SECOND);
  }
  
  
  int daysInMonth() {
    if (timeYears % 4 == 0 && monthsYear == 1) return 29;
    return MONTH_LENGTHS[monthsYear];
  }
  
  
  public int timeYears() {
    return timeYears;
  }
  
  
  public int monthInYear() {
    return monthsYear;
  }
  
  
  public int dayInMonth() {
    return daysMonth;
  }
  
  
  public int timeDays() {
    return timeDays;
  }
  
  
  public int timeHours() {
    return (int) timeHours;
  }
  
  
  public int timeMinutes() {
    return (int) ((timeHours % 1) * World.MINUTES_PER_HOUR);
  }
  
  
  public int totalHours() {
    return (timeDays * 24) + (int) (timeHours);
  }
  
  
  public boolean dayIsUp() {
    return dayIsUp;
  }
  
  
  public boolean monthIsUp() {
    return monthIsUp;
  }
  
  
  public float hoursInTick() {
    return hoursInTick;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String currentTimeString() {
    int minutes = timeMinutes();
    int hours   = timeHours  ();
    int day     = dayInMonth ();
    int month   = monthInYear();
    int year    = timeYears  ();
    String hourString = ""+I.lengthen(hours  , 2, true);
    String minsString = ""+I.lengthen(minutes, 2, true);
    String monthName = Timing.MONTH_NAMES[month];
    return hourString+":"+minsString+", "+monthName+" "+day+", "+year;
  }
  
  
  public String timeString(int totalHours) {
    int totalDays = totalHours / 24;
    int hours = totalHours % 24;
    String hourString = ""+I.lengthen(hours, 2, true);
    return hourString+":00 on Day "+totalDays;
  }
}





