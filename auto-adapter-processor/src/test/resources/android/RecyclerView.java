package android.support.v7.widget;

import android.view.View;

public class RecyclerView {

    public abstract static class ViewHolder {
        public ViewHolder(View itemView) {
        }
    }

    public abstract static class Adapter<VH extends ViewHolder> {

        public Adapter() {
        }

        public abstract VH onCreateViewHolder(android.view.ViewGroup viewGroup, int i);

        public abstract void onBindViewHolder(VH vh, int i);

        public int getItemViewType(final int position) {
            return 0;
        }

        public abstract int getItemCount();

        public abstract long getItemId(final int position);

        public void setHasStableIds(boolean hasStableIds) {
        }

        public void notifyItemRemoved(int index) {
        }

        public void notifyItemRangeRemoved(int index, int count){
        }

        public void notifyItemChanged(int index) {
        }

        public void notifyItemInserted(int index) {
        }

        public void onViewDetachedFromWindow(final VH holder) {
        }

        public void onViewAttachedToWindow(final VH holder) {
        }
    }
}
