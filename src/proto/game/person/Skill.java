

package proto.game.person;



public class Skill extends Trait {
  
  final public Skill roots[];
  
  
  public Skill(
    String name, String ID, String imgPath, String description,
    Skill... roots
  ) {
    super(name, ID, imgPath, description);
    this.roots = roots;
  }
  
  
  public Skill[] roots() {
    return roots;
  }
}
