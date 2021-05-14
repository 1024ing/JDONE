/**  
 * 名称： ArrayList.java 
 * 功能：
 * 作者：ysk  
 * 时间： 2021-4-2  
 * Copyright:杭州威灿科技有限公司(c) 2021
 * 版本 1.0.1 
 */
package com.classSequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
public class ArrayList extends AbstractList implements List, RandomAccess,
		Cloneable, Serializable {
	static final class ArrayListSpliterator implements Spliterator {

		private int getFence() {
			int i;
			ArrayList arraylist;
			if ((i = fence) < 0)
				if ((arraylist = list) == null) {
					i = fence = 0;
				} else {
					expectedModCount = arraylist.modCount;
					i = fence = arraylist.size;
				}
			return i;
		}

		public ArrayListSpliterator trySplit1() {
			int i = getFence();
			int j = index;
			int k = j + i >>> 1;
			return j < k ? new ArrayListSpliterator(list, j, index = k,
					expectedModCount) : null;
		}

		public boolean tryAdvance(Consumer consumer) {
			if (consumer == null)
				throw new NullPointerException();
			int i = getFence();
			int j = index;
			if (j < i) {
				index = j + 1;
				Object obj = list.elementData[j];
				consumer.accept(obj);
				if (list.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				else
					return true;
			} else {
				return false;
			}
		}

		public void forEachRemaining(Consumer consumer) {
			if (consumer == null)
				throw new NullPointerException();
			ArrayList arraylist;
			Object aobj[];
			if ((arraylist = list) != null
					&& (aobj = arraylist.elementData) != null) {
				int j;
				int k;
				if ((j = fence) < 0) {
					k = arraylist.modCount;
					j = arraylist.size;
				} else {
					k = expectedModCount;
				}
				int i;
				if ((i = index) >= 0 && (index = j) <= aobj.length) {
					for (; i < j; i++) {
						Object obj = aobj[i];
						consumer.accept(obj);
					}

					if (arraylist.modCount == k)
						return;
				}
			}
			throw new ConcurrentModificationException();
		}

		public long estimateSize() {
			return (long) (getFence() - index);
		}

		public int characteristics() {
			return 16464;
		}

		public volatile Spliterator trySplit() {
			return trySplit1();
		}

		private final ArrayList list;
		private int index;
		private int fence;
		private int expectedModCount;

		ArrayListSpliterator(ArrayList arraylist, int i, int j, int k) {
			list = arraylist;
			index = i;
			fence = j;
			expectedModCount = k;
		}
	}

	private class Itr implements Iterator {

		public boolean hasNext() {
			return cursor != size;
		}

		public Object next() {
			checkForComodification();
			int i = cursor;
			if (i >= size)
				throw new NoSuchElementException();
			Object aobj[] = elementData;
			if (i >= aobj.length) {
				throw new ConcurrentModificationException();
			} else {
				cursor = i + 1;
				return aobj[lastRet = i];
			}
		}

		public void remove() {
			if (lastRet < 0)
				throw new IllegalStateException();
			checkForComodification();
			try {
				ArrayList.this.remove(lastRet);
				cursor = lastRet;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException indexoutofboundsexception) {
				throw new ConcurrentModificationException();
			}
		}

		public void forEachRemaining(Consumer consumer) {
			Objects.requireNonNull(consumer);
			int i = size;
			int j = cursor;
			if (j >= i)
				return;
			Object aobj[] = elementData;
			if (j >= aobj.length)
				throw new ConcurrentModificationException();
			while (j != i && modCount == expectedModCount)
				consumer.accept(aobj[j++]);
			cursor = j;
			lastRet = j - 1;
			checkForComodification();
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			else
				return;
		}

		int cursor;
		int lastRet;
		int expectedModCount;
		final ArrayList this$0;

		private Itr() {
			this$0 = ArrayList.this;
			super();
			lastRet = -1;
			expectedModCount = modCount;
		}

	}

	private class ListItr extends Itr implements ListIterator {

		public boolean hasPrevious() {
			return cursor != 0;
		}

		public int nextIndex() {
			return cursor;
		}

		public int previousIndex() {
			return cursor - 1;
		}

		public Object previous() {
			checkForComodification();
			int i = cursor - 1;
			if (i < 0)
				throw new NoSuchElementException();
			Object aobj[] = elementData;
			if (i >= aobj.length) {
				throw new ConcurrentModificationException();
			} else {
				cursor = i;
				return aobj[lastRet = i];
			}
		}

		public void set(Object obj) {
			if (lastRet < 0)
				throw new IllegalStateException();
			checkForComodification();
			try {
				ArrayList.this.set(lastRet, obj);
			} catch (IndexOutOfBoundsException indexoutofboundsexception) {
				throw new ConcurrentModificationException();
			}
		}

		public void add(Object obj) {
			checkForComodification();
			try {
				int i = cursor;
				ArrayList.this.add(i, obj);
				cursor = i + 1;
				lastRet = -1;
				expectedModCount = modCount;
			} catch (IndexOutOfBoundsException indexoutofboundsexception) {
				throw new ConcurrentModificationException();
			}
		}

		final ArrayList this$0;

		ListItr(int i) {
			this$0 = ArrayList.this;
			super();
			cursor = i;
		}
	}

	private class SubList extends AbstractList implements RandomAccess {

		public Object set(int i, Object obj) {
			rangeCheck(i);
			checkForComodification();
			Object obj1 = elementData(offset + i);
			elementData[offset + i] = obj;
			return obj1;
		}

		public Object get(int i) {
			rangeCheck(i);
			checkForComodification();
			return elementData(offset + i);
		}

		public int size() {
			checkForComodification();
			return size;
		}

		public void add(int i, Object obj) {
			rangeCheckForAdd(i);
			checkForComodification();
			parent.add(parentOffset + i, obj);
			modCount = parent.modCount;
			size++;
		}

		public Object remove(int i) {
			rangeCheck(i);
			checkForComodification();
			Object obj = parent.remove(parentOffset + i);
			modCount = parent.modCount;
			size--;
			return obj;
		}

		protected void removeRange(int i, int j) {
			checkForComodification();
			parent.removeRange(parentOffset + i, parentOffset + j);
			modCount = parent.modCount;
			size -= j - i;
		}

		public boolean addAll(Collection collection) {
			return addAll(size, collection);
		}

		public boolean addAll(int i, Collection collection) {
			rangeCheckForAdd(i);
			int j = collection.size();
			if (j == 0) {
				return false;
			} else {
				checkForComodification();
				parent.addAll(parentOffset + i, collection);
				modCount = parent.modCount;
				size += j;
				return true;
			}
		}

		public Iterator iterator() {
			return listIterator();
		}

		public ListIterator listIterator(final int index) {
			checkForComodification();
			rangeCheckForAdd(index);
			final int offset = this.offset;
			return new ListIterator() {

				public boolean hasNext() {
					return cursor != size;
				}

				public Object next() {
					checkForComodification();
					int i = cursor;
					if (i >= size)
						throw new NoSuchElementException();
					Object aobj[] = elementData;
					if (offset + i >= aobj.length) {
						throw new ConcurrentModificationException();
					} else {
						cursor = i + 1;
						return aobj[offset + (lastRet = i)];
					}
				}

				public boolean hasPrevious() {
					return cursor != 0;
				}

				public Object previous() {
					checkForComodification();
					int i = cursor - 1;
					if (i < 0)
						throw new NoSuchElementException();
					Object aobj[] = elementData;
					if (offset + i >= aobj.length) {
						throw new ConcurrentModificationException();
					} else {
						cursor = i;
						return aobj[offset + (lastRet = i)];
					}
				}

				public void forEachRemaining(Consumer consumer) {
					Objects.requireNonNull(consumer);
					int i = size;
					int j = cursor;
					if (j >= i)
						return;
					Object aobj[] = elementData;
					if (offset + j >= aobj.length)
						throw new ConcurrentModificationException();
					while (j != i && modCount == expectedModCount)
						consumer.accept(aobj[offset + j++]);
					lastRet = cursor = j;
					checkForComodification();
				}

				public int nextIndex() {
					return cursor;
				}

				public int previousIndex() {
					return cursor - 1;
				}

				public void remove() {
					if (lastRet < 0)
						throw new IllegalStateException();
					checkForComodification();
					try {
						SubList.this.remove(lastRet);
						cursor = lastRet;
						lastRet = -1;
						expectedModCount = modCount;
					} catch (IndexOutOfBoundsException indexoutofboundsexception) {
						throw new ConcurrentModificationException();
					}
				}

				public void set(Object obj) {
					if (lastRet < 0)
						throw new IllegalStateException();
					checkForComodification();
					try {
						ArrayList.this.set(offset + lastRet, obj);
					} catch (IndexOutOfBoundsException indexoutofboundsexception) {
						throw new ConcurrentModificationException();
					}
				}

				public void add(Object obj) {
					checkForComodification();
					try {
						int i = cursor;
						SubList.this.add(i, obj);
						cursor = i + 1;
						lastRet = -1;
						expectedModCount = modCount;
					} catch (IndexOutOfBoundsException indexoutofboundsexception) {
						throw new ConcurrentModificationException();
					}
				}

				final void checkForComodification() {
					if (expectedModCount != modCount)
						throw new ConcurrentModificationException();
					else
						return;
				}

				int cursor;
				int lastRet;
				int expectedModCount;
				final int val$index;
				final int val$offset;
				final SubList this$1;

				{
					this$1 = SubList.this;
					index = i;
					offset = j;
					super();
					cursor = index;
					lastRet = -1;
					expectedModCount = modCount;
				}
			};
		}

		public List subList(int i, int j) {
			ArrayList.subListRangeCheck(i, j, size);
			return new SubList(this, offset, i, j);
		}

		private void rangeCheck(int i) {
			if (i < 0 || i >= size)
				throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
			else
				return;
		}

		private void rangeCheckForAdd(int i) {
			if (i < 0 || i > size)
				throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
			else
				return;
		}

		private String outOfBoundsMsg(int i) {
			return (new StringBuilder()).append("Index: ").append(i)
					.append(", Size: ").append(size).toString();
		}

		private void checkForComodification() {
			if (modCount != modCount)
				throw new ConcurrentModificationException();
			else
				return;
		}

		public Spliterator spliterator() {
			checkForComodification();
			return new ArrayListSpliterator(ArrayList.this, offset, offset
					+ size, modCount);
		}

		private final AbstractList parent;
		private final int parentOffset;
		private final int offset;
		int size;
		final ArrayList this$0;

		SubList(AbstractList abstractlist, int i, int j, int k) {
			this$0 = ArrayList.this;
			super();
			parent = abstractlist;
			parentOffset = j;
			offset = i + j;
			size = k - j;
			modCount = modCount;
		}
	}

	public ArrayList(int i) {
		if (i > 0)
			elementData = new Object[i];
		else if (i == 0)
			elementData = EMPTY_ELEMENTDATA;
		else
			throw new IllegalArgumentException((new StringBuilder())
					.append("Illegal Capacity: ").append(i).toString());
	}

	public ArrayList() {
		elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
	}

	public ArrayList(Collection collection)
	{
		elementData = collection.toArray();
	if((size = elementData.length) != 0){
		if(((Object) (elementData)).getClass() != Object[].class)
		        elementData = Arrays.copyOf(elementData, size, Object[].class);
	} else{
	    	elementData = EMPTY_ELEMENTDATA;
		  }
	}

	public void trimToSize() {
		modCount++;
		if (size < elementData.length)
			elementData = size != 0 ? Arrays.copyOf(elementData, size)
					: EMPTY_ELEMENTDATA;
	}

	public void ensureCapacity(int i) {
		byte byte0 = ((byte) (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA ? 10
				: 0));
		if (i > byte0)
			ensureExplicitCapacity(i);
	}

	private void ensureCapacityInternal(int i) {
		if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
			i = Math.max(10, i);
		ensureExplicitCapacity(i);
	}

	private void ensureExplicitCapacity(int i) {
		modCount++;
		if (i - elementData.length > 0)
			grow(i);
	}

	private void grow(int i) {
		int j = elementData.length;
		int k = j + (j >> 1);
		if (k - i < 0)
			k = i;
		if (k - 2147483639 > 0)
			k = hugeCapacity(i);
		elementData = Arrays.copyOf(elementData, k);
	}

	private static int hugeCapacity(int i) {
		if (i < 0)
			throw new OutOfMemoryError();
		else
			return i <= 2147483639 ? 2147483639 : 2147483647;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean contains(Object obj) {
		return indexOf(obj) >= 0;
	}

	public int indexOf(Object obj) {
		if (obj == null) {
			for (int i = 0; i < size; i++)
				if (elementData[i] == null)
					return i;

		} else {
			for (int j = 0; j < size; j++)
				if (obj.equals(elementData[j]))
					return j;

		}
		return -1;
	}

	public int lastIndexOf(Object obj) {
		if (obj == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null)
					return i;

		} else {
			for (int j = size - 1; j >= 0; j--)
				if (obj.equals(elementData[j]))
					return j;

		}
		return -1;
	}

	public Object clone() {
		try {
			ArrayList arraylist = (ArrayList) super.clone();
			arraylist.elementData = Arrays.copyOf(elementData, size);
			arraylist.modCount = 0;
			return arraylist;
		} catch (CloneNotSupportedException clonenotsupportedexception) {
			throw new InternalError(clonenotsupportedexception);
		}
	}

	public Object[] toArray() {
		return Arrays.copyOf(elementData, size);
	}

	public Object[] toArray(Object aobj[]) {
		if (aobj.length < size)
			return (Object[]) Arrays.copyOf(elementData, size,
					((Object) (aobj)).getClass());
		System.arraycopy(((Object) (elementData)), 0, ((Object) (aobj)), 0,
				size);
		if (aobj.length > size)
			aobj[size] = null;
		return aobj;
	}

	Object elementData(int i) {
		return elementData[i];
	}

	public Object get(int i) {
		rangeCheck(i);
		return elementData(i);
	}

	public Object set(int i, Object obj) {
		rangeCheck(i);
		Object obj1 = elementData(i);
		elementData[i] = obj;
		return obj1;
	}

	public boolean add(Object obj) {
		ensureCapacityInternal(size + 1);
		elementData[size++] = obj;
		return true;
	}

	public void add(int i, Object obj) {
		rangeCheckForAdd(i);
		ensureCapacityInternal(size + 1);
		System.arraycopy(((Object) (elementData)), i, ((Object) (elementData)),
				i + 1, size - i);
		elementData[i] = obj;
		size++;
	}

	public Object remove(int i) {
		rangeCheck(i);
		modCount++;
		Object obj = elementData(i);
		int j = size - i - 1;
		if (j > 0)
			System.arraycopy(((Object) (elementData)), i + 1,
					((Object) (elementData)), i, j);
		elementData[--size] = null;
		return obj;
	}

	public boolean remove(Object obj) {
		if (obj == null) {
			for (int i = 0; i < size; i++)
				if (elementData[i] == null) {
					fastRemove(i);
					return true;
				}

		} else {
			for (int j = 0; j < size; j++)
				if (obj.equals(elementData[j])) {
					fastRemove(j);
					return true;
				}

		}
		return false;
	}

	private void fastRemove(int i) {
		modCount++;
		int j = size - i - 1;
		if (j > 0)
			System.arraycopy(((Object) (elementData)), i + 1,
					((Object) (elementData)), i, j);
		elementData[--size] = null;
	}

	public void clear() {
		modCount++;
		for (int i = 0; i < size; i++)
			elementData[i] = null;

		size = 0;
	}

	public boolean addAll(Collection collection) {
		Object aobj[] = collection.toArray();
		int i = aobj.length;
		ensureCapacityInternal(size + i);
		System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), size,
				i);
		size += i;
		return i != 0;
	}

	public boolean addAll(int i, Collection collection) {
		rangeCheckForAdd(i);
		Object aobj[] = collection.toArray();
		int j = aobj.length;
		ensureCapacityInternal(size + j);
		int k = size - i;
		if (k > 0)
			System.arraycopy(((Object) (elementData)), i,
					((Object) (elementData)), i + j, k);
		System.arraycopy(((Object) (aobj)), 0, ((Object) (elementData)), i, j);
		size += j;
		return j != 0;
	}

	protected void removeRange(int i, int j) {
		modCount++;
		int k = size - j;
		System.arraycopy(((Object) (elementData)), j, ((Object) (elementData)),
				i, k);
		int l = size - (j - i);
		for (int i1 = l; i1 < size; i1++)
			elementData[i1] = null;

		size = l;
	}

	private void rangeCheck(int i) {
		if (i >= size)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
		else
			return;
	}

	private void rangeCheckForAdd(int i) {
		if (i > size || i < 0)
			throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
		else
			return;
	}

	private String outOfBoundsMsg(int i) {
		return (new StringBuilder()).append("Index: ").append(i)
				.append(", Size: ").append(size).toString();
	}

	public boolean removeAll(Collection collection) {
		Objects.requireNonNull(collection);
		return batchRemove(collection, false);
	}

	public boolean retainAll(Collection collection) {
		Objects.requireNonNull(collection);
		return batchRemove(collection, true);
	}

	private boolean batchRemove(Collection collection, boolean flag)
{
Object aobj[];
int i;
int j;
boolean flag1;
aobj = elementData;
i = 0;
j = 0;
flag1 = false;
for(; i < size; i++)
    if(collection.contains(aobj[i]) == flag)
        aobj[j++] = aobj[i];

if(i != size)
{
    System.arraycopy(((Object) (aobj)), i, ((Object) (aobj)), j, size - i);
    j += size - i;
}
if(j != size)
{
    for(int k = j; k < size; k++)
        aobj[k] = null;

    modCount += size - j;
    size = j;
    flag1 = true;
}
break MISSING_BLOCK_LABEL_252;
Exception exception;
exception;
if(i != size)
{
    System.arraycopy(((Object) (aobj)), i, ((Object) (aobj)), j, size - i);
    j += size - i;
}
if(j != size)
{
    for(int l = j; l < size; l++)
        aobj[l] = null;

    modCount += size - j;
    size = j;
    flag1 = true;
}
throw exception;
return flag1;
}

	private void writeObject(ObjectOutputStream objectoutputstream)
			throws IOException {
		int i = modCount;
		objectoutputstream.defaultWriteObject();
		objectoutputstream.writeInt(size);
		for (int j = 0; j < size; j++)
			objectoutputstream.writeObject(elementData[j]);

		if (modCount != i)
			throw new ConcurrentModificationException();
		else
			return;
	}

	private void readObject(ObjectInputStream objectinputstream)
			throws IOException, ClassNotFoundException {
		elementData = EMPTY_ELEMENTDATA;
		objectinputstream.defaultReadObject();
		objectinputstream.readInt();
		if (size > 0) {
			ensureCapacityInternal(size);
			Object aobj[] = elementData;
			for (int i = 0; i < size; i++)
				aobj[i] = objectinputstream.readObject();

		}
	}

	public ListIterator listIterator(int i) {
		if (i < 0 || i > size)
			throw new IndexOutOfBoundsException((new StringBuilder())
					.append("Index: ").append(i).toString());
		else
			return new ListItr(i);
	}

	public ListIterator listIterator() {
		return new ListItr(0);
	}

	public Iterator iterator() {
		return new Itr();
	}

	public List subList(int i, int j) {
		subListRangeCheck(i, j, size);
		return new SubList(this, 0, i, j);
	}

	static void subListRangeCheck(int i, int j, int k) {
		if (i < 0)
			throw new IndexOutOfBoundsException((new StringBuilder())
					.append("fromIndex = ").append(i).toString());
		if (j > k)
			throw new IndexOutOfBoundsException((new StringBuilder())
					.append("toIndex = ").append(j).toString());
		if (i > j)
			throw new IllegalArgumentException((new StringBuilder())
					.append("fromIndex(").append(i).append(") > toIndex(")
					.append(j).append(")").toString());
		else
			return;
	}

	public void forEach(Consumer consumer) {
		Objects.requireNonNull(consumer);
		int i = modCount;
		Object aobj[] = (Object[]) elementData;
		int j = size;
		for (int k = 0; modCount == i && k < j; k++)
			consumer.accept(aobj[k]);

		if (modCount != i)
			throw new ConcurrentModificationException();
		else
			return;
	}

	public Spliterator spliterator() {
		return new ArrayListSpliterator(this, 0, -1, 0);
	}

	public boolean removeIf(Predicate predicate) {
		Objects.requireNonNull(predicate);
		int i = 0;
		BitSet bitset = new BitSet(size);
		int j = modCount;
		int k = size;
		for (int l = 0; modCount == j && l < k; l++) {
			Object obj = elementData[l];
			if (predicate.test(obj)) {
				bitset.set(l);
				i++;
			}
		}

		if (modCount != j)
			throw new ConcurrentModificationException();
		boolean flag = i > 0;
		if (flag) {
			int i1 = k - i;
			int j1 = 0;
			for (int l1 = 0; j1 < k && l1 < i1; l1++) {
				j1 = bitset.nextClearBit(j1);
				elementData[l1] = elementData[j1];
				j1++;
			}

			for (int k1 = i1; k1 < k; k1++)
				elementData[k1] = null;

			size = i1;
			if (modCount != j)
				throw new ConcurrentModificationException();
			modCount++;
		}
		return flag;
	}

	public void replaceAll(UnaryOperator unaryoperator) {
		Objects.requireNonNull(unaryoperator);
		int i = modCount;
		int j = size;
		for (int k = 0; modCount == i && k < j; k++)
			elementData[k] = unaryoperator.apply(elementData[k]);

		if (modCount != i) {
			throw new ConcurrentModificationException();
		} else {
			modCount++;
			return;
		}
	}

	public void sort(Comparator comparator) {
		int i = modCount;
		Arrays.sort((Object[]) elementData, 0, size, comparator);
		if (modCount != i) {
			throw new ConcurrentModificationException();
		} else {
			modCount++;
			return;
		}
	}

	private static final long serialVersionUID = 8683452581122892189L;
	//默认初始容量大小为 10
	private static final int DEFAULT_CAPACITY = 10;
	//第一次添加元素时知道该 elementData 从空的构造函数还是有参构造函数被初始化的。以便确认如何扩容
	private static final Object EMPTY_ELEMENTDATA[] = new Object[0];
	private static final Object DEFAULTCAPACITY_EMPTY_ELEMENTDATA[] = new Object[0];
	transient Object elementData[];
	// 实际元素个数 用来记录elementData 元素的个数的
	private int size;
	private static final int MAX_ARRAY_SIZE = 2147483639;

}
