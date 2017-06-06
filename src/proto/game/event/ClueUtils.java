

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public class ClueUtils {
  
  
  protected static Series <Clue> addTraitClues(
    Plot plot, Element involved, Base follows, Batch <Clue> possible
  ) {
    //
    //  Wiretaps and mentions can't reliably reveal any descriptive features
    //  of the suspects involved, except as tipoffs.
    Role role = plot.roleFor(involved);
    if (role == null) return possible;
    
    if (involved.isPerson()) {
      Person p = (Person) involved;
      for (Trait t : Common.PERSON_TRAITS) {
        if (p.stats.levelFor(t) <= 0) continue;
        Clue forTrait = Clue.traitClue(plot, role, t);
        possible.add(forTrait);
      }
    }
    
    if (involved.isPlace()) {
      Place p = (Place) involved;
      for (Trait t : Common.VENUE_TRAITS) {
        if (! p.hasProperty(t)) continue;
        Clue forTrait = Clue.traitClue(plot, role, t);
        possible.add(forTrait);
      }
    }
    
    if (involved.isItem()) {
      Item p = (Item) involved;
      Clue match = Clue.confirmSuspect(plot, role, p, p.place());
      possible.add(match);
    }
    
    return possible;
  }
  
  
  protected static Batch <Clue> addLocationClues(
    Plot plot, Element involved, Base follows, Batch <Clue> possible
  ) {
    Role role = plot.roleFor(involved);
    if (role == null || ! involved.isPlace()) return possible;
    
    World world = plot.base.world();
    Region at = involved.region();
    Series <Region> around = world.regionsInRange(at, 1);
    
    for (Region near : around) {
      int range = (int) world.distanceBetween(at, near);
      Clue clue = Clue.locationClue(plot, role, near, range);
      clue.setGetChance(1f / ((1 + range) * (1 + range)));
      possible.add(clue);
    }
    
    return possible;
  }
  
  
  public static Series <Clue> possibleClues(
    Plot plot, Element involved, Element focus,
    Base follows, LeadType leadType
  ) {
    Batch <Clue> possible = new Batch();
    Batch <Clue> screened = new Batch();
    
    addTraitClues   (plot, involved, follows, possible);
    addLocationClues(plot, involved, follows, possible);
    
    CaseFile file = follows.leads.caseFor(plot);
    for (Clue clue : possible) {
      if (! leadType.canProvide(clue, involved, focus)) continue;
      if (file.isRedundant(clue)) continue;
      screened.add(clue);
    }
    
    return screened;
  }
  
  
  public static Clue pickFrom(Series <Clue> possible) {
    Clue band[] = possible.toArray(Clue.class);
    float weights[] = new float[band.length];
    for (int i = band.length; i-- > 0;) weights[i] = band[i].getChance();
    return (Clue) Rand.pickFrom(band, weights);
  }
}



