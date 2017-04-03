

package proto.common;



public class RunAsTest {
  public static void main(String args[]) {
    String command = "java -cp bin proto.common/DebugSceneFile";
    try { Runtime.getRuntime().exec(command); }
    catch (Exception e) {}
  }
}
