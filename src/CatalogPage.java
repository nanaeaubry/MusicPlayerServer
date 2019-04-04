import java.util.*;


public class CatalogPage
{
     List<CatalogItem> items;
     public CatalogPage() 
     {
         items = new ArrayList<CatalogItem>();
     }

    // getters
     public CatalogItem getItem(int i)
     {
        return this.items.get(i);
     }

     public int size()
     {
        return items.size();
     }

    // setters
     public void addItem(CatalogItem item)
     {
        this.items.add(item);
     }
};