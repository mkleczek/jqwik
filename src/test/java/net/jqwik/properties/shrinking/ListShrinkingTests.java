package net.jqwik.properties.shrinking;

import net.jqwik.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class ListShrinkingTests {

	@Example
	void shrinkFromEmptyListReturnsNothing() {
		Shrinker<List<Integer>> shrinker = Shrinkers.list(new IntegerShrinker(-5, 5));
		ShrinkableChoice<List<Integer>> shrinkableChoice = (ShrinkableChoice<List<Integer>>) shrinker.shrink(Collections.emptyList());

		assertThat(shrinkableChoice.choices()).hasSize(0);
	}

	@Example
	void shrinkingSizeOfListFirst() {
		Shrinker<List<Integer>> shrinker = Shrinkers.list(new IntegerShrinker(-5, 5));
		List<Integer> listOf6 = Arrays.asList(0, 0, 0, 0, 0, 0);
		ShrinkableChoice<List<Integer>> shrinkableChoice = (ShrinkableChoice<List<Integer>>) shrinker.shrink(listOf6);

		assertThat(shrinkableChoice.choices()).hasSize(3);
		assertThat(shrinkableChoice.choices().get(0)).isInstanceOf(LazyShrinkable.class);
	}

}
