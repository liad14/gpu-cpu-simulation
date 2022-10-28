package bgu.spl.mics.application.objects;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private List<Model> readyToPublish= Collections.synchronizedList(new LinkedList<Model>());
    public ConfrenceInformation(String _name,int _date){
        name=_name;
        date=_date;
    }

    public int getDate(){return date;    }
    public String getName(){return name;}
    public void addModel(Model _model){
        readyToPublish.add(_model);
    }

    public List<Model> getPublications(){
        return readyToPublish;
    }
}
