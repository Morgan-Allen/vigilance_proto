

package proto.game.person;



public class Skill extends Trait {
  
  final public Skill roots[];
  
  
  public Skill(String name, String ID, String description, Skill... roots) {
    super(name, ID, description);
    this.roots = roots;
  }
}
