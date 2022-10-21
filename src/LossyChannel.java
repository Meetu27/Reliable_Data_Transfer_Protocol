

import java.net.*; 
import java.util.*;

public class LossyChannel {
    private InetAddress m_IPAddress;
    private DatagramSocket m_socket = null;
    private int m_localport = 0;
    private int m_remoteport = 0;
	public static int lossyRateController=3; // Use to add lossRate

    private byte[] m_receiveBuffer = new byte[Packet.MAX_PACKET_SIZE]; 
    private TransportLayer m_transportLayer = null;
    
    public LossyChannel(int localport, int remoteport) {
	m_localport = localport;
	m_remoteport = remoteport;
	try {
	    m_IPAddress = InetAddress.getByName("localhost"); 
	    m_socket = new DatagramSocket(localport); 
	} catch(Exception e) {
	    System.out.println("Cannot create UDP socket: "+e);
	}

	// start the reading thread
	new ReadThread().start();
    }

    public void setTransportLayer(TransportLayer tl) {
	m_transportLayer = tl;
    }

	public void send(byte[] payload) {
		Random rand = new Random();
		int randnum = rand.nextInt(10); // range 0-10
		if(randnum < lossyRateController)
			return; // simulate a loss
		try {
			DatagramPacket p =
					new DatagramPacket(payload, payload.length,
							m_IPAddress, m_remoteport);
			//System.out.println("LossyChannel::send: "+new String(p.getData()));
			m_socket.send(p);
		} catch(Exception e) {
			System.out.println("Error sending packet: "+e);
		}
	}

    // Interface provided to the transport layer to pick up message received
    public byte[] receive() {
	return m_receiveBuffer;
    }

    // Thread to read packets arrived from the network and to notify
    // the transport layer
    public class ReadThread extends Thread {
	public void run() {
	    while(true) {
		DatagramPacket p = 
		    new DatagramPacket(m_receiveBuffer, 
				       m_receiveBuffer.length); 
		
		try {
		    m_socket.receive(p); 
		} catch(Exception e) {
		    System.out.println("Cannot receive from socket: "+e);
		}
		
		byte[] receivedData = p.getData();
	//	System.out.println("Received packet: "+ receivedData[0]);
		
		if(m_transportLayer != null)
		    m_transportLayer.onPacketArrival();
	    }
	}
    }
}