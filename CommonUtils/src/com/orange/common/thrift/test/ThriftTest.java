package com.orange.common.thrift.test;

/**
 * Created by pipi on 14/11/5.
 */

import java.io.IOException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class ThriftTest {


    private void start() {
        try {
            BulletinBoard.Processor processor;
            processor = new BulletinBoard.Processor(new BulletinBoardImpl());
            TServerTransport serverTransport = new TServerSocket(7911);
            TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            System.out.println("Starting server on port 7911 ...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ThriftTest srv = new ThriftTest();
        srv.start();
    }

}
