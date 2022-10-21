

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ParReceiver extends TransportLayer{
	public static final int RECEIVER_PORT = 9888;
	public static final int SENDER_PORT = 9887;
	private static final String DUPLICATE_PACKET_MESSAGE = "Duplicate packet has been received";
	public ParReceiver(LossyChannel lc) {
		super(lc);
	}

	public void run() {
		byte nextPacketExpected = 0;
		Packet packetReceived;
		Packet packetToSend = new Packet();

		System.out.println("Ready to receive: ");

		while (true) {
			int event = waitForEvent();
			if (EVENT_PACKET_ARRIVAL == event) {
				packetReceived = this.receiveFromLossyChannel();
				if (packetReceived.isValid()) {   // Validate Received Package
					if (packetReceived.seq == nextPacketExpected) { // No loss or duplicate found then use this block
						this.deliverMessage(packetReceived);
						nextPacketExpected = increment(nextPacketExpected);
					}
					else {   // If package sequence is not match then duplicate package
						System.out.println(DUPLICATE_PACKET_MESSAGE);
						writeFile(DUPLICATE_PACKET_MESSAGE);
					}
				}
				packetToSend.ack = packetReceived.seq;
				sendToLossyChannel(packetToSend);
			}
		}
	}

	void deliverMessage(Packet packet) {
		byte[] payload = new byte[packet.length];
		for(int i=0; i<payload.length; i++)
			payload[i] = packet.payload[i];
		String received = new String(payload);
		System.out.println("Received "+packet.length+" bytes: "
				+ received);
		writeFile("Received "+packet.length+" bytes: "
				+ received);
	}

	private void writeFile(String receivedPackage){
		String filePath = System.getProperty("user.dir")+"/src/output.txt";
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true))){
			writer.write(receivedPackage+'\n');
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}  // Don't need to close the file as try with resources is auto closable.
	}

	public static void main(String args[]) throws Exception {
		LossyChannel lc = new LossyChannel(RECEIVER_PORT, SENDER_PORT);
		ParReceiver receiver = new ParReceiver(lc);
		lc.setTransportLayer(receiver);
		receiver.run();
	}
}  