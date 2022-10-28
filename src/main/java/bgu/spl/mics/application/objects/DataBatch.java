package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int index;
    private Model myModel;
    public DataBatch(Data _data,int _index, Model _myModel){
        data=_data;
        index=_index;
        myModel=_myModel;
    }
    public Model getModel(){
        return myModel;
    }
    public  Data getData(){
        return data;
    }
    
}
