package com.tars.util.net.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * ByteFramer encapsulates reading and writing of new messages to a channel.
 *
 * It will add 4 bytes (int) representing the length of the next message.
 *
 * ByteFramers write() and flushWrite() are thread safe, but low or no contention is expected as it
 * is instantiated per client not for entire server.
 *
 * note [performance] can be improved if we use Bytes instead of ByteBuffer and get rid of any useless allocations
 */
class ByteFramer {

  final static Logger log = LoggerFactory.getLogger(ByteFramer.class);

  final ByteChannel channel;
  final ByteBuffer readBuffer;
  final ByteBuffer writeBuffer;
  LinkedList<byte[]> pending = new LinkedList<>();

  ByteFramer(ByteChannel channel, int bufferCapacity) {
    this.channel = channel;
    this.readBuffer = ByteBuffer.allocate(bufferCapacity);
    this.writeBuffer = ByteBuffer.allocate(bufferCapacity);
  }

  /**
   * Read all bytes currently in channel to readBuffer. This shall only be called from servers event
   * processor thread.
   *
   * @throws IllegalStateException if any exception takes place while reading data from channel
   */
  int read() throws IOException {
    // write data from channel to readBuffer
    int bytesRead = channel.read(this.readBuffer);

    log.trace("read {} bytes from channel", bytesRead);
    return bytesRead;
  }

  /**
   * Processes and publishes all complete messages to observable stream. This shall only be called
   * from servers event processor thread thus there is no need to synchronize on readBuffer.
   *
   * @throws IllegalStateException if any exception takes place while parsing messages from read
   *                               buffer
   */
  List<byte[]> flushRead() {
    List<byte[]> result = new ArrayList<>();

    log.trace("processing messages copied from channel to read buffer");

    // process data in the readBuffer
    readBuffer.flip();

    while (readBuffer.hasRemaining()) {
      // first 4 bytes represent the length of the message
      int length = readBuffer.getInt();

      log.trace("next message length : {}", length);

      // if next message is fully available in buffer
      if (readBuffer.remaining() >= length) {

        // read message to byte[]
        byte[] message = new byte[length];
        readBuffer.get(message, 0, length);

        log.trace("message read : {}", Arrays.toString(message));
        result.add(message);

        // if everything is consumed clean buffer for further use
        if (!readBuffer.hasRemaining()) {
          readBuffer.clear();
          break;
        }
      } else {
        log.trace(
            "message is incomplete, buffer filled : {}%",
            readBuffer.remaining() / readBuffer.capacity()
        );

        // retreat 4 bytes so that next time we read we get message length
        readBuffer.position(readBuffer.position() - 4);
        readBuffer.compact();
        break;
      }
    }
    return result;
  }

  /**
   * Frames and stores data in write buffer, but does not flush to channel. This method shall be
   * called from application thus synchronizing on writeBuffer.
   *
   * @throws IllegalStateException if data is bigger than write buffer capacity
   */
  void write(byte[] data) {
    if (data.length > writeBuffer.capacity()) {
      log.error("message is too big to ever fit in buffer(length : {})", data.length);
      throw new IllegalStateException("message is too big");
    }

    // write pending messages first if any
    while (!pending.isEmpty()) {
      byte[] next = pending.poll();

      if (writeBuffer.remaining() >= (next.length + 4)) {
        writeBuffer.putInt(next.length);
        writeBuffer.put(next);
      } else {
        pending.addFirst(next);
        log.warn("cant put next message, not enough space in buffer (flush needed)");
        break;
      }
    }

    // write the message to channel
    if (writeBuffer.remaining() >= (data.length + 4)) {
      writeBuffer.putInt(data.length);
      writeBuffer.put(data);
    } else {
      pending.add(data);
      log.warn("cant put next message, not enough space in buffer (flush needed)");
    }
  }

  /**
   * Flushes writeBuffer to channel. This method shall only be called from servers event processor
   * thread.
   *
   * @throws IllegalStateException if any exception takes place while flushing buffer to channel
   */
  void flushWrite() throws IOException {
    // prepare buffer for read
    writeBuffer.flip();
    // read from buffer write to channel
    channel.write(writeBuffer);
    // prepare buffer for write
    writeBuffer.compact();
    log.trace("flushed write buffer to channel");
  }

  boolean hasPendingWrites() {
    return writeBuffer.position() == 0 && writeBuffer.limit() == writeBuffer.capacity();
  }
}
