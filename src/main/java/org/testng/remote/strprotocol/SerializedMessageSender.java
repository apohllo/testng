package org.testng.remote.strprotocol;

import org.testng.remote.RemoteTestNG;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class SerializedMessageSender extends BaseMessageSender {

  public SerializedMessageSender(String host, int port) {
    super(host, port);
  }

  @Override
  public void sendMessage(IMessage message) throws IOException {
    synchronized(m_ackLock) {
      p("Sending message " + message);
      ObjectOutputStream oos = new ObjectOutputStream(m_outStream);
      oos.writeObject(message);
      oos.flush();

      try {
        p("Message sent, waiting for ACK...");
        m_ackLock.wait();
        p("... lock done");
      }
      catch(InterruptedException e) {
      }
    }
  }


  @Override
  public IMessage receiveMessage() throws IOException, ClassNotFoundException {

    IMessage result = null;
    try {
      ObjectInputStream ios = new ObjectInputStream(m_inStream);
//      synchronized(m_input) {
        result = (IMessage) ios.readObject();
        p("Received message " + result);
//        sendAck();
//      }
    }
    catch(Exception ex) {
      if (RemoteTestNG.isVerbose()) {
        ex.printStackTrace();
      }
    }
    return result;
  }

  private static void p(String s) {
    if (RemoteTestNG.isVerbose()) {
      System.out.println("[SerializedMessageSender] " + s);
    }
  }
}
