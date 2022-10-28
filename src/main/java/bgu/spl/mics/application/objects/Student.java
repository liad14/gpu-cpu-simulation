package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
import bgu.spl.mics.Messages.PublishEvent;
import bgu.spl.mics.Messages.TestEvent;
import bgu.spl.mics.Messages.TrainEvent;
import bgu.spl.mics.application.services.StudentService;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    List<Model> modelsToTrain;
    Queue<ConfrenceInformation> upcomingConferences;
    Future<Model> currFuture;
    Model currentlyWaitingOn;
    private int publications;
    private int papersRead;
    private AtomicInteger lastTick;
    private List<Model> testedModels;

    public Student(String _name, String _department, Degree _status) {//TODO department doesn't get in from the input
        name = _name;
        department = _department;
        status = _status;
        lastTick = new AtomicInteger(0);
        modelsToTrain = new LinkedList<>();
        testedModels = new LinkedList<>();
//        upcomingConferences=new PriorityBlockingQueue(1,confoCompare);
    }

    public Degree getDegree() {
        return status;
    }
    public String getDegreeAsString() {
        if (status == Degree.PhD)
            return "PhD";
        else
            return "MSc";
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void addModel(Model m) {
        modelsToTrain.add(m);
    }

    public String getName() {
        return name;
    }
    public Model getNextModel() {
        if (!modelsToTrain.isEmpty()) {
            currentlyWaitingOn = modelsToTrain.remove(0);
            return currentlyWaitingOn;
        } else
            return null;
    }
    public void addPublications(List<Model> modelList) {
        for (Model m : modelList) {
            if (m.getStudent() == this)
                publications++;
            else
                papersRead++;
        }
    }
    public void addTestedModel(Model m) {
        testedModels.add(m);
    }
    public List<Model> getTestedModels() {
        return testedModels;
    }
}