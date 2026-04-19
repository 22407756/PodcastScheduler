package model;

public class BonusEpisode extends Episode {
    public BonusEpisode(String id, String title, int durationMinutes){
        super(id,title,durationMinutes);
    }
    public String getTypeLabel(){ return "Bonus"; }
}
