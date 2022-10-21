public class ByteArrayUtils {
	public static final int   NUMBER_OF_BITS_IN_A_BYTE = 8;
	public static final short MASK_TO_BYTE             = 0xFF;
	public static final int   SIZE_OF_AN_INT_IN_BYTES  = 4;

	public static void writeInt( byte[] p_dest,
								 int    p_toWrite )
	{
		assert( p_dest.length >= SIZE_OF_AN_INT_IN_BYTES )
				: "Programming error: p_dest is too short to hold an int";

		// unrolled loop of 4 iterations
		p_dest[0] = (byte)(p_toWrite & MASK_TO_BYTE);
		p_toWrite >>= NUMBER_OF_BITS_IN_A_BYTE;

		p_dest[1] = (byte)(p_toWrite & MASK_TO_BYTE);
		p_toWrite >>= NUMBER_OF_BITS_IN_A_BYTE;

		p_dest[2] = (byte)(p_toWrite & MASK_TO_BYTE);
		p_toWrite >>= NUMBER_OF_BITS_IN_A_BYTE;

		p_dest[3] = (byte)(p_toWrite & MASK_TO_BYTE);
	}

	public static int readInt( byte[] p_src ) // must be of size 4
	{
		assert( p_src.length >= SIZE_OF_AN_INT_IN_BYTES )
				: "Programming error: p_src is too short to hold an int";

		// unrolled loop of 4 iterations
		int result = (   p_src[ 0 ] & MASK_TO_BYTE );
		result    |= ( ( p_src[ 1 ] & MASK_TO_BYTE ) << 8 );
		result    |= ( ( p_src[ 2 ] & MASK_TO_BYTE ) << 16 );
		result    |= ( ( p_src[ 3 ] & MASK_TO_BYTE ) << 24 );

		return result;
	}
}