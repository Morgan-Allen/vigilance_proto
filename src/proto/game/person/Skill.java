

package proto.game.person;



//  TODO:  Merge this with the Trait class, FFS.  And use ability-chains with
//  pre-reqs instead.


public class Skill extends Trait {
  
  final public Trait roots[];
  
  
  public Skill(
    String name, String ID, String imgPath, String description,
    Trait... roots
  ) {
    super(name, ID, imgPath, description);
    this.roots = roots;
  }
  
  
  public Trait[] roots() {
    return roots;
  }
}
