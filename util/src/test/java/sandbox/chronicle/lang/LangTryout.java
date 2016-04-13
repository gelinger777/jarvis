package sandbox.chronicle.lang;

import net.openhft.lang.collection.HugeCollections;
import net.openhft.lang.collection.HugeQueue;

public class LangTryout {

  public static void main(String[] args) {
    // can create an array of any size (provided you have the memory) off heap.
//    HugeArray<DataType> array = HugeCollections.newArray(DataType.class, 20);
//    DataType dt = array.get(0);
//
//    for (int i = 0; i < array.length(); i++) {
//      DataType ref = array.get(i);
//      System.out.printf("%s : %s\n", i, ref.getValue());
//      ref.setValue(i);
//      array.recycle(ref);
//    }
//
//    for (int i = 0; i < array.length(); i++) {
//      DataType ref = array.get(i);
//      System.out.printf("%s : %s\n", i, ref.getValue());
//      array.recycle(ref);
//    }

    // create a ring writeBuffer
    HugeQueue<DataType> queue = HugeCollections.newQueue(DataType.class, 10 * 1000 * 1000L);

    // give me a reference to an object to populate
    DataType dt2 = queue.offer();
    // set the values od dt2
    queue.recycle(dt2);

    DataType dt3 = queue.take();
    // get values
    queue.recycle(dt3);
  }

  public interface DataType {

    int getValue();

    void setValue(int value);
  }
}



