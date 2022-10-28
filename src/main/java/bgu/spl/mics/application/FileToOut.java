package bgu.spl.mics.application;

import java.io.*;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Cluster;

import bgu.spl.mics.application.FileToOut;
import com.google.gson.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class FileToOut {

    private static FileToOut instance = new FileToOut();
    private ConcurrentLinkedQueue<Student> students;
    private ConcurrentLinkedQueue<ConfrenceInformation> conferenceInformationList;

    private FileToOut(){
        students = new ConcurrentLinkedQueue<Student>();
        conferenceInformationList = new ConcurrentLinkedQueue<ConfrenceInformation>();
    }
    public static FileToOut getInstance(){
        return instance;
    }

    public void modelsToOut(Student myStudent){// ,Model[] modelsToReturn){
        myStudent.getTestedModels();
        students.add(myStudent);
    }

    public void conferenceToOut(ConfrenceInformation myConfrence) {
        conferenceInformationList.add(myConfrence);
    }


        public void makeOutputFile(int CPUTimeUsed, int GPUTimeUsed){
        Gson newGsonFile  = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter newWriter = new FileWriter("output.json");
            JsonObject newJsonObject = new JsonObject();
            JsonArray newStudentJsonArray = new JsonArray();

            for(Student studentToReturn : students) {
                JsonObject studentToJson = new JsonObject();
                studentToJson.addProperty("name",studentToReturn.getName());
                studentToJson.addProperty("department", studentToReturn.getDepartment());
                studentToJson.addProperty("status",studentToReturn.getDegreeAsString());
                studentToJson.addProperty("publications", studentToReturn.getPublications());
                studentToJson.addProperty("papersRead", studentToReturn.getPapersRead());
                JsonArray StudentModelsAsJsonArray = new JsonArray();
                for(Model modelToReturn: studentToReturn.getTestedModels()) {
                    JsonObject modelToJson = new JsonObject();
                    modelToJson.addProperty("name", modelToReturn.getName());
                    JsonObject dataToReturn = new JsonObject();
                    JsonPrimitive dataSize = new JsonPrimitive(modelToReturn.getData().getSize());
                    JsonPrimitive dataType = new JsonPrimitive(String.valueOf(modelToReturn.getData().getType()));

                    dataToReturn.add("type", dataType);
                    dataToReturn.add("size", dataSize);

                    modelToJson.add("data", dataToReturn);
                    modelToJson.addProperty("result", modelToReturn.getResultAsString());
                    modelToJson.addProperty("status", modelToReturn.getStatusAsString());
                    StudentModelsAsJsonArray.add(modelToJson);

                }
                studentToJson.add("trainedModels",StudentModelsAsJsonArray);
                newStudentJsonArray.add(studentToJson);
            }

            JsonArray newConferenceJsonArray = new JsonArray();
            for (ConfrenceInformation conferenceToReturn : conferenceInformationList) {
                System.out.println(conferenceToReturn.getName() + " Date: " + conferenceToReturn.getDate());
                JsonObject conferenceToJson = new JsonObject();
                conferenceToJson.addProperty("name",conferenceToReturn.getName());
                conferenceToJson.addProperty("date",conferenceToReturn.getDate());
                JsonArray publicationsToJsonArray = new JsonArray();
                for(Model modelToReturn : conferenceToReturn.getPublications()) {
                    JsonObject modelToJsonFromConference = new JsonObject();
                    modelToJsonFromConference.addProperty("name", modelToReturn.getName());
                    JsonObject dataToJsonFromConference = new JsonObject();

                    JsonPrimitive size = new JsonPrimitive(modelToReturn.getData().getSize());
                    JsonPrimitive type = new JsonPrimitive(String.valueOf(modelToReturn.getData().getType()));
                    modelToJsonFromConference.add("type", type);
                    modelToJsonFromConference.add("size", size);

                    modelToJsonFromConference.add("data", dataToJsonFromConference);
                    modelToJsonFromConference.addProperty("result", modelToReturn.getResultAsString());
                    modelToJsonFromConference.addProperty("status", modelToReturn.getStatusAsString());

                    publicationsToJsonArray.add(modelToJsonFromConference);
                }
                conferenceToJson.add("publications",publicationsToJsonArray);
                newConferenceJsonArray.add(conferenceToJson);
            }
            JsonPrimitive processedBatches = new JsonPrimitive(Cluster.getInstance().getProcessedBatches());
            JsonPrimitive CPUTimeUsedAsJson = new JsonPrimitive(CPUTimeUsed);
            JsonPrimitive GPUTimeUsedAsJson = new JsonPrimitive(GPUTimeUsed);

            newJsonObject.add("students", newStudentJsonArray);
            newJsonObject.add("conferences", newConferenceJsonArray);
            newJsonObject.add("gpuTimeUsed", GPUTimeUsedAsJson);
            newJsonObject.add("cpuTimeUsed", CPUTimeUsedAsJson);
            newJsonObject.add("batchesProcessed", processedBatches);
            newGsonFile.toJson(newJsonObject,newWriter);
            //            gson.toJson(studentArray);
            //            gson.toJson(confrenceArray);

            newWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}


