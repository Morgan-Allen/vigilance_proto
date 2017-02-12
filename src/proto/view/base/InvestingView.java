

package proto.view.base;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;



public class InvestingView extends UINode {
  
  
  RegionView regionView;
  MapInsetView mapView;
  
  
  
  public InvestingView(UINode parent, Box2D bounds) {
    super(parent, bounds);
    
    int fullWide = (int) bounds.xdim(), fullHigh = (int) bounds.ydim();
    
    mapView = new MapInsetView(this, new Box2D(
      320, 5, fullWide - 640, fullHigh - 10
    ));
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    
    regionView = new RegionView(this, mapView, new Box2D(
      0, 5, 320, fullHigh - 10
    ));
    
    addChildren(regionView, mapView);
  }
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    Person person = mainView.rosterView.selectedPerson();
    int across = 10, down = 10;
    /*
    int income = region.incomeFor(base), expense = region.expensesFor(base);
    g.drawString("Income: " +income , vx + 30 , vy + down);
    g.drawString("Expense: "+expense, vx + 130, vy + down);
    //*/
    
    //  TODO:  Turn investments into a kind of Task?
    /*
    TaskCraft current = null;
    for (Assignment a : person.assignments()) if (a instanceof TaskCraft) {
      current = (TaskCraft) a;
    }
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      "Current Crafting: "+(current == null ? "None" : current.made()), g,
      vx + across, vy + down, 320, 30
    );
    //*/
    
    renderFinanceReport(surface, g, mainView.world().playerBase());
    return true;
  }
  
  
  protected void renderFinanceReport(Surface surface, Graphics2D g, Base base) {
    int down = 10, across = vw - 320;
    StringBuffer s = new StringBuffer();
    
    Region region = mapView.selectedRegion();
    if (region != null) {
      float violence   = region.currentValue(Region.VIOLENCE  ) / 100f;
      float corruption = region.currentValue(Region.CORRUPTION) / 100f;
      int baseIncome   = region.kind().baseFunding();
      int regionIncome = region.incomeFor(base) - region.expensesFor(base);
      int violenceLoss = (int) (baseIncome * violence  );
      int corruptLoss  = (int) (baseIncome * corruption);
      
      s.append("\n\nFinances For "+region);
      s.append("\n  Basic Income: "+baseIncome);
      s.append("\n  Losses from violence: "+violenceLoss);
      s.append("\n  Losses from corruption: "+corruptLoss);
      s.append("\n  Total with facilities: "+regionIncome);
    }
    
    final BaseFinance BF = base.finance;
    int margin = BF.publicIncome() - BF.publicExpense();
    s.append("\n\nJanus Industries");
    s.append("\n  Total Income: " +BF.publicIncome ());
    s.append("\n  Total Expense: "+BF.publicExpense());
    s.append("\n  Public Funds: " +BF.publicFunds  ());
    s.append(" ("+I.signNum(margin)+" per month)");
    
    s.append("\n\nProject Vigil");
    s.append("\n  Total Income: " +BF.secretIncome ());
    s.append(" ("+BF.secretPercent()+"% of revenue)");
    s.append("\n  Total Expense: "+BF.secretExpense());
    s.append("\n  Secret Funds: " +BF.secretFunds  ());
    
    g.setColor(Color.WHITE);
    ViewUtils.drawWrappedString(
      s.toString(), g, vx + across, vy + down, 320, vh - 20
    );
  }
}


