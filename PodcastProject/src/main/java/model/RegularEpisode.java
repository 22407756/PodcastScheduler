package model;

public class RegularEpisode extends Episode {
    public RegularEpisode(String id, String title, int durationMinutes) { super(id,title,durationMinutes);}
    public String getTypeLabel(){ return "Regular"; }

}
