package bgu.spl.mics.Messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.List;

public class PublishConferenceBroadcast implements Broadcast {
    List<Model> publishModels;
    public PublishConferenceBroadcast(ConfrenceInformation publishIt){
        publishModels=publishIt.getPublications();
    }
    public List<Model> getInfo(){
        return publishModels;
    }
    
}
