package bgu.spl.mics.application.InputFiles;

import bgu.spl.mics.application.InputFiles.InputModel;

public class InputStudent {

    //----------------------fields--------------------
    private String name;
    private String depatrtment;
    private String status;
    private InputModel[] models;

    //----------------------Constructor------------------
    public InputStudent() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepatrtment() {
        return depatrtment;
    }

    public void setDepatrtment(String depatrtment) {
        this.depatrtment = depatrtment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InputModel[] getModels() {
        return models;
    }

    public void setModels(InputModel[] models) {
        this.models = models;
    }
}
