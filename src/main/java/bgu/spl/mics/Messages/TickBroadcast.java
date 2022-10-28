package bgu.spl.mics.Messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    int curTick;
    public TickBroadcast(int _curTick){
        curTick=_curTick;
    }

    public int getCurTick() {
        return curTick;
    }
}
