

package proto.game.world;
import proto.common.*;
import proto.util.*;



public class BaseFinance {
  
  
  final Base base;
  private int
    publicIncome,
    publicExpense,
    publicFunds,
    secretPercent,
    secretExpense,
    secretFunds;
  
  
  BaseFinance(Base base) {
    this.base = base;
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt(publicIncome );
    s.saveInt(publicExpense);
    s.saveInt(publicFunds  );
    s.saveInt(secretPercent);
    s.saveInt(secretExpense);
    s.saveInt(secretFunds  );
  }
  
  
  void loadState(Session s) throws Exception {
    publicIncome  = s.loadInt();
    publicExpense = s.loadInt();
    publicFunds   = s.loadInt();
    secretPercent = s.loadInt();
    secretExpense = s.loadInt();
    secretFunds   = s.loadInt();
  }
  
  
  
  public int publicIncome () { return publicIncome ; }
  public int publicExpense() { return publicExpense; }
  public int publicFunds  () { return publicFunds  ; }
  public int secretPercent() { return secretPercent; }
  public int secretExpense() { return secretExpense; }
  public int secretFunds  () { return secretFunds  ; }
  
  
  public void updateFinance() {
    
    publicIncome  = 0;
    publicExpense = 0;
    secretExpense = 0;
    
    for (Region dist : base.world.regions) {
      publicIncome  += dist.incomeFor  (base);
      publicExpense += dist.expensesFor(base);
    }
    
    if (base.world.timing.monthIsUp()) {
      int secretShare = secretIncome();
      if (secretShare > 0) {
        secretFunds += secretShare;
        publicFunds -= secretShare;
      }
      publicFunds += publicIncome ;
      publicFunds -= publicExpense;
      secretFunds -= secretExpense;
      
      if (publicFunds < 0) {
        publicFunds = 0;
        //  TODO:  Render public facilities defunct!
      }
      if (secretFunds < 0) {
        secretFunds = 0;
        //  TODO:  Render secret facilities defunct!
      }
    }
  }
  
  
  public void setSecretPercent(int percent) {
    secretPercent = percent;
  }
  
  
  public int secretIncome() {
    int publicMargin = publicIncome - publicExpense;
    return (publicMargin * secretPercent) / 100;
  }
  
  
  public boolean incPublicFunds(int funds) {
    publicFunds = Nums.max(0, publicFunds + funds);
    return true;
  }
  
  
  public boolean incSecretFunds(int funds) {
    secretFunds = Nums.max(0, secretFunds + funds);
    return true;
  }
  
}





