/**  
  *  Written by Morgan Allen.
  *  I intend to slap on some kind of open-source license here in a while, but
  *  for now, feel free to poke around for non-commercial purposes.
  */
package proto.util;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;



/**  This class is used to provide shorthand versions of various print output
  *  functions.
  *  (The name is intended to be as terse as possible.)
  *  TODO:  You need to have a logging system that allows various classes to
  *         be toggled on and off for reports.
  *  TODO:  Try scanning for static 'verbose' fields in all classes?
  */
public class I {
  
  
  public static boolean mute = false;
  public static Object talkAbout = null;
  public static boolean used60Frames = false;
  
  final public static boolean
    AM_INSIDE_JAR ,
    WRITE_TO_LOG  ,
    LOG_BIG_EVENTS = true;
  
  static {
    
    final java.net.URL toThis = I.class.getResource("I.class");
    final boolean isJar =
      toThis.toString().startsWith("jar:" ) ||
      toThis.toString().startsWith("rsrc:");
    AM_INSIDE_JAR = isJar;
    WRITE_TO_LOG  = isJar;
    
    System.out.println("\nPATH TO SELF IS: "+toThis);
    System.out.println("  IS JAR FILE?     "+isJar );
    
    if (WRITE_TO_LOG) try {
      String
        date    = new java.util.Date().toString(),
        outPath = "saves/log_output_"+date+".txt";
      outPath = outPath.replace(" ", "_");
      outPath = outPath.replace(":", "_");
      System.out.println("  LOG OUTPUT PATH: "+outPath);
      
      final File outFile = new File(outPath);
      if (! outFile.exists()) outFile.createNewFile();
      
      final PrintStream logOutput = new PrintStream(outFile) {
        public void finalize() {
          this.flush();
        }
      };
      System.setOut(logOutput);
      System.setErr(logOutput);
    }
    catch (IOException e) {
      System.out.println("COULD NOT OPEN LOG FILE! "+e);
    }
  }
  
  
  public static final boolean amDebug() {
    return WRITE_TO_LOG;
  }
  
  
  public static final boolean logEvents() {
    return LOG_BIG_EVENTS;
  }
  
  
  public static final void add(String s) {
    if (! mute) {
      System.out.print(s);
    }
  }
  
  
  public static final void say(String s) {
    if (! mute) {
      System.out.print("\n");
      System.out.print(s);
    }
  }
  
  
  public static final void complain(String e) {
    say(e);
    throw new RuntimeException(e);
  }
  
  
  private static void reportStackTrace(Exception e) {
    String trace = "";
    for (Object o : e.getStackTrace()) trace+="\n  "+o;
    say("  STACK TRACE: "+trace);
  }
  
  
  public static void report(Exception e) {
    say("\nERROR:  "+e);
    reportStackTrace(e);
  }
  
  
  public static void reportStackTrace() {
    reportStackTrace(new Exception());
  }
  
  
  public static boolean matchOrNull(Object o, Object with) {
    return with == null || with.equals(o);
  }
  
  
  public static Object cast(Object o, Class type) {
    if (o == null || type == null) return o;
    if (type.isAssignableFrom(o.getClass())) return o;
    return null;
  }
  
  
  public static void amMute(boolean m) { mute = m; }
  
  
  
  /**  A few utility printing methods-
    */
  public static String shorten(float f, int decimals) {
    final float margin = 1f / Nums.pow(10, decimals);
    if (Nums.abs(f) < margin) return "0";
    final boolean neg = f < 0;
    if (neg) f *= -1;
    final int i = (int) f;
    final float r = f - i;
    if (r < margin) return (neg ? "-" : "")+i;
    final String fraction = r+"";
    final int trim = Nums.min(decimals + 2, fraction.length());
    return (neg ? "-" : "")+i+(fraction.substring(1, trim));
  }
  
  
  public static String lengthen(int i, int decimals, boolean whole) {
    String s = whole ? ""+i : ""+(float) i;
    if (whole) while (s.length() < decimals) s = "0"+s;
    else while (s.length() < decimals) s+="0";
    return s;
  }
  
  
  public static String tagHash(Object o) {
    if (o == null) return "NULL";
    return o+" "+o.hashCode();
  }
  
  
  public static String shorten(String s, int maxLen) {
    if (s == null || s.length() <= maxLen || s.length() <= 3) return s;
    return s.substring(0, maxLen - 3)+"...";
  }
  
  
  public static String signNum(int num) {
    if (num >= 0) return "+"+num;
    else return "-"+(0 - num);
  }
  
  
  public static String list(Object array[]) {
    if (array == null) return "NULL";
    final StringBuffer s = new StringBuffer();
    
    for (int i = 0; i < array.length; i++) {
      Object o = array[i];
      final String pads = i == (array.length - 1) ? "" : ", ";
      s.append(o+pads);
    }
    return s.toString();
  }
  
  
  
  /**  This one could use a little explanation.  Basically, the header gets
    *  printed first, the lineFeed gets stuck on the front of every subsequent
    *  line, and the printArgs should consist of alternating string-labels
    *  and referenced variables (which can be anything.)  Like so:
    *  
    *  "Label1", variableOne,
    *  "Label2", variableTwo,
    *  "Label3", variableThree,
    *  etc...
    *  
    *  Labels can be of different lengths, but automatic pretty-printing will
    *  be applied (so that variables line up in the same column.)
    */
  public static void reportVars(
    String header, String lineFeed, Object... printArgs
  ) {
    
    final int numRows = printArgs.length / 2;
    final String labels[] = new String[numRows];
    final Object refers[] = new Object[numRows];
    int maxLen = 0;
    
    for (int i = 0, n = 0; i < numRows; i++) {
      labels[i] = (String) printArgs[n++];
      refers[i] = n >= printArgs.length ? null : printArgs[n++];
      maxLen = Nums.max(maxLen, labels[i].length());
    }

    I.say(header);
    for (int i = 0; i < numRows; i++) {
      I.say(lineFeed+""+padToLength(labels[i], maxLen)+": "+refers[i]);
    }
  }
  
  
  public static String padToLength(String s, int toLength) {
    final StringBuffer b = new StringBuffer(s);
    for(int n = toLength; n-- > s.length();) b.append(' ');
    return b.toString();
  }
  
  
  
  /**  Console input-
    */
  public static String listen() {
    final StringBuffer b = new StringBuffer();
    while (true) try {
      final char c = (char) System.in.read();
      if (c == '\n') break;
      b.append(c);
    }
    catch (Exception e) { return ""; }
    return b.toString();
  }
  
  
  
  /**  Reflection shortcuts-
    */
  public static Method findMethod(
    Class <? extends Object> baseClass, String name, Class <?>... params
  ) {
    //
    //  Direct reflection access can either give us inherited methods or non-
    //  public methods, but not both, so we have to dig down recursively when
    //  looking out for matches.
    Method match = null;
    try { match = baseClass.getDeclaredMethod(name, params); }
    catch (Exception e) {
      if (baseClass == Object.class) return null;
    }
    if (match != null) {
      match.setAccessible(true);
      return match;
    }
    else return findMethod(baseClass.getSuperclass(), name);
  }
  
  
  
  /**  Visual presentations-
    */
  private final static int
    MODE_GREY   = 0,
    MODE_COLOUR = 1;
  private static Table <String, Presentation> windows = new Table();
  
  
  
  private static class Presentation extends JFrame implements MouseListener {
    
    final static long serialVersionUID = 0;
    
    private Object data;
    private int dataWide, dataHigh;
    private int mode;
    private float min, max;
    
    private boolean isClicked;
    
    
    Presentation(String name, Object data, int mode) {
      super(name);
      this.data = data;
      this.mode = mode;
      this.addMouseListener(this);
    }
    
    
    public void paint(Graphics g) {
      super.paint(g);
      if (mode == MODE_GREY  ) paintGrey  (g);
      if (mode == MODE_COLOUR) paintColour(g);
    }
    
    
    public void mouseClicked(MouseEvent arg0) {
      this.isClicked = true;
    }
    
    
    public void mouseEntered (MouseEvent arg0) {}
    public void mouseExited  (MouseEvent arg0) {}
    public void mousePressed (MouseEvent arg0) {}
    public void mouseReleased(MouseEvent arg0) {}


    private void paintGrey(Graphics g) {
      final byte scale[] = new byte[256];
      for (int s = 256; s-- > 0;) {
        scale[s] = (byte) s;
      }
      float vals[][] = (float[][]) data;
      final int w = dataWide = vals.length, h = dataHigh = vals[0].length;
      
      final byte byteData[] = new byte[w * h];
      for (Coord c : Visit.grid(0, 0, w, h, 1)) {
        final float pushed = (vals[c.x][c.y] - min) / (max - min);
        final int grey = (int) Nums.clamp(pushed * 255, 0, 255);
        byteData[imgIndex(c.x, c.y, w, h)] = scale[grey];
      }
      presentImage(g, byteData, BufferedImage.TYPE_BYTE_GRAY, w, h);
    }
    
    
    private void paintColour(Graphics g) {
      final int vals[][] = (int[][]) data;
      final int w = dataWide = vals.length, h = dataHigh = vals[0].length;
      
      final int intData[] = new int[w * h];
      for (Coord c : Visit.grid(0, 0, w, h, 1)) {
        intData[imgIndex(c.x, c.y, w, h)] = vals[c.x][c.y];
      }
      presentImage(g, intData, BufferedImage.TYPE_INT_ARGB, w, h);
    }
    
    
    private int imgIndex(int x, int y, int w, int h) {
      return ((h - (y + 1)) * w) + x;
    }
    
    
    private void presentImage(
      Graphics g, Object imgData, int imageMode, int w, int h
    ) {
      final BufferedImage image = new BufferedImage(w, h, imageMode);
      image.getRaster().setDataElements(0, 0, w, h, imgData);
      final Container pane = this.getContentPane();
      g.drawImage(
        image,
        0, this.getHeight() - pane.getHeight(),
        pane.getWidth(), pane.getHeight(),
        null
      );
    }
  }
  
  
  public static void present(
    float greyVals[][],
    String name, int w, int h, float min, float max
  ) {
    final Presentation p = present(greyVals, MODE_GREY, name, w, h);
    p.min = min;
    p.max = max;
  }
  
  
  public static void present(
    int colourVals[][],
    String name, int w, int h
  ) {
    final Presentation p = present(colourVals, MODE_COLOUR, name, w, h);
  }
  
  
  private static Presentation present(
    Object vals, int mode,
    String name, int w, int h
  ) {
    Presentation window = windows.get(name);
    if (window == null) {
      window = new Presentation(name, vals, mode);
      window.getContentPane().setPreferredSize(new Dimension(w, h));
      window.pack();
      window.setVisible(true);
      windows.put(name, window);
    }
    else {
      window.data = vals;
      window.getContentPane().setPreferredSize(new Dimension(w, h));
      window.pack();
      window.repaint();
    }
    return window;
  }
  
  
  public static Coord getDataCursor(String windowName, boolean report) {
    final Presentation window = windows.get(windowName);
    if (window == null) return new Coord(0, 0);
    
    final Container pane = window.getContentPane();
    final Point loc      = MouseInfo.getPointerInfo().getLocation();
    final Point corner   = pane.getLocationOnScreen();

    int x = loc.x - corner.x, y = loc.y - corner.y;
    x *= window.dataWide * 1f / pane.getWidth ();
    y *= window.dataHigh * 1f / pane.getHeight();
    x = Nums.clamp(x                        , window.dataWide);
    y = Nums.clamp(window.dataHigh - (y + 1), window.dataHigh);
    
    if (report) {
      I.say("\nGetting cursor:");
      I.say("  Screen location: "+loc);
      I.say("  Window origin:   "+corner);
      I.say("  X and Y within data: "+x+"/"+y);
    }
    return new Coord(x, y);
  }
  
  
  public static boolean checkMouseClicked(String windowName) {
    final Presentation window = windows.get(windowName);
    if (window == null) return false;
    
    final boolean clicked = window.isClicked;
    window.isClicked = false;
    return clicked;
  }
}




















