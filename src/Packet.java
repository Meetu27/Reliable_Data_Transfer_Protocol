
public class Packet {
	public static final byte[] PREAMBLE =
			{'E','E','C','4','8','4','F','A'};
	public static final int MAX_PACKET_PAYLOAD = 1024;
	public static final int MAX_PACKET_SIZE = MAX_PACKET_PAYLOAD + 6 +
			PREAMBLE.length; // payload length + header fields + preamble len

	public byte seq; // sequence number for duplicate detection, actually needed 1 bit
	public byte ack; // acked sequence number, actually needed 1 bit
	public int length; // payload length, really need 4 bytes
	public byte[] payload = new byte[MAX_PACKET_PAYLOAD];

	private boolean m_isValid = true;

	public Packet() {
		seq = -1;
		ack =-1;
		length = 0;
	}

	public Packet(byte[] receivedData) {
		// Does the packet carries the right preamble?
		m_isValid = verifyPacket(receivedData);
		int index = PREAMBLE.length;
		if(m_isValid) {
			//System.out.println("Valid packet");

			// set seq number
			seq = receivedData[index++];

			// set ack sequence number
			ack = receivedData[index++];

			//System.out.println("b1: "+receivedData[index]);
			//System.out.println("b2: "+receivedData[index+1]);
			//System.out.println("b3: "+receivedData[index+2]);
			//System.out.println("b4: "+receivedData[index+3]);

			// payload length
			byte[] intArray = {receivedData[index], receivedData[index+1],
					receivedData[index+2], receivedData[index+3]};
			index += 4;
			length = ByteArrayUtils.readInt(intArray);

			// the rest is the application payload
			//System.out.println("payload len: "+length);

			for(int i=0; i<length; i++) {
				payload[i] = receivedData[index+i];
			}
			//String recvd = new String(payload);
			//System.out.println("Packet::Received: "+recvd);
			//System.out.println("Got ack, try get another message to send");
		}
	}

	public byte[] toBytes() {
		// we want to construct a byte array consisting
		// a preamble, a sequence number, an ack,
		// a length field (4 bytes), and the payload
		int totalLen = length + 2 + 4 + PREAMBLE.length;
		byte[] data = new byte[totalLen];
		//System.out.println("Send total length: "+totalLen);

		int i = 0;
		for(i=0; i<PREAMBLE.length; i++)
			data[i] = PREAMBLE[i];
		// note that our sequence number is 1-bit, so it fits one byte
		data[i++] = seq;
		data[i++] = ack;

		byte[] intArray = new byte[4];
		ByteArrayUtils.writeInt(intArray, length);
		int k = 0;
		data[i++] = intArray[k++];
		data[i++] = intArray[k++];
		data[i++] = intArray[k++];
		data[i++] = intArray[k++];

		//System.out.println("Packet::toBytes: payload len="+length);

		for(int j=0; j<length; j++,i++)
			data[i] = payload[j];

		// self test on marshalling
		//System.out.println("----");
		//Packet test = new Packet(data);
		//System.out.println("----");

		return data;
	}

	private boolean verifyPacket(byte[] data) {
		boolean verified = true;
		for(int i=0; i<PREAMBLE.length; i++)
			if(data[i] != PREAMBLE[i]) {
				verified = false;
				break;
			}
		return verified;
	}

	public boolean isValid() {
		return m_isValid;
	}
}