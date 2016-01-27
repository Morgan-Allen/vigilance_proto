/**  
  *  Written by Morgan Allen.
  *  I intend to slap on some kind of open-source license here in a while, but
  *  for now, feel free to poke around for non-commercial purposes.
  */
package proto.common;
import java.io.*;
import java.util.zip.*;

import proto.util.*;

import java.net.*;
import java.security.CodeSource;



public class Assets {
  
  
  final static boolean EXCLUDE_BASE_DIR = false;
  //  TODO:  TEMPORARY HACK BELOW, REMOVE!
  final static String DEFAULT_JAR_NAME = "Vigilance_JAR.jar";
  final public static char REP_SEP = '/';
  private static boolean
    callsVerbose = false,
    extraVerbose = false;
  
  
  public static abstract class Loadable {
    
    protected static enum State {
      INIT, LOADED, DISPOSED, ERROR
    }
    
    final String assetID;
    final Class sourceClass;
    
    //  TODO:  Get rid of this and refer directly to the parent Playable object
    //  instead- that will give you a safer way to filter out the assets that
    //  actually need disposal.
    final boolean disposeWithSession;
    
    protected State state = State.INIT;
    private Table <String, String> fileStamps = new Table();
    private long refreshTime = -1;
    
    
    protected Loadable(
      String modelName, Class sourceClass, boolean disposeWithSession
    ) {
      this.assetID            = modelName+"_"+sourceClass.getSimpleName();
      this.sourceClass        = sourceClass;
      this.disposeWithSession = disposeWithSession;
      Assets.registerForLoading(this);
    }
    
    public String assetID() { return assetID; }
    public Class sourceClass() { return sourceClass; }

    protected abstract State loadAsset();
    protected abstract State disposeAsset();
    
    public boolean stateLoaded  () { return state == State.LOADED  ; }
    public boolean stateDisposed() { return state == State.DISPOSED; }
    
    protected void setKeyFile(String filePath) {
      final String stamp = fileStampFor(filePath);
      if (stamp != null) fileStamps.put(filePath, stamp);
    }
    
    protected boolean assetExists(String filePath) {
      return exists(filePath);
    }
  }
  
  
  final static List <String>
    classesToLoad = new List <String> ();
  final static List <Class>
    classesLoaded = new List <Class> ();
  
  final static List <Loadable>
    assetsToLoad = new List <Loadable> (),
    assetsLoaded = new List <Loadable> ();
  
  protected static Table <String, Object>
    resCache = new Table <String, Object> (1000);
  
  static Table <String, Loadable>
    assetCache = new Table <String, Loadable> (1000);
  
  
  
  /**  Methods called externally by the PlayLoop class.
    */
  public static void compileAssetList(
    String sourcePackage, Class... loadFirst
  ) {
    if (callsVerbose) {
      I.say("\nCompiling list of classes to load from "+sourcePackage);
      for (Class c : loadFirst) I.say("  Also: "+c.getName());
    }
    
    try { for (Class l : loadFirst) Class.forName(l.getName()); }
    catch (Exception e) { I.report(e); }
    
    final Batch <String> names = new Batch <String> ();
    if (I.AM_INSIDE_JAR) compileJarredClassNames(sourcePackage, names);
    else compileClassNames(sourcePackage, names);
    
    final String prefix = sourcePackage+".";
    for (String name : names) {
      if (EXCLUDE_BASE_DIR) name = name.substring(prefix.length());
      if (extraVerbose) I.say("  Class to load is: "+name);
      classesToLoad.add(name);
    }
  }
  
  
  public static void advanceAssetLoading(int timeLimit) {
    if (classesToLoad.size() <= 0 && assetsToLoad.size() <= 0) {
      if (extraVerbose) I.say("No assets left to load!");
      return;
    }
    if (extraVerbose) I.say("Advancing asset loading...");
    
    //  We limit the time spent in this loop so as to ensure a smooth external
    //  frame-rate:
    final long initTime = System.currentTimeMillis();
    while (true) {
      final long time = System.currentTimeMillis(), timeSpent = time - initTime;
      if (extraVerbose) {
        I.say("  Advancing load loop, time: "+timeSpent+"/"+timeLimit);
        I.say("  Current system time: "+time+", init: "+initTime);
      }
      
      if (timeLimit > 0 && timeSpent >= timeLimit) {
        if (extraVerbose) I.say("  ...Load loop ran out of time!");
        return;
      }
      
      //  While there are still classes to load, load those-
      if (classesToLoad.size() > 0) {
        final String className = classesToLoad.removeFirst();
        try {
          final Class match = Class.forName(className);
          classesLoaded.add(match);
          if (extraVerbose) I.say("  Class loaded okay: "+match);
        }
        catch (ClassNotFoundException e) {
          I.say("CLASS NOT FOUND: "+className);
        }
      }
      
      //  Otherwise, move on to loading any registered assets-
      else if (assetsToLoad.size() > 0 && RunGame.onMainThread()) {
        final Loadable asset = assetsToLoad.first();
        if (extraVerbose) I.say("  Begun loading of:  "+asset.assetID+"...");
        loadNow(asset);
      }
      
      else {
        if (extraVerbose) I.say("  ...Load loop done for now.");
        return;
      }
    }
  }
  
  
  public static float loadProgress() {
    final int classTotal = classesToLoad.size() + classesLoaded.size();
    final float classProgress = (classTotal == 0) ?
      1 : classesLoaded.size() * 1f / classTotal;
    
    final int assetTotal = assetsToLoad.size() + assetsLoaded.size();
    final float assetProgress = (assetTotal == 0) ?
      1 : assetsLoaded.size() * 1f / assetTotal;
    
    if (classProgress == 1 && assetTotal == 0) return 1;
    float progress = 0;
    progress += classProgress * 0.1f;
    progress += assetProgress * 0.9f;
    
    if (extraVerbose && progress < 1) {
      I.say("Class/assets load progress: "+classProgress+"/"+assetProgress);
      I.say("Total load progress: "+progress);
    }
    return progress;
  }
  
  
  public static void registerForLoading(Loadable asset) {
    if (asset == null) return;
    if (assetCache.get(asset.assetID) != null) {
      final Loadable older = assetCache.get(asset.assetID);
      I.say("\nWARNING FOR: "+asset.sourceClass);
      I.say(asset.assetID+" ALREADY DEFINED IN "+older.sourceClass);
    }
    
    if (extraVerbose) I.say("    Registering- "+asset.assetID);
    assetCache.put(asset.assetID, asset);
    assetsToLoad.add(asset);
  }
  
  
  public static void loadNow(Loadable asset) {
    if (asset == null) return;
    //
    //  This appears to be the simplest solution to ensuring that assets being
    //  loaded up on a separate thread don't wreck the place.  In essence, wait
    //  until the main thread begins or give it a chance to catch up.
    while ((! RunGame.onMainThread()) && (! asset.stateLoaded())) {
      if (! RunGame.mainThreadBegun()) return;
      try {
        if (extraVerbose) I.say("\n...Pausing to wait for assets-thread.");
        Thread.sleep(250);
      }
      catch (Exception e) {}
    }
    if (extraVerbose) I.say("  Begun loading of:  "+asset.assetID+"...");
    //
    //  We have some safety-checks to ensure assets don't get loaded twice, but
    //  to avoid the queue being blocked we remove them either way.
    if (! asset.stateLoaded()) {
      asset.loadAsset();
      assetsLoaded.add(asset);
    }
    if (asset.stateLoaded()) {
      assetsToLoad.remove(asset);
      assetCache.put(asset.assetID, asset);
      if (extraVerbose) I.say(" ...OK.");
    }
    else if (extraVerbose) I.say("  ...Still no joy.");
  }
  
  
  public static void disposeOf(Loadable asset) {
    if (asset == null) return;
    if (! asset.stateDisposed()) asset.disposeAsset();
    assetsToLoad.remove(asset);
    assetsLoaded.remove(asset);
    assetCache.remove(asset.assetID);
  }
  
  
  public static void disposeSessionAssets() {
    //
    //  Gets rid of assets associated with a single game save/load session-
    for (ListEntry <Loadable> e : assetsLoaded.entries()) {
      if (e.refers.disposeWithSession) {
        e.refers.disposeAsset();
        assetsLoaded.removeEntry(e);
        assetCache.remove(e.refers.assetID);
      }
    }
  }
  
  
  public static void disposeGameAssets() {
    //
    //  Gets rid of all game assets upon quit.
    for (Loadable asset : assetsLoaded) {
      asset.disposeAsset();
    }
    assetsToLoad.clear();
    assetsLoaded.clear();
    assetCache.clear();
  }
  
  
  public static List <String> classesToLoad() {
    return classesToLoad;
  }
  
  
  public static List <Class> classesLoaded() {
    return classesLoaded;
  }
  
  
  
  /**  Utility methods for referring to assets and their derivable state from
    *  with game-save files-
    */
  static Table <Integer, Loadable>
    IDassets = new Table <Integer, Loadable> (1000);
  static Table <Loadable, Integer>
    assetIDs = new Table <Loadable, Integer> (1000);
  static int counterID = 0;
  
  public static void clearReferenceIDs() {
    IDassets.clear();
    assetIDs.clear();
    counterID = 0;
  }
  
  
  public static void saveReference(
      Loadable model, DataOutputStream out
  ) throws Exception {
    if (model == null) { out.writeInt(-1); return; }
    final Integer modelID = assetIDs.get(model);
    if (modelID != null) { out.writeInt(modelID); return; }
    final int newID = counterID++;
    assetIDs.put(model, newID);
    out.writeInt(newID);
    Assets.writeString(out, model.assetID);
    Assets.writeString(out, model.sourceClass.getName());
  }
  
  
  public static Loadable loadReference(
      DataInputStream in
  ) throws Exception {
    final int modelID = in.readInt();
    if (modelID == -1) return null;
    Loadable loaded = IDassets.get(modelID);
    if (loaded != null) return loaded;
    final String modelName = Assets.readString(in);
    final String className = Assets.readString(in);
    Class.forName(className);
    loaded = assetCache.get(modelName);
    
    if (loaded == null) I.complain(
      "MODEL NAMED: "+modelName+
      "\nNO LONGER DEFINED IN SPECIFIED CLASS: "+className
    );
    IDassets.put(modelID, loaded);
    return loaded;
  }
  

  
  /**  Methods related to asset-caching and checking for refresh-
    */
  public static void cacheResource(Object res, String key) {
    resCache.put(key, res);
  }
  
  
  public static void clearCachedResource(String key) {
    resCache.remove(key);
  }
  
  
  public static Object getResource(String key) {
    return resCache.get(key);
  }
  
  
  public static boolean exists(String fileName) {
    if (fileName == null) return false;
    final File file = new File(fileName);
    return file.exists();
  }
  
  
  public static String fileStampFor(String fileName) {
    final File asset = new File(fileName);
    if (! asset.exists()) return null;
    final String stamp = ""+asset.lastModified();
    return stamp;
  }
  
  
  public static boolean checkForRefresh(Loadable asset, int delayMillis) {
    //
    //  First, we check to see if a refresh is required at all.  If not, just
    //  return a negative.  (-1 is used to flag no refresh scheduled.)
    //  We have to allow a small delay here for files to complete being written
    //  after their time-stamp has been modified.
    if (asset.refreshTime == -1) {
      boolean noChange = true;
      for (String fileName : asset.fileStamps.keySet()) {
        final String
          oldStamp = asset.fileStamps.get(fileName),
          newStamp = fileStampFor(fileName);
        if (! oldStamp.equals(newStamp)) noChange = false;
      }
      if (noChange) return false;
    }
    final long time = System.currentTimeMillis();
    if (asset.refreshTime == -1) asset.refreshTime = time + delayMillis;
    if (asset.refreshTime > time) return false;
    //
    //  Once that's done, we set the asset's time-stamps to their present
    //  values, and clear any cached file-versions we might have...
    final String keyFiles[] = asset.fileStamps.keySet().toArray(new String[0]);
    for (String fileName : keyFiles) {
      clearCachedResource(fileName);
      asset.fileStamps.put(fileName, fileStampFor(fileName));
    }
    //
    //  Then refresh the dependent asset itself, and return:
    disposeOf(asset);
    loadNow  (asset);
    asset.refreshTime = -1;
    return true;
  }
  
  
  
  /**  Recursively compiles the (presumed) full-path names of all classes in
    *  the given file directory.
    */
  private static void compileClassNames(String packg, Batch <String> loaded) {
    final File basedir = new File("bin");
    packg = packg.replace('.', '/');
    addClasses(basedir.toURI(), new File(basedir, packg), loaded);
  }
  
  
  private static void addClasses(URI base, File dir, Batch <String> list) {
    
    if (callsVerbose) I.say("Attempting to list classes in "+dir);
    File[] files = dir.listFiles();
    if (files == null) return; // not a directory
    
    for (File f : files) {
      if (f.isDirectory()) {
        addClasses(base, f, list);
        continue;
      }
      if (! f.getName().endsWith(".class")) continue;
      if (f.getName().contains("$")       ) continue;
      URI relative = base.relativize(f.toURI());
      String path = relative.toString().replaceAll("/|\\\\", ".");
      path = path.substring(0, path.length() - 6);
      if (extraVerbose) I.say("  Found class file: "+path);
      
      list.add(path);
    }
  }
  
  
  public static Batch <Class> loadPackage(String packageName) {
    if (classesToLoad.size() > 0) I.complain("CLASS LOADING NOT COMPLETE");
    
    final String key = "package"+packageName;
    final Batch <Class> cached = (Batch<Class>) getResource(key);
    if (cached != null) return cached;

    final Batch <Class> matches = new Batch <Class> ();
    for (Class checked : classesLoaded) {
      if (checked.getName().startsWith(packageName)) matches.add(checked);
    }
    
    cacheResource(matches, key);
    return matches;
  }
  
  
  
  /**  Pulls the same trick, but applied to class files embedded in a .jar
    *  file.
    */
  private static void compileJarredClassNames(
    String dirName, Batch <String> loaded
  ) {
    CodeSource code = Assets.class.getProtectionDomain().getCodeSource();
    if (code == null) {
      if (callsVerbose) I.say("COULD NOT FIND JARRED CLASSES!");
      return;
    }
    
    String jarPath = code.getLocation().getFile();
    if (jarPath.length() <= 2) jarPath = DEFAULT_JAR_NAME;
    //  NOTE:  CLASSES WILL NOT BE REGISTERED UNDER OSX FILEPATHS WITH SPACES
    //  UNLESS THIS SUBSTITUTION IS PERFORMED.  DO NOT DELETE.
    jarPath = jarPath.replace("%20", " ");
    
    File file = new File(jarPath);
    if (callsVerbose) {
      I.say("Code location: "+jarPath+", exists? "+file.exists());
    }
    
    if (! file.exists()) file = new File(file.getName());
    if (callsVerbose) {
      I.say("Relative location: "+file+", exists? "+file.exists());
    }
    
    if (! file.exists()) return;
    
    try {
      final URL jarURL = file.toURI().toURL();
      ZipInputStream zip = new ZipInputStream(jarURL.openStream());
      for (ZipEntry e; (e = zip.getNextEntry()) != null;) {
        if (e.isDirectory()) continue;
        
        final String name = e.getName();
        if (extraVerbose) I.say("Jarred file is: "+name);
        if (! name.endsWith(".class")) continue;
        if (name.indexOf('$') != -1) continue;
        
        final int cutoff = name.length() - ".class".length();
        final String className = name.substring(0, cutoff).replace('/', '.');
        if (dirName != null && ! className.startsWith(dirName)) continue;
        loaded.add(className);
      }
    }
    catch (IOException e) { I.report(e); }
  }
  
  
  
  /**  Seperates the given full file path into it's path and file-name
    *  components, returning them as a two-element array- (path first, file
    *  name second.)
    */
  public static String[] sepPath(String fullPath) {
    char[] fP = fullPath.toCharArray();
    int ind;
    for (ind = fP.length; ind-- > 0;)
      if (fP[ind] == REP_SEP) break;
    
    if (ind < fP.length) ind++;
    String PN[] = {
      new String(fP, 0, ind),
      new String(fP, ind, fP.length - ind)
    };
    return PN;
  }
  
  
  /**  Returns a safe version of the given path-string (using the default
    *  separator.)
    */
  public static String safePath(String path) {
    return safePath(path, java.io.File.separatorChar);
  }
  
  
  /**  Returns a safe version of the given path-string (i.e, using the given
    *  seperator.)
    */
  public static String safePath(String path, char separator) {
    int l = path.length();
    char chars[] = new char[l];
    path.getChars(0, l, chars, 0);
    for (int x = 0; x < l; x++) {
      switch(chars[x]) {
        case('/'):
        case('\\'):
          chars[x] = separator;
        break;
      }
    }
    return new String(chars);
  }
  
  
  public static void writeString(DataOutputStream dOut, String s)
      throws IOException {
    byte chars[] = s.getBytes();
    dOut.writeInt(chars.length);
    dOut.write(chars);
  }
  
  
  public static String readString(DataInputStream dIn) throws IOException {
    final int len = dIn.readInt();
    byte chars[] = new byte[len];
    dIn.read(chars);
    return new String(chars);
  }
}





