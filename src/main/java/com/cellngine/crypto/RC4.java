package com.cellngine.crypto;

/**
 * Provides a simple synchronized implementation of the Rivest cipher 4 random number generator and
 * encryption scheme.
 * 
 * @author qwertty <hellraz0r.386@googlemail.com>
 */
public class RC4 implements StreamCipher
{
	public static final int	SKIP_BYTES	= 1024;
	
	private int				a			= 0;
	private int				b			= 0;
	private int[]			s			= new int[256];
	
	/**
	 * Initialises the RC4 S-Box, thus preparing the algorithm to allow it to produce pseudo-random
	 * numbers. As the first few bytes of RC4's PRNG output leak information about the original key
	 * (see "<i>Weaknesses in the Key Scheduling Algorithm of RC4</i>" by Scott Fluhrer, Itsik
	 * Mantin and Adi Shamir), {@link RC4#SKIP_BYTES} of random data are automatically
	 * generated and discarded once the PRNG is ready.
	 * 
	 * @param seed
	 *            The seed to initialise the RC4 PRNG with.
	 * @throws IllegalArgumentException
	 *             If the key length is invalid (smaller than 1 or larger than 256).
	 */
	public RC4(final byte[] seed)
	{
		if (seed.length < 1) { throw new IllegalArgumentException("RC4 Key too short (minimum: 1 byte)"); }
		if (seed.length > 256) { throw new IllegalArgumentException("RC4 Key too long (maximum: 256 bytes)"); }
		
		for (int i = 0; i < 256; i++)
		{
			s[i] = i;
		}
		for (int i = 0; i < 256; i++)
		{
			b = (b + s[i] + byte2int(seed[i % seed.length])) % 256;
			swap(b, i);
		}
		b = 0;
		
		for (int i = 0; i < RC4.SKIP_BYTES; i++)
		{
			getByte();
		}
	}
	
	/**
	 * @return A single pseudorandom <code>byte</code> generated by the RC4 PRNG.
	 */
	private byte getByte()
	{
		// a = (a + 1) % 256;
		// b = (b + s[a]) % 256;
		a++;
		a %= 256;
		
		b += s[a];
		b %= 256;
		
		swap(a, b);
		return (byte) s[(s[a] + s[b]) % 256];
	}
	
	/**
	 * Applies the RC4 encryption scheme by XOR'ing the input with data from the RC4 PRNG.
	 * 
	 * @param input
	 *            The plain- or ciphertext that shall be en- or decrypted.
	 * @return The result of the XOR operation.
	 * @throws <code>NullPointerException</code> if <code>input</code> is <code>null</code>.
	 */
	private void crypt(final byte[] input)
	{
		if (input == null) { throw new NullPointerException(); }
		if (input.length == 0) { return; }
		
		for (int i = 0; i < input.length; i++)
		{
			input[i] ^= getByte();
		}
	}
	
	@Override
	public void encrypt(final byte[] input)
	{
		crypt(input);
	}
	
	@Override
	public void decrypt(final byte[] input)
	{
		crypt(input);
	}
	
	/**
	 * Swaps the values of two elements in the S-Box.
	 */
	private void swap(final int x, final int y)
	{
		final int temp = s[x];
		s[x] = s[y];
		s[y] = temp;
	}
	
	/**
	 * Converts an unsigned byte to an integer. (e.g. 0xff would be converted to 255)
	 */
	private int byte2int(final byte toInt)
	{
		return toInt & 0xff;
	}
}
