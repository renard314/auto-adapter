package com.renard.auto_adapter;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AutoAdapterTest {

    private static final int MODEL_1_VIEW_TYPE = 1;
    private static final int MODEL_2_VIEW_TYPE = 2;
    private static final int ILLEGAL_VIEW_TYPE = 3;

    @Mock
    ViewHolderFactory<Model1> model1ViewHolderFactory;
    @Mock
    ViewHolderFactory<Model2> model2ViewHolderFactory;
    @Mock
    ViewBinder<Model1> model1ViewBinder;
    @Mock
    ViewBinder<Model2> model2ViewBinder;
    @Mock
    RecyclerView.AdapterDataObserver adapterDataObserver;
    @Mock
    ViewGroup view;

    private Model1 model1 = new Model1(0);
    private Model2 model2 = new Model2(1);
    private AutoAdapterViewHolder<Model1> model1ViewHolder = new AutoAdapterViewHolder<>(new LinearLayout(InstrumentationRegistry.getContext()), null);
    private AutoAdapter autoAdapter = new AutoAdapter() {
    };

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        model1ViewHolder = new AutoAdapterViewHolder<>(view, model1ViewBinder);
    }

    @Test
    public void testRemoveNotExistingItem() {
        givenAdapterFor2Models();

        whenModelIsAdded(model1);
        thenObserversGetNotifiedAboutInsertionAt(0);
        thenItemCountIs(1);

        whenModelIsRemoved(model2);
        thenObserversDontGetNotified();
        thenItemCountIs(1);
    }

    @Test
    public void testRemoveExistingItem() {
        givenAdapterFor2Models();

        whenModelIsAdded(model1);
        thenObserversGetNotifiedAboutInsertionAt(0);
        thenItemCountIs(1);

        whenModelIsAdded(model2);
        thenItemCountIs(2);
        thenObserversGetNotifiedAboutInsertionAt(1);

        whenModelIsRemoved(new Model2(1));
        thenObserversGetNotifiedAboutDeletionAt(1);
        thenItemCountIs(1);
    }

    @Test
    public void testGetItemViewType() {
        givenAdapterFor2Models();

        whenModelIsAdded(model1);
        whenModelIsAdded(model2);

        thenViewTypeAtPositionIs(0, MODEL_1_VIEW_TYPE);
        thenViewTypeAtPositionIs(1, MODEL_2_VIEW_TYPE);
    }

    @Test
    public void testOnBindViewHolder() {
        givenAdapterFor2Models();

        whenModelIsAdded(model1);

        thenItemCountIs(1);

        whenViewHolderIsBound(model1ViewHolder, 0);

        thenViewBinderGetsCalled();
    }

    @Test
    public void testCreateViewHolderForBothModels() {
        givenAdapterFor2Models();

        whenViewHolderIsCreatedForViewType(MODEL_2_VIEW_TYPE);
        thenViewHolderWasCreatedBy(model2ViewHolderFactory);

        whenViewHolderIsCreatedForViewType(MODEL_1_VIEW_TYPE);
        thenViewHolderWasCreatedBy(model1ViewHolderFactory);
    }

    @Test
    public void testCreateViewHolderForModel1() {
        givenAdapterFor2Models();

        whenViewHolderIsCreatedForViewType(MODEL_1_VIEW_TYPE);

        thenViewHolderWasCreatedBy(model1ViewHolderFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateUnknownViewHolder() {
        givenAdapterFor2Models();

        whenViewHolderIsCreatedForViewType(ILLEGAL_VIEW_TYPE);

        //then crash
    }

    @Test
    public void testCreateViewHolderForModel2() {
        givenAdapterFor2Models();

        whenViewHolderIsCreatedForViewType(MODEL_2_VIEW_TYPE);

        thenViewHolderWasCreatedBy(model2ViewHolderFactory);
    }

    @Test
    public void testAddingExistingItems() {
        givenAdapterFor2Models();

        whenModelIsAdded(new Model1(0));
        whenModelIsAdded(new Model1(0));

        thenItemCountIs(1);
        thenObserversGetNotifiedAboutInsertionAt(0);
        thenObserversGetNotifiedAboutChangeAt(0);
    }

    @Test
    public void testAddingNewItems() {
        givenAdapterFor2Models();

        whenModelIsAdded(new Model1(0));
        whenModelIsAdded(new Model2(1));

        thenItemCountIs(2);
        thenObserversGetNotifiedAboutInsertionAt(0);
        thenObserversGetNotifiedAboutInsertionAt(1);
    }

    private AutoAdapter givenAdapterFor2Models() {
        autoAdapter = new AutoAdapter() {
        };
        autoAdapter.registerAdapterDataObserver(adapterDataObserver);

        when(model1ViewHolderFactory.getViewType()).thenReturn(MODEL_1_VIEW_TYPE);
        when(model2ViewHolderFactory.getViewType()).thenReturn(MODEL_2_VIEW_TYPE);
        when(model1ViewHolderFactory.create(Matchers.<ViewGroup>any())).thenReturn(new AutoAdapterViewHolder<>(view, model1ViewBinder));
        when(model2ViewHolderFactory.create(Matchers.<ViewGroup>any())).thenReturn(new AutoAdapterViewHolder<>(view, model2ViewBinder));

        autoAdapter.putMapping(Model1.class, model1ViewHolderFactory);
        autoAdapter.putMapping(Model2.class, model2ViewHolderFactory);
        return autoAdapter;
    }

    private void whenModelIsRemoved(Unique item) {
        autoAdapter.removeItem(item);
    }

    private void whenModelIsAdded(final Unique model) {
        autoAdapter.addItem(model);
    }

    private void whenViewHolderIsCreatedForViewType(final int viewType) {
        autoAdapter.createViewHolder(view, viewType);
    }

    private void whenViewHolderIsBound(AutoAdapterViewHolder viewHolder, int position) {
        autoAdapter.bindViewHolder(viewHolder, position);
    }

    private void thenViewTypeAtPositionIs(int position, int viewType) {
        Assert.assertSame(autoAdapter.getItemViewType(position), viewType);
    }

    private void thenViewBinderGetsCalled() {
        verify(model1ViewBinder).bind(eq(model1));
    }

    private void thenObserversGetNotifiedAboutInsertionAt(final int index) {
        thenObserversGetNotifiedAboutInsertionAt(index, 1);
    }

    private void thenObserversGetNotifiedAboutInsertionAt(final int index, final int count) {
        verify(adapterDataObserver).onItemRangeInserted(eq(index), eq(count));
    }

    private void thenObserversGetNotifiedAboutChangeAt(final int index) {
        verify(adapterDataObserver).onItemRangeChanged(eq(index), eq(1), Matchers.any());
    }

    private void thenObserversGetNotifiedAboutDeletionAt(int index) {
        verify(adapterDataObserver).onItemRangeRemoved(eq(index), eq(1));
    }

    private void thenItemCountIs(final int count) {
        assertThat(autoAdapter.getItemCount(), is(count));
    }

    private void thenViewHolderWasCreatedBy(final ViewHolderFactory model1ViewHolderFactory) {
        verify(model1ViewHolderFactory).create(eq(view));
    }

    private void thenObserversDontGetNotified() {
        verifyNoMoreInteractions(adapterDataObserver);
    }

}
