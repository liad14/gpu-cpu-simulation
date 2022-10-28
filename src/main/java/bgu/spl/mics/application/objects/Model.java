package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.InputFiles.InputModel;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    public enum Result {GOOD, BAD}
    public enum Status{PreTrained,Training,Trained,Tested}
    Data data;
    String name;
    GPU myGPU;
    Status myStatus;
    Result myResult;
    Student myStudent;
    boolean wasPublished;

    public Model(String _name, Data.Type _type ,int _size, Student _myStudent){
        name=_name;
        data=new Data(_type,_size);
        myStatus=Status.PreTrained;
        myStudent=_myStudent;
        myResult=null;
        wasPublished=false;
    }

    public  Model(Student student, InputModel inputModel) {
        name = inputModel.getName();
        data = new Data(Data.Type.valueOf(inputModel.getType()), inputModel.getSize());
        myStudent = student;
        myStatus = Status.PreTrained;
        myResult = null;
        wasPublished = false;



    }
    public String getName(){
        return name;
    }
    public Data getData(){
        return data;
    }
    public void setGPU(GPU g){
        myGPU=g;
    }
    public void setStatus(Status newState){
        myStatus=newState;
    }
    public void setResult(Result newRes){
        myResult=newRes;
    }
    public Student getStudent(){
        return myStudent;
    }
    public Result getResult(){ return myResult; }
    public String getResultAsString(){
        if (myStatus == Status.PreTrained)
            return "PreTrained";
        else if (myStatus == Status.Training)
            return "Training";
        else if (myStatus == Status.Trained)
            return "Trained";
        else
            return "Tested";
    }
    public Status getStatus(){
        return myStatus;
    }
    public String getStatusAsString(){
        if (myResult == Result.GOOD)
            return "GOOD";
        else
            return "BAD";
    }
    public boolean positiveResult(){
        return myResult==Result.GOOD;
    }
    public String toString(){
        return name;
    }
    public boolean getWasPublished(){return wasPublished;}
    public void setWasPublished(){wasPublished=true;}
    }


