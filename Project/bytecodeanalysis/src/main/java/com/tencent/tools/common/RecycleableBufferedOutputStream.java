package com.tencent.tools.common;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RecycleableBufferedOutputStream extends FilterOutputStream {
    private boolean used;
    private RecycleableBufferedOutputStream next;
    private static RecycleableBufferedOutputStream sPool;
    private final  int MAX_POOL_SIZE=4;
    private static int poolSize=0;
    private static final Object POOL_LOCK=new Object();
    /**
     * The buffer containing the bytes to be written to the target stream.
     */
    protected byte[] buf;

    /**
     * The total number of bytes inside the byte array {@code buf}.
     */
    protected int count;

    private RecycleableBufferedOutputStream(OutputStream out) {
        this (out,8192);
    }
    private RecycleableBufferedOutputStream(OutputStream out, int size)
    {
        super(out);
        buf= new byte[size];
        used=true;
    }

    public static RecycleableBufferedOutputStream obtain(OutputStream out)
    {
        RecycleableBufferedOutputStream recycleableBufferedOutputStream =null;
        synchronized (POOL_LOCK)
        {
            if(sPool!=null)
            {
                recycleableBufferedOutputStream =sPool;
                sPool= recycleableBufferedOutputStream.next;
                recycleableBufferedOutputStream.next=null;
                poolSize--;
            }
        }


        if(recycleableBufferedOutputStream !=null)
        {
            recycleableBufferedOutputStream.out=out;
            recycleableBufferedOutputStream.used=true;
        }
        else
        {
            recycleableBufferedOutputStream =new RecycleableBufferedOutputStream(out);
        }

        return recycleableBufferedOutputStream;
    }

    private void recycle()
    {


        clearForRecycle();
        synchronized (POOL_LOCK)
        {
            if(poolSize<MAX_POOL_SIZE)
            {
                next=sPool;
                sPool=this;
                poolSize++;
            }

        }

    }

    /**
     * Flushes this stream to ensure all pending data is written out to the
     * target stream. In addition, the target stream is flushed.
     *
     * @throws IOException
     *             if an error occurs attempting to flush this stream.
     */
    @Override
    public synchronized void flush() throws IOException {
        checkNotClosed();
        flushInternal();
        out.flush();
    }

    private void checkNotClosed() throws IOException {
        if (!used) {
            throw new IOException("BufferedOutputStream is closed");
        }
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * {@code offset} to this stream. If there is room in the buffer to hold the
     * bytes, they are copied in. If not, the buffered bytes plus the bytes in
     * {@code buffer} are written to the target stream, the target is flushed,
     * and the buffer is cleared.
     *
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the start position in {@code buffer} from where to get bytes.
     * @param length
     *            the number of bytes from {@code buffer} to write to this
     *            stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the size of
     *             {@code buffer}.
     * @throws IOException
     *             if an error occurs attempting to write to this stream.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @throws ArrayIndexOutOfBoundsException
     *             If offset or count is outside of bounds.
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int length) throws IOException {
        checkNotClosed();

        if (buffer == null) {
            throw new NullPointerException("buffer == null");
        }

        byte[] internalBuffer = buf;
        if (length >= internalBuffer.length) {
            flushInternal();
            out.write(buffer, offset, length);
            return;
        }

        checkOffsetAndCount(buffer.length, offset, length);

        // flush the internal buffer first if we have not enough space left
        if (length > (internalBuffer.length - count)) {
            flushInternal();
        }

        System.arraycopy(buffer, offset, internalBuffer, count, length);
        count += length;
    }
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new IndexOutOfBoundsException("length=" + arrayLength + "; regionStart=" + offset
                    + "; regionLength=" + count);
        }
    }
    @Override public synchronized void close() throws IOException {
        if (!used) {
            return;
        }

        try {
            super.close();
        } finally {
            recycle();
        }
    }

    private void clearForRecycle()
    {
        count=0;
        this.out=null;
        this.used=false;
    }
    /**
     * Writes one byte to this stream. Only the low order byte of the integer
     * {@code oneByte} is written. If there is room in the buffer, the byte is
     * copied into the buffer and the count incremented. Otherwise, the buffer
     * plus {@code oneByte} are written to the target stream, the target is
     * flushed, and the buffer is reset.
     *
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an error occurs attempting to write to this stream.
     */
    @Override
    public synchronized void write(int oneByte) throws IOException {
        checkNotClosed();
        if (count == buf.length) {
            out.write(buf, 0, count);
            count = 0;
        }
        buf[count++] = (byte) oneByte;
    }

    /**
     * Flushes only internal buffer.
     */
    private void flushInternal() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }
}
