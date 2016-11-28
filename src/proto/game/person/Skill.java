

package proto.game.person;



//  TODO:  Merge this with the Trait class, FFS.  And use ability-chains with
//  pre-reqs instead.


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
