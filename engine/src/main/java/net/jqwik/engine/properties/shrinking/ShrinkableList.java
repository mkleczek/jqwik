package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.engine.support.*;

public class ShrinkableList<E> extends ShrinkableContainer<List<E>, E> {

	private final int minSize;

	public ShrinkableList(List<Shrinkable<E>> elements, int minSize) {
		super(elements, minSize);
		this.minSize = minSize;
	}

	@Override
	Collector<E, ?, List<E>> containerCollector() {
		return Collectors.toList();
	}

	@Override
	Shrinkable<List<E>> createShrinkable(List<Shrinkable<E>> shrunkElements) {
		return new ShrinkableList<>(shrunkElements, minSize);
	}

	@Override
	public Stream<Shrinkable<List<E>>> shrink() {
		return JqwikStreamSupport.lazyConcat(
			this::shrinkSizeOfList,
			this::shrinkElementsOneAfterTheOther,
			this::shrinkPairsOfElements
		);
	}

	private Stream<Shrinkable<List<E>>> shrinkSizeOfList() {
		Set<List<Shrinkable<E>>> shrinkSizeOfListElements = new NEW_ShrinkSizeOfListCandidates<E>(minSize).candidatesFor(elements);
		return shrinkSizeOfListElements.stream().map(this::createShrinkable).sorted(Comparator.comparing(Shrinkable::distance));
	}

	private Stream<Shrinkable<List<E>>> shrinkElementsOneAfterTheOther() {
		List<Stream<Shrinkable<List<E>>>> shrinkPerElementStreams = new ArrayList<>();
		for (int i = 0; i < elements.size(); i++) {
			int index = i;
			Shrinkable<E> element = elements.get(i);
			List<Shrinkable<E>> elementsCopy = new ArrayList<>(elements);
			Stream<Shrinkable<List<E>>> shrinkElement = element.shrink().flatMap(shrunkElement -> {
				elementsCopy.set(index, shrunkElement);
				return Stream.of(createShrinkable(elementsCopy));
			});
			shrinkPerElementStreams.add(shrinkElement);
		}
		return JqwikStreamSupport.concat(shrinkPerElementStreams);
	}

	private Stream<Shrinkable<List<E>>> shrinkPairsOfElements() {
		if (elements.size() < 2) {
			return Stream.empty();
		}
		List<Supplier<Stream<Shrinkable<List<E>>>>> suppliers = new ArrayList<>();
		for (Tuple.Tuple2<Integer, Integer> pair : Combinatorics.distinctPairs(elements.size())) {
			Supplier<Stream<Shrinkable<List<E>>>> zip = () -> JqwikStreamSupport.zip(
				elements.get(pair.get1()).shrink(),
				elements.get(pair.get2()).shrink(),
				(Shrinkable<E> s1, Shrinkable<E> s2) -> {
					List<Shrinkable<E>> newElements = new ArrayList<>(elements);
					newElements.set(pair.get1(), s1);
					newElements.set(pair.get2(), s2);
					return createShrinkable(newElements);
				}
			);
			suppliers.add(zip);
		}
		return JqwikStreamSupport.lazyConcat(suppliers);
	}
}
