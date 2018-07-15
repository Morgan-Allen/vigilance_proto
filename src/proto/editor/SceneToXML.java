
package proto.editor;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import java.io.*;



public class SceneToXML {
  
  
  static void exportToXML(
    Scene scene, Editor editor,
    String name, String ID,
    String basePath, String outFile
  ) {
    
    final char DIR_CHARS[] = { 'n', 'e', 's', 'w' };
    
    StringBuffer xml = new StringBuffer();
    xml.append(
      "\n<scene "+
      "\n  name   = "+quote(name)+
      "\n  ID     = "+quote(ID  )+
      "\n  wide   = "+quote(scene.wide())+
      "\n  high   = "+quote(scene.high())+
      "\n>"
    );
    
    xml.append(
      "\n  <grid"
    );
    
    int typeID = 1;
    
    for (PropType type : editor.propTypes) {
      String refString = type.entryKey();
      refString = refString.replace(basePath, "");
      xml.append("\n    "+typeID+" = "+quote(refString));
      typeID += 1;
    }
    xml.append("\n  >");
    
    for (int y = 0; y < scene.high(); y++) {
      xml.append("\n    ");
      
      for (int x = 0; x < scene.wide(); x++) {
        
        Tile t = scene.tileAt(x, y);
        boolean empty = true, first = true;
        
        for (Element e : t.inside()) if (e instanceof Prop) {
          Prop p = (Prop) e;
          int propID = editor.propTypes.indexOf(p.kind());
          int facing = p.facing();
          
          if (p.origin() == t && propID != -1) {
            xml.append(first ? " " : " +");
            xml.append(propID + 1);
            if (facing != TileConstants.N) xml.append(DIR_CHARS[facing / 2]);
            empty = false;
            first = false;
          }
        }
        if (empty) {
          xml.append(" .");
        }
      }
    }
    
    xml.append("\n  </grid>");
    xml.append("\n</scene>");
    
    try {
      String outPath = basePath+""+outFile;
      BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
      out.write(xml.toString());
      out.close();
    }
    catch(Exception e) {
      I.report(e);
    }
  }
  
  private static String quote(Object o) {
    return "\""+o+"\"";
  }
  
}






