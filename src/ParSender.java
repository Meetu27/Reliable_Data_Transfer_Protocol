import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParSender extends TransportLayer{
	public static final int RECEIVER_PORT = 9888;
	public static final int SENDER_PORT = 9887;

	public ParSender(LossyChannel lc) {
		super(lc);
	}
	public void run() {
		String filePath = System.getProperty("user.dir")+"/src/input.txt"; // Add absolute Path of your file
		List<String> fileInputReader = readFromFile(filePath); // Read data from above file and add it into List of String
		sendDataToReceiver(fileInputReader); // Use this method to send data to receiver side and validate acknowledge
	}

	private void sendDataToReceiver(List<String> fileInputReader){
		byte nextPacketExpected = 0;
		Packet packetReceived = new Packet();
			byte[] payload = null;
		if(fileInputReader.isEmpty()){     // If array is empty then read from keyboard
			payload = this.getMessageToSend();
		}else{
			payload	= this.getMessageFromFile(fileInputReader);
		}

		if (null == payload) {
			return;
		}
		System.out.println("Ready to receive: ");
		while(true) {
			packetReceived.payload = payload;
			packetReceived.length = payload.length;
			packetReceived.seq = nextPacketExpected;
			sendToLossyChannel(packetReceived);
			m_wakeup = false;  // Stop Wakeup
			startTimer();   // Start Timer
			int event = waitForEvent();
			if(EVENT_PACKET_ARRIVAL == event) {  // if even is packet arrival
				packetReceived = receiveFromLossyChannel();
				if(!packetReceived.isValid()){ // If packet is not validated then start this process further
					continue;
				}
				if(packetReceived.ack == nextPacketExpected){
					stopTimer();          // Stop Timer once get expected package response
					if(!fileInputReader.isEmpty()){
						payload = this.getMessageFromFile(fileInputReader); // Add payload from fileInput
					}else{
						payload = this.getMessageToSend(); // Add payload from keyboard
					}
					if (payload == null) {
						return;
					}
					nextPacketExpected = increment(nextPacketExpected);
				}
				else {
					System.out.println("Duplicate ack has been received");
				}
			}
		}
	}

  private List<String> readFromFile(String filePath){
		List<String> fileInputReader = new ArrayList<>();
		try(BufferedReader inFromFile = new BufferedReader(new FileReader(filePath))){
			String readLine = inFromFile.readLine();
		//	System.out.println("Add an integer from 1 to 10 for LossyRate such that n packets will be lost for every 10 packets sent:- ");
			LossyChannel.lossyRateController = Integer.valueOf(readLine); // First line should be integer
			readLine = inFromFile.readLine();
			while (readLine!=null){
				fileInputReader.add(readLine);
				readLine = inFromFile.readLine();
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found:- "+e);
		} catch (IOException e) {
			System.err.println("Error while reading file:- "+e);
		} // Don't need to close the file as try with resources is auto closable.
		return fileInputReader;
	}

	byte[] getMessageFromFile(List<String> inputFileReader){
		if(!inputFileReader.isEmpty()){
			String sentence =	inputFileReader.remove(0);
			System.out.println("Sending: "+sentence);
			return  sentence.getBytes();
		}else{
			System.out.println("File data has been sent successfully");
			System.out.println("User Input process has been started ");
			return null;
		}
	}
/**
 *  Use this function to get data from user's keyboard and return byte array of the added line.
 * */
	byte[] getMessageToSend() {
		System.out.println("Please enter a message to send: ");
		try {
			BufferedReader inFromUser =
					new BufferedReader(new InputStreamReader(System.in));
			String sentence = inFromUser.readLine();
			if(null == sentence)
				System.exit(1);
			System.out.println("Sending: "+sentence);
			return sentence.getBytes();
		} catch(Exception e) {
			System.out.println("IO error: "+e);
			return null;
		}
	}

	public static void main(String args[]) throws Exception {
		LossyChannel lc = new LossyChannel(SENDER_PORT, RECEIVER_PORT);
		ParSender sender = new ParSender(lc);
		lc.setTransportLayer(sender);
		sender.run();
	}
}