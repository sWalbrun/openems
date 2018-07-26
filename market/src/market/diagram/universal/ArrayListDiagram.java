package market.diagram.universal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import market.diagram.api.Diagram;
import market.diagram.api.Value;

public class ArrayListDiagram<T extends Value<T>> implements Diagram<T> {

	private List<Node<T>> tl;

	public ArrayListDiagram() {
		tl = new ArrayList<Node<T>>();
	}

	@Override
	public T getValue(Date at) {
		int index = -1 - Collections.binarySearch(tl, new Node<T>(at, false));
		try {
			Node<T> before = tl.get(index - 1);
			if (before.isStart()) {
				return before.getValue();
			} else if (at.getTime() == before.getTime()) {
				return before.getValue();
			}
		} catch (IndexOutOfBoundsException e) {
		}
		throw new NullPointerException();
	}

	@Override
	public void deletePeriod(Date from, Date to) {
		if (from.getTime() > to.getTime()) {
			return;
		}
		int index = -1 - Collections.binarySearch(tl, new Node<T>(from, true));
		Node<T> cutOffEnd = null;
		Node<T> cutOffStart = null;
		int latestType = 0; // 0 = undefined; 1 = start; 2 = end
		while (index < tl.size() && tl.get(index).getTime() <= to.getTime()) {
			Node<T> cur = tl.get(index);
			if (cur.isStart()) {
				cutOffStart = cur;
				latestType = 1;
			} else {
				if (latestType == 0) {
					cutOffEnd = cur;
				}
				latestType = 2;
			}
			tl.remove(index);
		}
		if (cutOffEnd != null) {
			binaryInsert(new Node<T>(new Date(from.getTime() - 1), cutOffEnd.getValue(), false));
		}
		if (latestType == 1) {
			binaryInsert(new Node<T>(new Date(to.getTime() + 1), cutOffStart.getValue(), true));
		}
		// no nodes inside period to delete -> period could be completely inside
		// one block
		if (latestType != 1 && cutOffEnd == null) {
			try {
				Node<T> before = tl.get(index - 1);
				if (before.isStart()) {
					// suspicion true
					binaryInsert(new Node<T>(new Date(from.getTime() - 1), before.getValue(), false));
					binaryInsert(new Node<T>(new Date(to.getTime() + 1), before.getValue(), true));
				}
			} catch (IndexOutOfBoundsException exc) {

			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getAvg(Date from, Date to) {
		long duration = to.getTime() - from.getTime() + 1;
		if (duration < 0) {
			throw new NullPointerException();
		}
		if (duration == 0) {
			throw new NullPointerException();
		}
		if (duration == 1) {
			return getValue(from);
		}

		T sum = null;
		int index = -1 - Collections.binarySearch(tl, new Node<T>(from, true));
		Node<T> curStart = null;
		Node<T> curEnd = null;
		while (index < tl.size() && (curStart == null || curStart.getTime() <= from.getTime() + duration - 1)) {
			if (tl.get(index).isStart()) {
				// count time since curEnd as 0.0
				curStart = tl.get(index);
			} else {
				curEnd = tl.get(index);
				long startTime = from.getTime();
				long endTime = from.getTime() + duration - 1;
				if (curStart != null) {
					startTime = curStart.getTime();
				}
				if (curEnd.getTime() < endTime) {
					endTime = curEnd.getTime();
				}
				T endCopy = null;
				try {
					endCopy = (T) curEnd.getValue().clone();
					endCopy = endCopy.multiply(endTime - startTime + 1);
				} catch (CloneNotSupportedException | NullPointerException e) {
					e.printStackTrace();
				}
				if (sum == null) {
					sum = endCopy;
				} else {
					sum = endCopy.add(sum);
				}
			}
			index++;
		}
		return sum.divide(duration);
	}

	@Override
	public void setValue(Date from, Date to, T value) {
		if (from.getTime() > to.getTime()) {
			return;
		}
		int s = binaryInsert(new Node<T>(from, value, true));
		int e = binaryInsert(new Node<T>(to, value, false));

		// overwrite old nodes
		Node<T> cutOffEnd = null;
		Node<T> cutOffStart = null;
		int latestType = 0; // 0 = undefined; 1 = start; 2 = end
		while (s < e - 1) {
			Node<T> cur = tl.get(s + 1);
			if (cur.isStart()) {
				cutOffStart = cur;
				latestType = 1;
			} else {
				if (latestType == 0) {
					cutOffEnd = cur;
				}
				latestType = 2;
			}
			tl.remove(s + 1);
			e--;
		}
		if (latestType == 1) {
			tl.add(e + 1, new Node<T>(new Date(to.getTime() + 1), cutOffStart.getValue(), true));
		}
		if (cutOffEnd != null) {
			// there must be a start, if a cut-off-end exists -> tl.get(s-1) can't be null
			tl.add(s, new Node<T>(new Date(from.getTime() - 1), cutOffEnd.getValue(), false));
		}
		// no nodes inside new time-range -> new time-range could be completely inside
		// an old one
		if (latestType != 1 && cutOffEnd == null) {
			try {
				Node<T> before = tl.get(s - 1);
				if (before.isStart()) {
					// suspicion true
					tl.add(s, new Node<T>(new Date(from.getTime() - 1), before.getValue(), false));
					s++;
					e++;
					tl.add(e + 1, new Node<T>(new Date(to.getTime() + 1), tl.get(e + 1).getValue(), true));
				}
			} catch (IndexOutOfBoundsException exc) {

			}
		}

	}

	public void print() {
		for (Node<T> n : tl) {
			if (n.isStart()) {
				System.out.println("->->->->->->->->: " + n.getTime());
				System.out.println();
				System.out.println(n.getValue().toString());
			} else {
				System.out.println();
				System.out.println("<-<-<-<-<-<-<-<-: " + n.getTime());
			}
		}
	}

	private int binaryInsert(Node<T> node) {
		int index = -1 - Collections.binarySearch(tl, node);
		tl.add(index, node);
		return index;
	}

}
