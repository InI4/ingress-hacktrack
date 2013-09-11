package de.spieleck.ingress.hackstat;

import java.util.Arrays;

public class HackResult
{
  String _id;
	int[] resos;
	Hacker hacker;
	Hack hack;
  int levelSum = -1;
	int sourceLine;
  double timestamp;
  // String source; // unused ..

  private void checkLevelSum()
  {
        if ( levelSum == -1 && resos != null ) { 
            levelSum = 0;
            for(int i = 0; i < 8; i++) levelSum += resos[i];
        }
  }

	public int getPlayerLevel()
	{
      return hacker.level;
	}

	public int getLevel()
	{
      checkLevelSum();
      return Math.min(hacker.level, Math.max(1, levelSum / 8));
	}

	public int getPortalLevel()
	{
		checkLevelSum();
        return Math.max(1, levelSum / 8);
	}

	public int getOverLevel()
	{
        checkLevelSum();
        return levelSum % 8;
	}

	public int getItemCount()
	{
        return hack.getItemCount();
	}

	public String toString()
	{
      int l = getPortalLevel();
      StringBuilder sb = new StringBuilder(8);
      int[] sresos = new int[resos.length];
      System.arraycopy(resos, 0, sresos, 0, resos.length);
      Arrays.sort(sresos);
      for ( int i = 7; i >= 0; i--) sb.append(sresos[i]);
      int hackLevel = Math.min(l, hacker.level);
      String xtra = "";
      for(HackItem item : hack.items) if ( item.level > 0 && item.level < hackLevel - 1 ) xtra = " XXX";
      return "Result[L"+hackLevel+"("+l+hacker.level+") "+sb+" "+hack+",sl="+sourceLine+","+hacker.name+xtra+"]";
	}

  public boolean hasCanGetUltra() { return hack.can_get_ultra; }
}

