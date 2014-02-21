package de.spieleck.ingress.hackstat;

import java.util.List;

public class Hack
{
		String type;
		List<HackItem> items;
		List<HackItem> bonus;
    boolean can_get_ultra = false;
    int nkeys = -1;

		public int getItemCount()
		{
        int sum = 0;
        for(HackItem i : items) {
          sum += i.quantity;
        }
        return sum;
		}

		public int getDiffItemCount()
		{
			return items.size();
		}

    public List<HackItem> result(int i)
    {
        return i == 1 ? bonus : items;
    }

		public String toString()
		{
			return "Hack["+type+" "+items+" "+bonus+"]";
		}
}
