package com.renard.auto_adapter;

import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.view.View;

public class AutoAdapterViewHolderTest {

    @Mock
    View view;
    @Mock
    ViewBinder<TestItem> binder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private static class TestItem implements Unique {
        @Override
        public long getId() {
            return 0;
        }
    }

    @Test
    public void bind_should_call_binder() {
        TestItem testItem = new TestItem();
        AutoAdapterViewHolder<TestItem> viewHolder = new AutoAdapterViewHolder<>(view, binder, new int[]{});
        viewHolder.bind(testItem);
        verify(binder).bind(eq(testItem));
    }

}
