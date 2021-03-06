package com.orange.common.thrift.test;

import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;

class BulletinBoardImpl implements BulletinBoard.Iface {
    private List<Message> msgs;

    public BulletinBoardImpl() {
        msgs = new ArrayList<Message>();
    }

    @Override
    public void add(Message msg) throws TException {
        System.out.println("date: " + msg.date);
        System.out.println("text: " + msg.text);
        msgs.add(msg);
    }

    @Override
    public List<Message> get() throws TException {
        return msgs;
    }
}